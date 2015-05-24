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
import sx.lambda.voxel.net.packet.client.PacketUnloadChunk;
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

    private static final float GRAVITY = 4.69f;

    private static final float TERMINAL_VELOCITY = 56;

    private final ChunkGenerator chunkGen;

    private final boolean remote, server;

    private List<Entity> loadedEntities = new CopyOnWriteArrayList<>();

    private Set<IChunk> chunksToRerender = Collections.newSetFromMap(new ConcurrentHashMap<IChunk, Boolean>());

    private Queue<Vec3i> sunlightQueue = new ConcurrentLinkedQueue<>();
    private Queue<Vec3i> sunlightRemovalQueue = new ConcurrentLinkedQueue<>();

    private ModelBatch modelBatch;

    private boolean shouldUpdateLight, updatingLight;

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

        IntMap<IChunk> zMap = this.chunkMapX.get(x);
        if(zMap == null)
            return null;

        return zMap.get(z);
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

        if (!updatingLight && (sunlightQueue.size() > 0 || sunlightRemovalQueue.size() > 0 || shouldUpdateLight)) {
            processLightQueue(); // If a chunk is doing its rerender, we want it to have the most recent lighting possible
        }
        for (IChunk c : chunksToRerender) {
            c.rerender();
            chunksToRerender.remove(c);
        }

        long renderStartNS = System.nanoTime();

        modelBatch.begin(VoxelGameClient.getInstance().getCamera());
        for (IChunk c : this.chunkList) {
            c.render(modelBatch);
        }
        for (IChunk c : this.chunkList) {
            c.renderWater(modelBatch);
        }
        modelBatch.end();
        if (VoxelGameClient.getInstance().numChunkRenders == 100) {  // Reset every 100 renders
            VoxelGameClient.getInstance().numChunkRenders = 0;
            VoxelGameClient.getInstance().chunkRenderTimes = 0;
        }
        VoxelGameClient.getInstance().chunkRenderTimes += (int) (System.nanoTime() - renderStartNS);
        VoxelGameClient.getInstance().numChunkRenders++;
    }

    @Override
    public void loadChunks(EntityPosition playerPosition, int viewDistance) {
        if (!remote) { //don't gen chunks if we're not local
            this.getChunksInRange(playerPosition, viewDistance);
        }

        gcChunks(playerPosition, viewDistance);
    }

    @Override
    public int getSeaLevel() {
        return SEA_LEVEL;
    }

    @Override
    public int getChunkPosition(float value) {
        // TODO changed (int) to MathUtils.floor, might cause issues. If it doesn't, remove this TODO.
        int subtraction = MathUtils.floor(value % CHUNK_SIZE);
        if (value < 0 && subtraction != 0) {
            subtraction = CHUNK_SIZE + subtraction;
        }
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
                    if (Math.abs(x + (x < 0 ? 1 : 0)) % 16 == 15) {
                        if (x < 0) {
                            rerenderChunk(getChunkAtPosition(x - 1, z));
                        } else {
                            rerenderChunk(getChunkAtPosition(x + 1, z));
                        }
                    } else if (Math.abs(x + (x < 0 ? 1 : 0)) % 16 == 0) {
                        if (x < 0) {
                            rerenderChunk(getChunkAtPosition(x + 1, z));
                        } else {
                            rerenderChunk(getChunkAtPosition(x - 1, z));
                        }
                    }

                    if (Math.abs(z + (z < 0 ? 1 : 0)) % 16 == 15) {
                        if (z < 0) {
                            rerenderChunk(getChunkAtPosition(x, z - 1));
                        } else {
                            rerenderChunk(getChunkAtPosition(x, z + 1));
                        }
                    } else if (Math.abs(z + (z < 0 ? 1 : 0)) % 16 == 0) {
                        if (z < 0) {
                            rerenderChunk(getChunkAtPosition(x, z + 1));
                        } else {
                            rerenderChunk(getChunkAtPosition(x, z - 1));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void addBlock(int block, int x, int y, int z) {
        synchronized (this) {
            final IChunk c = this.getChunkAtPosition(x, z);
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
    public void gcChunks(EntityPosition playerPosition, int viewDistance) {
        int range = viewDistance * CHUNK_SIZE;

        int playerChunkX = getChunkPosition(playerPosition.getX());
        int playerChunkZ = getChunkPosition(playerPosition.getZ());

        for (final IChunk chunk : chunkList) {
            int x = chunk.getStartPosition().x;
            int z = chunk.getStartPosition().z;
            if (Math.abs(x - playerChunkX) > range
                    || Math.abs(z - playerChunkZ) > range) {
                this.chunkList.remove(chunk);
                VoxelGameClient.getInstance().addToGLQueue(new Runnable() {
                    @Override
                    public void run() {
                        chunk.cleanup();
                    }
                });
                chunkMapX.get(x).remove(z);
            }
        }
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
            final IChunk c = new Chunk(this, new Vec3i(startX, 0, startZ));
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
                addToSunlightQueue(new Vec3i(c.getStartPosition().x + x, WORLD_HEIGHT - 1, c.getStartPosition().z + z));
            }
        }
        c.finishChangingSunlight();
    }

    public void addEntity(Entity e) {
        loadedEntities.add(e);
    }

    @Override
    public void rerenderChunk(IChunk c) {
        chunksToRerender.add(c);
    }

    @Override
    public ChunkGenerator getChunkGen() {
        return this.chunkGen;
    }

    /**
     * Add a block to a list of blocks to process sunlight for
     * The block at the position passed should be transparent or null and have a sunlight level greater than 0
     */
    @Override
    public void addToSunlightQueue(Vec3i block) {
        sunlightQueue.add(block);
    }

    @Override
    public void addToSunlightRemovalQueue(Vec3i block) {
        sunlightRemovalQueue.add(block);
    }

    @Override
    public void processLightQueue() {
        shouldUpdateLight = true;
        if (!updatingLight) {
            new Thread("Light update") {
                @Override
                public void run() {
                    shouldUpdateLight = false;
                    updatingLight = true;

                    processLightRemovalQueue();

                    if (!sunlightQueue.isEmpty()) {
                        Queue<IChunk> changedChunks = new LinkedBlockingDeque<>();
                        Vec3i pos;
                        while ((pos = sunlightQueue.poll()) != null) {
                            IChunk posChunk = getChunkAtPosition(pos);
                            if (posChunk == null) {
                                continue;
                            }
                            int ll = posChunk.getSunlight(pos.x, pos.y, pos.z);
                            int nextLL = ll - 1;

                            int negX = pos.x - 1;
                            int posX = pos.x + 1;
                            int negZ = pos.z - 1;
                            int posZ = pos.z + 1;
                            IChunk negXNeighborChunk = getChunkAtPosition(negX, pos.z);
                            IChunk posXNeighborChunk = getChunkAtPosition(posX, pos.z);
                            IChunk negZNeighborChunk = getChunkAtPosition(pos.x, negZ);
                            IChunk posZNeighborChunk = getChunkAtPosition(pos.x, posZ);

                            if (negXNeighborChunk != null) {
                                Block bl = negXNeighborChunk.getBlockAtPosition(negX, pos.y, pos.z);
                                if (bl == null) {
                                    if (negXNeighborChunk.getSunlight(negX, pos.y, pos.z) < nextLL) {
                                        negXNeighborChunk.setSunlight(negX, pos.y, pos.z, nextLL);
                                        sunlightQueue.add(new Vec3i(negX, pos.y, pos.z));
                                        changedChunks.add(negXNeighborChunk);
                                    }
                                } else if (bl.isTransparent()) {
                                    if (negXNeighborChunk.getSunlight(negX, pos.y, pos.z) < nextLL) {
                                        negXNeighborChunk.setSunlight(negX, pos.y, pos.z, nextLL);
                                        sunlightQueue.add(new Vec3i(negX, pos.y, pos.z));
                                        changedChunks.add(negXNeighborChunk);
                                    }
                                }
                            }
                            if (posXNeighborChunk != null) {
                                Block bl = posXNeighborChunk.getBlockAtPosition(posX, pos.y, pos.z);
                                if (bl == null) {
                                    if (posXNeighborChunk.getSunlight(posX, pos.y, pos.z) < nextLL) {
                                        posXNeighborChunk.setSunlight(posX, pos.y, pos.z, nextLL);
                                        sunlightQueue.add(new Vec3i(posX, pos.y, pos.z));
                                        changedChunks.add(posXNeighborChunk);
                                    }
                                } else if (bl.isTransparent()) {
                                    if (posXNeighborChunk.getSunlight(posX, pos.y, pos.z) < nextLL) {
                                        posXNeighborChunk.setSunlight(posX, pos.y, pos.z, nextLL);
                                        sunlightQueue.add(new Vec3i(posX, pos.y, pos.z));
                                        changedChunks.add(posXNeighborChunk);
                                    }
                                }
                            }
                            if (negZNeighborChunk != null) {
                                Block bl = negZNeighborChunk.getBlockAtPosition(pos.x, pos.y, negZ);
                                if (bl == null) {
                                    if (negZNeighborChunk.getSunlight(pos.x, pos.y, negZ) < nextLL) {
                                        negZNeighborChunk.setSunlight(pos.x, pos.y, negZ, nextLL);
                                        sunlightQueue.add(new Vec3i(pos.x, pos.y, negZ));
                                        changedChunks.add(negZNeighborChunk);
                                    }
                                } else if (bl.isTransparent()) {
                                    if (negZNeighborChunk.getSunlight(pos.x, pos.y, negZ) < nextLL) {
                                        negZNeighborChunk.setSunlight(pos.x, pos.y, negZ, nextLL);
                                        sunlightQueue.add(new Vec3i(pos.x, pos.y, negZ));
                                        changedChunks.add(negZNeighborChunk);
                                    }
                                }
                            }
                            if (posZNeighborChunk != null) {
                                Block bl = posZNeighborChunk.getBlockAtPosition(pos.x, pos.y, posZ);
                                if (bl == null) {
                                    if (posZNeighborChunk.getSunlight(pos.x, pos.y, posZ) < nextLL) {
                                        posZNeighborChunk.setSunlight(pos.x, pos.y, posZ, nextLL);
                                        sunlightQueue.add(new Vec3i(pos.x, pos.y, posZ));
                                        changedChunks.add(posZNeighborChunk);
                                    }
                                } else if (bl.isTransparent()) {
                                    if (posZNeighborChunk.getSunlight(pos.x, pos.y, posZ) < nextLL) {
                                        posZNeighborChunk.setSunlight(pos.x, pos.y, posZ, nextLL);
                                        sunlightQueue.add(new Vec3i(pos.x, pos.y, posZ));
                                        changedChunks.add(posZNeighborChunk);
                                    }
                                }
                            }

                            if (pos.y < WORLD_HEIGHT - 2) {
                                int posY = pos.y + 1;
                                Block posYBlock = posChunk.getBlockAtPosition(pos.x, posY, pos.z);
                                if (posYBlock == null) {
                                    if (nextLL > posChunk.getSunlight(pos.x, posY, pos.z)) {
                                        posChunk.setSunlight(pos.x, posY, pos.z, nextLL);
                                        sunlightQueue.add(new Vec3i(pos.x, posY, pos.z));
                                        changedChunks.add(posChunk);
                                    }
                                } else if (posYBlock.isTransparent()) {
                                    if (nextLL > posChunk.getSunlight(pos.x, posY, pos.z)) {
                                        posChunk.setSunlight(pos.x, posY, pos.z, nextLL);
                                        sunlightQueue.add(new Vec3i(pos.x, posY, pos.z));
                                        changedChunks.add(posChunk);
                                    }
                                }
                            }

                            if (pos.y > 0) {
                                int negY = pos.y - 1;
                                Block negYBlock = posChunk.getBlockAtPosition(pos.x, negY, pos.z);
                                if (negYBlock == null) {
                                    if (ll == 16) {
                                        if (posChunk.getSunlight(pos.x, negY, pos.z) < 16) {
                                            posChunk.setSunlight(pos.x, negY, pos.z, 16);
                                            sunlightQueue.add(new Vec3i(pos.x, negY, pos.z));
                                            changedChunks.add(posChunk);
                                        }
                                    } else {
                                        if (posChunk.getSunlight(pos.x, negY, pos.z) < nextLL) {
                                            posChunk.setSunlight(pos.x, negY, pos.z, 16);
                                            sunlightQueue.add(new Vec3i(pos.x, negY, pos.z));
                                            changedChunks.add(posChunk);
                                        }
                                    }
                                } else if (negYBlock.isTransparent()) {
                                    if (ll == 16) {
                                        if (posChunk.getSunlight(pos.x, negY, pos.z) < 16) {
                                            posChunk.setSunlight(pos.x, negY, pos.z, 16);
                                            sunlightQueue.add(new Vec3i(pos.x, negY, pos.z));
                                            changedChunks.add(posChunk);
                                        }
                                    } else {
                                        if (posChunk.getSunlight(pos.x, negY, pos.z) < nextLL) {
                                            posChunk.setSunlight(pos.x, negY, pos.z, 16);
                                            sunlightQueue.add(new Vec3i(pos.x, negY, pos.z));
                                            changedChunks.add(posChunk);
                                        }
                                    }
                                }
                            }
                        }
                        IChunk changedChunk;
                        while ((changedChunk = changedChunks.poll()) != null) {
                            changedChunk.finishChangingSunlight();
                        }
                    }
                    updatingLight = false;
                }
            }.start();
        }
    }

    private void processLightRemovalQueue() {
        if (!sunlightRemovalQueue.isEmpty()) {
            Queue<IChunk> changedChunks = new LinkedBlockingDeque<>();
            Vec3i pos;
            while ((pos = sunlightRemovalQueue.poll()) != null) {
                IChunk posChunk = getChunkAtPosition(pos);
                if (posChunk == null) {
                    continue;
                }
                int ll = posChunk.getSunlight(pos.x, pos.y, pos.z);

                Vec3i negXNeighborPos = pos.translate(-1, 0, 0);
                Vec3i posXNeighborPos = pos.translate(1, 0, 0);
                Vec3i negZNeighborPos = pos.translate(0, 0, -1);
                Vec3i posZNeighborPos = pos.translate(0, 0, 1);
                IChunk negXNeighborChunk = getChunkAtPosition(negXNeighborPos);
                IChunk posXNeighborChunk = getChunkAtPosition(posXNeighborPos);
                IChunk negZNeighborChunk = getChunkAtPosition(negZNeighborPos);
                IChunk posZNeighborChunk = getChunkAtPosition(posZNeighborPos);

                if (negXNeighborChunk != null) {
                    Block bl = negXNeighborChunk.getBlockAtPosition(negXNeighborPos);
                    int bll = negXNeighborChunk.getSunlight(negXNeighborPos.x, negXNeighborPos.y, negXNeighborPos.z);
                    if (bll < ll && bll != 0) {
                        if (bl == null) {
                            sunlightRemovalQueue.add(negXNeighborPos);
                        } else if (bl.isTransparent()) {
                            sunlightRemovalQueue.add(negXNeighborPos);
                        }
                    } else if (bll >= ll) {
                        if (bl == null) {
                            sunlightQueue.add(negXNeighborPos);
                        } else if (bl.isTransparent()) {
                            sunlightQueue.add(negXNeighborPos);
                        }
                    }
                }
                if (posXNeighborChunk != null) {
                    Block bl = posXNeighborChunk.getBlockAtPosition(posXNeighborPos);
                    int bll = posXNeighborChunk.getSunlight(posXNeighborPos.x, posXNeighborPos.y, posXNeighborPos.z);
                    if (bll < ll && bll != 0) {
                        if (bl == null) {
                            sunlightRemovalQueue.add(posXNeighborPos);
                        } else if (bl.isTransparent()) {
                            sunlightRemovalQueue.add(posXNeighborPos);
                        }
                    } else if (bll >= ll) {
                        if (bl == null) {
                            sunlightQueue.add(posXNeighborPos);
                        } else if (bl.isTransparent()) {
                            sunlightQueue.add(posXNeighborPos);
                        }
                    }
                }
                if (negZNeighborChunk != null) {
                    Block bl = negZNeighborChunk.getBlockAtPosition(negZNeighborPos);
                    int bll = negZNeighborChunk.getSunlight(negZNeighborPos.x, negZNeighborPos.y, negZNeighborPos.z);
                    if (bll < ll && bll != 0) {
                        if (bl == null) {
                            sunlightRemovalQueue.add(negZNeighborPos);
                        } else if (bl.isTransparent()) {
                            sunlightRemovalQueue.add(negZNeighborPos);
                        }
                    } else if (bll >= ll) {
                        if (bl == null) {
                            sunlightQueue.add(negZNeighborPos);
                        } else if (bl.isTransparent()) {
                            sunlightQueue.add(negZNeighborPos);
                        }
                    }
                }
                if (posZNeighborChunk != null) {
                    Block bl = posZNeighborChunk.getBlockAtPosition(posZNeighborPos);
                    int bll = posZNeighborChunk.getSunlight(posZNeighborPos.x, posZNeighborPos.y, posZNeighborPos.z);
                    if (bll < ll && bll != 0) {
                        if (bl == null) {
                            sunlightRemovalQueue.add(posZNeighborPos);
                        } else if (bl.isTransparent()) {
                            sunlightRemovalQueue.add(posZNeighborPos);
                        }
                    } else if (bll >= ll) {
                        if (bl == null) {
                            sunlightQueue.add(posZNeighborPos);
                        } else if (bl.isTransparent()) {
                            sunlightQueue.add(posZNeighborPos);
                        }
                    }
                }

                if (pos.y > 0) {
                    Vec3i negYPos = pos.translate(0, -1, 0);
                    Block negYBlock = posChunk.getBlockAtPosition(negYPos);
                    if (negYBlock == null) {
                        if (posChunk.getSunlight(negYPos.x, negYPos.y, negYPos.z) != 0) {
                            sunlightRemovalQueue.add(negYPos);
                        }
                    } else if (negYBlock.isTransparent()) {
                        if (posChunk.getSunlight(negYPos.x, negYPos.y, negYPos.z) != 0) {
                            sunlightRemovalQueue.add(negYPos);
                        }
                    }
                }

                posChunk.setSunlight(pos.x, pos.y, pos.z, 0);
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

    public int getNumChunksMeshing() {
        return chunksMeshing;
    }

    public void incrChunksMeshing() {
        chunksMeshing++;
    }

    public void decrChunksMeshing() {
        chunksMeshing--;
    }

}
