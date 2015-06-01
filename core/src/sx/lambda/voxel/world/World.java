package sx.lambda.voxel.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntMap;
import io.netty.util.internal.ConcurrentSet;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.api.VoxelGameAPI;
import sx.lambda.voxel.api.events.worldgen.EventFinishChunkGen;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.entity.Entity;
import sx.lambda.voxel.entity.EntityPosition;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.chunk.Chunk;
import sx.lambda.voxel.world.chunk.IChunk;
import sx.lambda.voxel.world.generation.ChunkGenerator;
import sx.lambda.voxel.world.generation.SimplexChunkGenerator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

public class World implements IWorld {

    private static final int CHUNK_SIZE = 16;

    private static final int WORLD_HEIGHT = 128;

    private static final int SEA_LEVEL = 64;

    private final IntMap<IntMap<IChunk>> chunkMapX = new IntMap<>();
    private final Set<IChunk> chunkList = new ConcurrentSet<>();
    private List<IChunk> sortedChunkList;

    private IChunk lastPlayerChunk;

    private static final float GRAVITY = 6f;

    private static final float TERMINAL_VELOCITY = 56;

    private final ChunkGenerator chunkGen;

    private final boolean remote, server;

    private List<Entity> loadedEntities = new CopyOnWriteArrayList<>();

    private Set<IChunk> chunksToRerender = Collections.newSetFromMap(new ConcurrentHashMap<IChunk, Boolean>());

    private Queue<int[]> sunlightQueue = new ConcurrentLinkedQueue<>();
    private Queue<int[]> sunlightRemovalQueue = new ConcurrentLinkedQueue<>();

    private ModelBatch modelBatch;

    private boolean shouldUpdateLight;

    private int lightUpdaters;

    private int chunksMeshing;

    public World(boolean remote, boolean server) {
        this.remote = remote;
        this.server = server;
        if (!remote) {
            this.chunkGen = new SimplexChunkGenerator(this, 200, new Random().nextInt());
        } else {
            this.chunkGen = null;
        }
    }

    public int getChunkSize() {
        return CHUNK_SIZE;
    }

    public int getHeight() {
        return WORLD_HEIGHT;
    }

    public IChunk getChunkAtPosition(Vec3i position) {
        return getChunkAtPosition(position.x, position.z);
    }

    @Override
    public IChunk getChunkAtPosition(int x, int z) {
        x = getChunkPosition(x);
        z = getChunkPosition(z);

        IntMap<IChunk> zMap = null;
        try {
            zMap = this.chunkMapX.get(x);
        } catch(ArrayIndexOutOfBoundsException ex) { // Sometimes the libgdx intmap will give aioob. Not sure why.
            ex.printStackTrace();
        }
        if(zMap == null)
            return null;

        IChunk chunk = null;
        try {
            chunk = zMap.get(z);
        } catch(ArrayIndexOutOfBoundsException ex) { // Sometimes the libgdx intmap will give aioob. Not sure why.
            ex.printStackTrace();
        }

        return chunk;
    }

    private void removeChunkFromMap(Vec3i pos) {
        removeChunkFromMap(pos.x, pos.z);
    }

    private void removeChunkFromMap(int x, int z) {
        IntMap<IChunk> zMap = this.chunkMapX.get(x);
        if(zMap == null)
            return;

        zMap.remove(z);
    }

    @Override
    public void render() {
        assert !server;

        if (modelBatch == null) {
            modelBatch = new ModelBatch(Gdx.files.internal("shaders/gdx/world.vert.glsl"), Gdx.files.internal("shaders/gdx/world.frag.glsl"));
        }

        if (lightUpdaters < 2 && (sunlightQueue.size() > 0 || sunlightRemovalQueue.size() > 0 || shouldUpdateLight)) {
            processLightQueue(); // If a chunk is doing its rerender, we want it to have the most recent lighting possible
        }
        for (IChunk c : chunksToRerender) {
            if(VoxelGameClient.getInstance().getPlayer().getPosition().planeDistance(c.getStartPosition().x, c.getStartPosition().z) <=
                    VoxelGameClient.getInstance().getSettingsManager().getVisualSettings().getViewDistance()*CHUNK_SIZE) {
                c.rerender();
                chunksToRerender.remove(c);
            }
        }

        final IChunk playerChunk = getChunkAtPosition(
                MathUtils.floor(VoxelGameClient.getInstance().getPlayer().getPosition().getX()),
                MathUtils.floor(VoxelGameClient.getInstance().getPlayer().getPosition().getZ()));
        if(playerChunk != null && (playerChunk != lastPlayerChunk || (sortedChunkList != null && chunkList.size() != sortedChunkList.size()))) {
            sortedChunkList = new ArrayList<>();
            for(IChunk c : chunkList) {
                if (VoxelGameClient.getInstance().getPlayer().getPosition().planeDistance(c.getStartPosition().x, c.getStartPosition().z) <=
                        VoxelGameClient.getInstance().getSettingsManager().getVisualSettings().getViewDistance() * CHUNK_SIZE) {
                    sortedChunkList.add(c);
                }
            }
            Collections.sort(sortedChunkList, new Comparator<IChunk>() {
                @Override
                public int compare(IChunk c1, IChunk c2) {
                    int xDiff1 = playerChunk.getStartPosition().x - c1.getStartPosition().x;
                    int zDiff1 = playerChunk.getStartPosition().z - c1.getStartPosition().z;
                    int xDiff2 = playerChunk.getStartPosition().x - c2.getStartPosition().x;
                    int zDiff2 = playerChunk.getStartPosition().z - c2.getStartPosition().z;
                    int sqDist2 = xDiff2*xDiff2 + zDiff2*zDiff2;
                    int sqDist1 = xDiff1*xDiff1 + zDiff1*zDiff1;
                    return sqDist2 - sqDist1;
                }
            });
            lastPlayerChunk = playerChunk;
        }

        if(sortedChunkList != null) {
            long renderStartNS = System.nanoTime();
            modelBatch.begin(VoxelGameClient.getInstance().getCamera());
            if(VoxelGameClient.getInstance().isWireframe())
                Gdx.gl.glLineWidth(5);
            boolean[] chunkVisible = new boolean[sortedChunkList.size()];
            int chunkNum = 0;
            for (IChunk c : sortedChunkList) {
                int x = c.getStartPosition().x;
                int z = c.getStartPosition().z;
                int halfWidth = getChunkSize()/2;
                int midX = x + halfWidth;
                int midZ = z + halfWidth;
                int midY = c.getHighestPoint()/2;
                boolean visible = VoxelGameClient.getInstance().getGameRenderer().getFrustum().boundsInFrustum(midX, midY, midZ, halfWidth, midY, halfWidth);
                chunkVisible[chunkNum] = visible;
                chunkNum++;
                if(visible) {
                    c.render(modelBatch);
                }
            }
            chunkNum = 0;
            for (IChunk c : sortedChunkList) {
                if(chunkVisible[chunkNum]) {
                    c.renderTranslucent(modelBatch);
                }
                chunkNum++;
            }
            modelBatch.end();
            if (VoxelGameClient.getInstance().numChunkRenders == 100) {  // Reset every 100 renders
                VoxelGameClient.getInstance().numChunkRenders = 0;
                VoxelGameClient.getInstance().chunkRenderTimes = 0;
            }
            VoxelGameClient.getInstance().chunkRenderTimes += (int) (System.nanoTime() - renderStartNS);
            VoxelGameClient.getInstance().numChunkRenders++;
        }
    }

    @Override
    public void loadChunks(EntityPosition playerPosition, int viewDistance) {
        if (!remote) { //don't gen chunks if we're not local
            this.getChunksInRange(playerPosition, viewDistance);
        }

    }

    @Override
    public int getSeaLevel() {
        return SEA_LEVEL;
    }

    @Override
    public int getChunkPosition(float value) {
        int subtraction = Math.floorMod(MathUtils.floor(value), CHUNK_SIZE);
        return MathUtils.floor(value - subtraction);
    }

    @Override
    public float getGravity() {
        return GRAVITY;
    }

    @Override
    public float applyGravity(float velocity, long ms) {
        if (ms < 0) ms = 0 - ms;
        return Math.max(-TERMINAL_VELOCITY, velocity - (getGravity() / 1000) * (ms / 10f));
    }

    @Override
    public void removeBlock(int x, int y, int z) {
        synchronized (this) {
            final IChunk c = this.getChunkAtPosition(x, z);
            if (c != null) {
                c.removeBlock(x, y, z);
                if (!server) {
                    if (Math.floorMod(x, CHUNK_SIZE) == CHUNK_SIZE-1) {
                        rerenderChunk(getChunkAtPosition(x + 1, z));
                    } else if (x % CHUNK_SIZE == 0) {
                        rerenderChunk(getChunkAtPosition(x - 1, z));
                    }

                    if (Math.floorMod(z, CHUNK_SIZE) == CHUNK_SIZE-1) {
                        rerenderChunk(getChunkAtPosition(x, z + 1));
                    } else if (z % CHUNK_SIZE == 0) {
                        rerenderChunk(getChunkAtPosition(x, z - 1));
                    }
                }
            }
        }
    }

    @Override
    public void addBlock(int block, int x, int y, int z) {
        synchronized (this) {
            final IChunk c = this.getChunkAtPosition(x, z);
            if(c != null)
                c.addBlock(block, x, y, z);
        }
    }

    @Override
    public IChunk[] getChunksInRange(EntityPosition epos, int viewDistance) {
        List<IChunk> chunkList = new ArrayList<>();
        int playerChunkX = getChunkPosition(epos.getX());
        int playerChunkZ = getChunkPosition(epos.getZ());
        int range = viewDistance * CHUNK_SIZE;
        for (int x = playerChunkX - range; x < playerChunkX + range; x += CHUNK_SIZE) {
            for (int z = playerChunkZ - range; z < playerChunkZ + range; z += CHUNK_SIZE) {
                chunkList.add(loadChunk(x, z));
            }
        }
        return chunkList.toArray(new IChunk[chunkList.size()]);
    }

    @Override
    public void addChunk(final IChunk chunk) {
        IChunk c = getChunkAtPosition(chunk.getStartPosition());
        if (c != null) {
            removeChunkFromMap(chunk.getStartPosition());
            this.chunkList.remove(c);
        }
        addChunk(chunk, chunk.getStartPosition().x, chunk.getStartPosition().z);

        addSun(chunk);
    }

    @Override
    public List<Entity> getLoadedEntities() {
        return this.loadedEntities;
    }

    private void addChunk(IChunk chunk, int x, int z) {
        IntMap<IChunk> foundChunkMapZ = this.chunkMapX.get(x);
        if(foundChunkMapZ == null) {
            foundChunkMapZ = new IntMap<>();
            this.chunkMapX.put(x, foundChunkMapZ);
        }
        foundChunkMapZ.put(z, chunk);
        this.chunkList.add(chunk);
    }

    private IChunk loadChunk(int startX, int startZ) {
        IChunk foundChunk = getChunkAtPosition(startX, startZ);
        if (foundChunk == null && !remote) {
            final IChunk c = new Chunk(this, new Vec3i(startX, 0, startZ), VoxelGameAPI.instance.getBiomeByID(0));
            VoxelGameAPI.instance.getEventManager().push(new EventFinishChunkGen(c));
            addChunk(c, startX, startZ);
            addSun(c);
            return c;
        } else {
            return foundChunk;
        }
    }

    private void addSun(IChunk c) {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                c.setSunlight(x, WORLD_HEIGHT - 1, z, 16);
                addToSunlightQueue(new int[]{c.getStartPosition().x + x, WORLD_HEIGHT - 1, c.getStartPosition().z + z});
            }
        }
        c.finishChangingSunlight();
    }

    public void addEntity(Entity e) {
        loadedEntities.add(e);
    }

    @Override
    public void rerenderChunk(IChunk c) {
        if(c == null)
            return;
        chunksToRerender.add(c);
    }

    @Override
    public ChunkGenerator getChunkGen() {
        return this.chunkGen;
    }

    /**
     * Add a block to a list of blocks to process sunlight for
     * The block at the position passed should be translucent or null and have a sunlight level greater than 0
     */
    @Override
    public void addToSunlightQueue(int[] pos) {
        assert pos.length == 3;
        sunlightQueue.add(pos);
    }

    @Override
    public void addToSunlightRemovalQueue(int[] pos) {
        assert pos.length == 3;
        sunlightRemovalQueue.add(pos);
    }

    @Override
    public void processLightQueue() {
        if (sunlightQueue.isEmpty() && sunlightRemovalQueue.isEmpty())
            return;
        shouldUpdateLight = true;
        if (lightUpdaters < 2) {
            lightUpdaters++;
            new Thread("Light update") {
                @Override
                public void run() {
                    shouldUpdateLight = false;

                    processLightRemovalQueue();

                    Queue<IChunk> changedChunks = new LinkedBlockingDeque<>();
                    int[] pos;
                    while ((pos = sunlightQueue.poll()) != null) {
                        int x = pos[0];
                        int y = pos[1];
                        int z = pos[2];
                        IChunk posChunk = getChunkAtPosition(x, z);
                        if (posChunk == null) {
                            continue;
                        }
                        int ll = posChunk.getSunlight(x, y, z);
                        int nextLL = ll - 1;

                        int negX = x - 1;
                        int posX = x + 1;
                        int negZ = z - 1;
                        int posZ = z + 1;
                        IChunk negXNeighborChunk = posChunk;
                        IChunk posXNeighborChunk = posChunk;
                        IChunk negZNeighborChunk = posChunk;
                        IChunk posZNeighborChunk = posChunk;
                        if(x % CHUNK_SIZE == 0) {
                            negXNeighborChunk = getChunkAtPosition(negX, z);
                        } else if(Math.floorMod(x, CHUNK_SIZE) == CHUNK_SIZE-1) {
                            posXNeighborChunk = getChunkAtPosition(posX, z);
                        }
                        if(z % CHUNK_SIZE == 0) {
                            negZNeighborChunk = getChunkAtPosition(x, negZ);
                        } else if(Math.floorMod(z, CHUNK_SIZE) == CHUNK_SIZE-1) {
                            posZNeighborChunk = getChunkAtPosition(x, posZ);
                        }

                        if (negXNeighborChunk != null) {
                            Block bl = negXNeighborChunk.getBlockAtPosition(negX, y, z);
                            if (bl == null || bl.doesLightPassThrough()) {
                                if (negXNeighborChunk.getSunlight(negX, y, z) < nextLL) {
                                    negXNeighborChunk.setSunlight(negX, y, z, nextLL);
                                    sunlightQueue.add(new int[]{negX, y, z});
                                    changedChunks.add(negXNeighborChunk);
                                }
                            }
                        }
                        if (posXNeighborChunk != null) {
                            Block bl = posXNeighborChunk.getBlockAtPosition(posX, y, z);
                            if (bl == null || bl.doesLightPassThrough()) {
                                if (posXNeighborChunk.getSunlight(posX, y, z) < nextLL) {
                                    posXNeighborChunk.setSunlight(posX, y, z, nextLL);
                                    sunlightQueue.add(new int[]{posX, y, z});
                                    changedChunks.add(posXNeighborChunk);
                                }
                            }
                        }
                        if (negZNeighborChunk != null) {
                            Block bl = negZNeighborChunk.getBlockAtPosition(x, y, negZ);
                            if (bl == null || bl.doesLightPassThrough()) {
                                if (negZNeighborChunk.getSunlight(x, y, negZ) < nextLL) {
                                    negZNeighborChunk.setSunlight(x, y, negZ, nextLL);
                                    sunlightQueue.add(new int[]{x, y, negZ});
                                    changedChunks.add(negZNeighborChunk);
                                }
                            }
                        }
                        if (posZNeighborChunk != null) {
                            Block bl = posZNeighborChunk.getBlockAtPosition(x, y, posZ);
                            if (bl == null || bl.doesLightPassThrough()) {
                                if (posZNeighborChunk.getSunlight(x, y, posZ) < nextLL) {
                                    posZNeighborChunk.setSunlight(x, y, posZ, nextLL);
                                    sunlightQueue.add(new int[]{x, y, posZ});
                                    changedChunks.add(posZNeighborChunk);
                                }
                            }
                        }

                        if (y < WORLD_HEIGHT - 2) {
                            int posY = y + 1;
                            Block posYBlock = posChunk.getBlockAtPosition(x, posY, z);
                            if (posYBlock == null || posYBlock.doesLightPassThrough()) {
                                if (nextLL > posChunk.getSunlight(x, posY, z)) {
                                    posChunk.setSunlight(x, posY, z, nextLL);
                                    sunlightQueue.add(new int[]{x, posY, z});
                                    changedChunks.add(posChunk);
                                }
                            }
                        }

                        if (y > 0) {
                            int negY = y - 1;
                            Block bl = posChunk.getBlockAtPosition(x, y, z);
                            Block negYBlock = posChunk.getBlockAtPosition(x, negY, z);
                            if (negYBlock == null || negYBlock.doesLightPassThrough() ||
                                    !negYBlock.decreasesLight()) {
                                boolean maxLL = ll == 16 && (bl == null || bl.decreasesLight());
                                if (posChunk.getSunlight(x, negY, z) < (maxLL ? 16 : nextLL)) {
                                    posChunk.setSunlight(x, negY, z, (maxLL ? 16 : nextLL));
                                    sunlightQueue.add(new int[]{x, negY, z});
                                    changedChunks.add(posChunk);
                                }
                            }
                        }
                    }
                    IChunk changedChunk;
                    while ((changedChunk = changedChunks.poll()) != null) {
                        changedChunk.finishChangingSunlight();
                    }
                    lightUpdaters--;
                }
            }.start();
        }
    }

    private void processLightRemovalQueue() {
        if (!sunlightRemovalQueue.isEmpty()) {
            Queue<IChunk> changedChunks = new LinkedBlockingDeque<>();
            int[] pos;
            while ((pos = sunlightRemovalQueue.poll()) != null) {
                int x = pos[0];
                int y = pos[1];
                int z = pos[2];
                IChunk posChunk = getChunkAtPosition(x, z);
                if (posChunk == null) {
                    continue;
                }
                int ll = posChunk.getSunlight(x, y, z);

                int negX = x - 1;
                int posX = x + 1;
                int negZ = x - 1;
                int posZ = x + 1;
                IChunk negXNeighborChunk = getChunkAtPosition(negX, z);
                IChunk posXNeighborChunk = getChunkAtPosition(posX, z);
                IChunk negZNeighborChunk = getChunkAtPosition(x, negZ);
                IChunk posZNeighborChunk = getChunkAtPosition(x, posZ);

                if (negXNeighborChunk != null) {
                    Block bl = negXNeighborChunk.getBlockAtPosition(negX, y, z);
                    int bll = negXNeighborChunk.getSunlight(negX, y, z);
                    if (bll < ll && bll != 0) {
                        if (bl == null || bl.doesLightPassThrough()) {
                            sunlightRemovalQueue.add(new int[]{negX, y, z});
                        }
                    } else if (bll >= ll) {
                        if (bl == null || bl.doesLightPassThrough()) {
                            sunlightQueue.add(new int[]{negX, y, z});
                        }
                    }
                }
                if (posXNeighborChunk != null) {
                    Block bl = posXNeighborChunk.getBlockAtPosition(posX, y, z);
                    int bll = posXNeighborChunk.getSunlight(posX, y, z);
                    if (bll < ll && bll != 0) {
                        if (bl == null || bl.doesLightPassThrough()) {
                            sunlightRemovalQueue.add(new int[]{posX, y, z});
                        }
                    } else if (bll >= ll) {
                        if (bl == null || bl.doesLightPassThrough()) {
                            sunlightQueue.add(new int[]{posX, y, z});
                        }
                    }
                }
                if (negZNeighborChunk != null) {
                    Block bl = negZNeighborChunk.getBlockAtPosition(x, y, negZ);
                    int bll = negZNeighborChunk.getSunlight(x, y, negZ);
                    if (bll < ll && bll != 0) {
                        if (bl == null || bl.doesLightPassThrough()) {
                            sunlightRemovalQueue.add(new int[]{x, y, negZ});
                        }
                    } else if (bll >= ll) {
                        if (bl == null || bl.doesLightPassThrough()) {
                            sunlightQueue.add(new int[]{x, y, negZ});
                        }
                    }
                }
                if (posZNeighborChunk != null) {
                    Block bl = posZNeighborChunk.getBlockAtPosition(x, y, posZ);
                    int bll = posZNeighborChunk.getSunlight(x, y, posZ);
                    if (bll < ll && bll != 0) {
                        if (bl == null || bl.doesLightPassThrough()) {
                            sunlightRemovalQueue.add(new int[]{x, y, posZ});
                        }
                    } else if (bll >= ll) {
                        if (bl == null || bl.doesLightPassThrough()) {
                            sunlightQueue.add(new int[]{x, y, posZ});
                        }
                    }
                }

                if (y > 0) {
                    int negY = y - 1;
                    Block negYBlock = posChunk.getBlockAtPosition(x, negY, z);
                    if (negYBlock == null || negYBlock.doesLightPassThrough()) {
                        if (posChunk.getSunlight(x, negY, z) != 0) {
                            sunlightRemovalQueue.add(new int[]{x, negY, z});
                        }
                    }
                }

                posChunk.setSunlight(x, y, z, 0);
                changedChunks.add(posChunk);
            }
            IChunk changedChunk;
            while ((changedChunk = changedChunks.poll()) != null) {
                changedChunk.finishChangingSunlight();
            }
        }
    }

    @Override
    public float getLightLevel(Vec3i pos) {
        IChunk chunk = getChunkAtPosition(pos);
        if (chunk == null) {
            return 1;
        }
        return chunk.getLightLevel(pos.x, pos.y, pos.z);
    }

    @Override
    public void cleanup() {
        for (IChunk c : chunkList) {
            c.cleanup();
        }
        modelBatch.dispose();
        modelBatch = null;
    }

    @Override
    public void rerenderChunks() {
        for(IChunk c : chunkList) {
            if(VoxelGameClient.getInstance().getPlayer().getPosition().planeDistance(c.getStartPosition().x, c.getStartPosition().z) <=
                    VoxelGameClient.getInstance().getSettingsManager().getVisualSettings().getViewDistance()*CHUNK_SIZE) {
                rerenderChunk(c);
            }
        }
    }

    public int getNumChunksMeshing() {
        return chunksMeshing;
    }

    public void incrChunksMeshing() {
        chunksMeshing++;
    }

    public void decrChunksMeshing() {
        chunksMeshing--;
    }

    @Override
    public void rmChunk(IChunk chunk) {
        if(chunk == null)return;
        removeChunkFromMap(chunk.getStartPosition());
        this.chunkList.remove(chunk);
    }

}
