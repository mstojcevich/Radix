package sx.lambda.voxel.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntMap;
import io.netty.util.internal.ConcurrentSet;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.api.RadixAPI;
import sx.lambda.voxel.api.events.worldgen.EventFinishChunkGen;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.block.Side;
import sx.lambda.voxel.entity.Entity;
import sx.lambda.voxel.entity.EntityPosition;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.chunk.BlockStorage.CoordinatesOutOfBoundsException;
import sx.lambda.voxel.world.chunk.Chunk;
import sx.lambda.voxel.world.chunk.IChunk;
import sx.lambda.voxel.world.chunk.MeshQueueWorker;
import sx.lambda.voxel.world.generation.ChunkGenerator;
import sx.lambda.voxel.world.generation.SimplexChunkGenerator;

import java.util.*;
import java.util.concurrent.*;

public class World implements IWorld {

    private static final int CHUNK_SIZE = 16; // SHOULD ALWAYS BE 2^x
    private static final int WORLD_HEIGHT = 256;
    private static final int SEA_LEVEL = 64;
    private static final float GRAVITY = 32f;
    private static final float TERMINAL_VELOCITY = 78.4f;

    /**
     * The amount of lighting workers to have per light type (sunlight and blocklight)
     */
    private static final int LIGHTING_WORKERS = 2;

    private final IntMap<IChunk> chunkMap = new IntMap<>();
    private final Set<IChunk> chunkList = new ConcurrentSet<>();
    private List<IChunk> sortedChunkList;
    private final Set<IChunk> chunksToRerender = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private IChunk lastPlayerChunk;

    private final boolean remote, server;
    private final ChunkGenerator chunkGen;
    private final List<Entity> loadedEntities = new CopyOnWriteArrayList<>();

    // Light-related stuff
    private final ExecutorService sunlightPoolExecutor = Executors.newFixedThreadPool(LIGHTING_WORKERS);
    private final ExecutorService blocklightPoolExecutor = Executors.newFixedThreadPool(LIGHTING_WORKERS);
    private final Queue<int[]> sunlightQueue = new LinkedBlockingQueue<>();
    private final Queue<int[]> sunlightRemovalQueue = new ConcurrentLinkedQueue<>();
    private final Queue<int[]> blocklightQueue = new LinkedBlockingQueue<>();

    // Mesh related stuff
    // Manages meshing chunks off of the main thread
    private final Queue<Runnable> chunkMeshQueue = new LinkedBlockingQueue<>();
    // Manages uploading chunks on the gl thread
    private final Queue<Runnable> chunkUploadQueue = new LinkedList<>();

    // Skybox stuff
    private ModelBatch modelBatch;
    private ModelBatch wiremeshBatch;
    private ModelInstance skybox;
    private Model skyboxModel;
    private Texture skyboxTexture;

    public World(boolean remote, boolean server) {
        this.remote = remote;
        this.server = server;
        if (!remote) {
            this.chunkGen = new SimplexChunkGenerator(this, 200, new Random().nextInt());
        } else {
            this.chunkGen = null;
        }

        for(int i = 0; i < LIGHTING_WORKERS; i++) {
            sunlightPoolExecutor.submit(new SunlightQueueWorker(this, sunlightQueue));
        }
        for(int i = 0; i < LIGHTING_WORKERS; i++) {
            blocklightPoolExecutor.submit(new BlocklightQueueWorker(this, blocklightQueue));
        }

        new MeshQueueWorker(chunkMeshQueue).start();
    }

    public int getChunkSize() {
        return CHUNK_SIZE;
    }

    public int getHeight() {
        return WORLD_HEIGHT;
    }

    public IChunk getChunk(Vec3i position) {
        return getChunk(position.x, position.z);
    }

    private int getChunkKey(int x, int z) {
        short nx = (short)Math.floorDiv(x, getChunkSize());
        short nz = (short)Math.floorDiv(z, getChunkSize());

        return (nx << 16) | (nz & 0xFFFF);
    }

    @Override
    public IChunk getChunk(int x, int z) {
        try {
            return chunkMap.get(getChunkKey(x, z));
        } catch(ArrayIndexOutOfBoundsException ex) {
            return null;
        }
    }

    private void removeChunkFromMap(Vec3i pos) {
        removeChunkFromMap(pos.x, pos.z);
    }

    private void removeChunkFromMap(int x, int z) {
        chunkMap.remove(getChunkKey(x, z));
    }

    @Override
    public void render() {
        assert !server;

        if (modelBatch == null) {
            modelBatch = new ModelBatch(Gdx.files.internal("shaders/gdx/world.vert.glsl"), Gdx.files.internal("shaders/gdx/world.frag.glsl"));
            wiremeshBatch = new ModelBatch(Gdx.files.internal("shaders/gdx/world.vert.glsl"), Gdx.files.internal("shaders/gdx/wiremesh.frag.glsl"));
        }
        if(skybox == null) {
            skybox = createSkybox();
        }

        processChunkUploadQueue();

        processLightQueue(); // If a chunk is doing its rerender, we want it to have the most recent lighting possible
        for (IChunk c : chunksToRerender) {
            if(RadixClient.getInstance().getPlayer().getPosition().planeDistance(c.getStartPosition().x, c.getStartPosition().z) <=
                    RadixClient.getInstance().getSettingsManager().getVisualSettings().getViewDistance()*CHUNK_SIZE) {
                c.rerender();
                chunksToRerender.remove(c);
            }
        }

        final IChunk playerChunk = getChunk(
                MathUtils.floor(RadixClient.getInstance().getPlayer().getPosition().getX()),
                MathUtils.floor(RadixClient.getInstance().getPlayer().getPosition().getZ()));
        if(playerChunk != null && (playerChunk != lastPlayerChunk || (sortedChunkList != null && chunkList.size() != sortedChunkList.size()))) {
            sortedChunkList = new ArrayList<>();
            for(IChunk c : chunkList) {
                if (RadixClient.getInstance().getPlayer().getPosition().planeDistance(c.getStartPosition().x, c.getStartPosition().z) <=
                        RadixClient.getInstance().getSettingsManager().getVisualSettings().getViewDistance() * CHUNK_SIZE) {
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

        float playerX = RadixClient.getInstance().getPlayer().getPosition().getX(),
                playerY = RadixClient.getInstance().getPlayer().getPosition().getY(),
                playerZ = RadixClient.getInstance().getPlayer().getPosition().getZ();
        boolean wireframe = RadixClient.getInstance().isWireframe();
        if(wireframe) {
            RadixClient.getInstance().setWireframe(false);
        }
        modelBatch.begin(RadixClient.getInstance().getCamera());
        skybox.transform.translate(playerX, playerY, playerZ);
        modelBatch.render(skybox);
        skybox.transform.translate(-playerX, -playerY, -playerZ);
        if(sortedChunkList != null) {
            boolean[] visibleChunks = new boolean[sortedChunkList.size()];
            int chunkNum = 0;
            for (IChunk c : sortedChunkList) {
                int x = c.getStartPosition().x;
                int z = c.getStartPosition().z;
                int halfWidth = getChunkSize()/2;
                int midX = x + halfWidth;
                int midZ = z + halfWidth;
                int midY = c.getHighestPoint()/2;
                boolean visible = RadixClient.getInstance().getGameRenderer().getFrustum().boundsInFrustum(midX, midY, midZ, halfWidth, midY, halfWidth);
                if(visible) {
                    visibleChunks[chunkNum] = true;
                    c.render(modelBatch);
                }
                chunkNum++;
            }

            chunkNum = 0;
            for (IChunk c : sortedChunkList) {
                boolean visible = visibleChunks[chunkNum];
                if(visible) {
                    c.renderTranslucent(modelBatch);
                }
                chunkNum++;
            }
        }
        modelBatch.end();
        if(wireframe && sortedChunkList != null) {
            RadixClient.getInstance().setWireframe(true);
            Gdx.gl.glLineWidth(2);
            wiremeshBatch.begin(RadixClient.getInstance().getCamera());
            for (IChunk c : sortedChunkList) {
                int x = c.getStartPosition().x;
                int z = c.getStartPosition().z;
                int halfWidth = getChunkSize()/2;
                int midX = x + halfWidth;
                int midZ = z + halfWidth;
                int midY = c.getHighestPoint()/2;
                boolean visible = RadixClient.getInstance().getGameRenderer().getFrustum().boundsInFrustum(midX, midY, midZ, halfWidth, midY, halfWidth);
                if(visible) {
                    c.render(wiremeshBatch);
                }
            }
            wiremeshBatch.end();
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
        int subtraction = MathUtils.floor(value) & (CHUNK_SIZE-1);
        return MathUtils.floor(value - subtraction);
    }

    @Override
    public float getGravity() {
        return GRAVITY;
    }

    @Override
    public float applyGravity(float velocity, long ms) {
        return Math.max(-TERMINAL_VELOCITY, velocity - getGravity() * (ms / 1000f));
    }

    @Override
    public void removeBlock(int x, int y, int z) {
        final IChunk c = this.getChunk(x, z);
        if (c != null) {
            x &= getChunkSize()-1;
            y &= getChunkSize()-1;
            try {
                c.removeBlock(x, y, z);
                c.setMeta((short) 0, x, y, z);
            } catch (CoordinatesOutOfBoundsException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void addBlock(int block, int x, int y, int z) {
        final IChunk c = this.getChunk(x, z);
        if(c != null) {
            try {
                c.setBlock(block, x & (getChunkSize() - 1), y, z * (getChunkSize() - 1));
            } catch (CoordinatesOutOfBoundsException e) {
                e.printStackTrace();
            }
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
        IChunk c = getChunk(chunk.getStartPosition());
        if (c != null) {
            removeChunkFromMap(chunk.getStartPosition());
            this.chunkList.remove(c);
        }
        addChunk(chunk, chunk.getStartPosition().x, chunk.getStartPosition().z);

        // Rerender neighbors
        for(int x = chunk.getStartPosition().x - getChunkSize(); x <= chunk.getStartPosition().x + getChunkSize(); x += getChunkSize()) {
            for (int z = chunk.getStartPosition().z - getChunkSize(); z <= chunk.getStartPosition().z + getChunkSize(); z += getChunkSize()) {
                if(x == chunk.getStartPosition().x && z == chunk.getStartPosition().z)
                    continue;
                IChunk neighbor = getChunk(x, z);
                if(neighbor != null) {
                    rerenderChunk(neighbor);
                }
            }
        }
    }

    @Override
    public List<Entity> getLoadedEntities() {
        return this.loadedEntities;
    }

    private void addChunk(IChunk chunk, int x, int z) {
        this.chunkMap.put(getChunkKey(x, z), chunk);
        this.chunkList.add(chunk);
    }

    private IChunk loadChunk(int startX, int startZ) {
        IChunk foundChunk = getChunk(startX, startZ);
        if (foundChunk == null && !remote) {
            final IChunk c = new Chunk(this, new Vec3i(startX, 0, startZ), RadixAPI.instance.getBiomeByID(0), true);
            RadixAPI.instance.getEventManager().push(new EventFinishChunkGen(c));
            addChunk(c, startX, startZ);
            return c;
        } else {
            return foundChunk;
        }
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

    @Override
    public void addToSunlightQueue(int x, int y, int z) {
        sunlightQueue.add(new int[]{x, y, z});
        synchronized(sunlightQueue) {
            sunlightQueue.notify();
        }
    }

    @Override
    public void addToBlocklightQueue(int x, int y, int z) {
        blocklightQueue.add(new int[]{x, y, z});
        synchronized(blocklightQueue) {
            blocklightQueue.notify();
        }
    }

    @Override
    public void addToSunlightRemovalQueue(int x, int y, int z) {
        sunlightRemovalQueue.add(new int[]{x, y, z});
    }

    @Override
    public void processLightQueue() {
        // If the chunk is not lighted and it is in range, setup lighting then set as lighted
        chunkList.stream().filter(c -> !c.hasInitialSun())
                .filter(c -> RadixClient.getInstance().getPlayer().getPosition().planeDistance(
                        c.getStartPosition().x, c.getStartPosition().z)
                        <= RadixClient.getInstance().getSettingsManager().getVisualSettings().getViewDistance() * CHUNK_SIZE)
                .forEach(c -> {
            setupLighting(c);
            c.finishAddingSun();
        });
        if (sunlightQueue.isEmpty() && sunlightRemovalQueue.isEmpty()) {
            chunkList.stream().filter(IChunk::waitingOnLightFinish).forEach(IChunk::finishChangingSunlight);
            return;
        }
        processLightRemovalQueue();
    }

    private void processLightRemovalQueue() {
        if (!sunlightRemovalQueue.isEmpty()) {
            Side[] sides = Side.values();

            List<IChunk> changedChunks = new ArrayList<>();
            int[] pos;
            while ((pos = sunlightRemovalQueue.poll()) != null) {
                int x = pos[0];
                int cx = x & (CHUNK_SIZE-1);
                int y = pos[1];
                int z = pos[2];
                int cz = z & (CHUNK_SIZE-1);
                IChunk posChunk = getChunk(x, z);
                if (posChunk == null) {
                    continue;
                }
                int ll = 0;
                try {
                    ll = posChunk.getSunlight(x & (CHUNK_SIZE-1), y, z & (CHUNK_SIZE-1));

                    // Spread off to each side
                    for(Side s : sides) {
                        int sx = x; // Side x coord
                        int sy = y; // Side y coord
                        int sz = z; // Side z coord
                        int scx = cx; // Chunk-relative side x coord
                        int scz = cz; // Chunk-relative side z coord
                        IChunk sChunk = posChunk;

                        // Offset values based on side
                        switch (s) {
                            case TOP:
                                sy += 1;
                                break;
                            case BOTTOM:
                                sy -= 1;
                                break;
                            case WEST:
                                sx -= 1;
                                scx -= 1;
                                break;
                            case EAST:
                                sx += 1;
                                scx += 1;
                                break;
                            case NORTH:
                                sz += 1;
                                scz += 1;
                                break;
                            case SOUTH:
                                sz -= 1;
                                scz -= 1;
                                break;
                        }
                        if (sy < 0)
                            continue;
                        if (sy > WORLD_HEIGHT - 1)
                            continue;

                        // Select the correct chunk
                        if(scz < 0) {
                            scz += CHUNK_SIZE;
                            sChunk = getChunk(sx, sz);
                        } else if(scz > CHUNK_SIZE-1) {
                            scz -= CHUNK_SIZE;
                            sChunk = getChunk(sx, sz);
                        }
                        if(scx < 0) {
                            scx += CHUNK_SIZE;
                            sChunk = getChunk(sx, sz);
                        } else if(scx > CHUNK_SIZE-1) {
                            scx -= CHUNK_SIZE;
                            sChunk = getChunk(sx, sz);
                        }

                        if(sChunk == null)
                            continue;

                        Block sBlock = sChunk.getBlock(scx, sy, scz);
                        int sSunlight = sChunk.getSunlight(scx, sy, scz);
                        if (sBlock == null || sBlock.doesLightPassThrough() || !sBlock.decreasesLight()) {
                            if ((sSunlight < ll || s == Side.BOTTOM) && sSunlight != 0) { // Reset lighting for blocks affected by this block
                                addToSunlightRemovalQueue(sx, sy, sz);
                            } else if (sSunlight >= ll && s != Side.BOTTOM) { // Add surrounding blocks to queue to restore lighting of the reset blocks
                                addToSunlightQueue(sx, sy, sz);
                            }
                        }
                    }

                    // Reset lighting for the block
                    posChunk.setSunlight(cx, y, cz, 0);
                    changedChunks.add(posChunk);
                } catch (CoordinatesOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            for(IChunk changedChunk : changedChunks) {
                changedChunk.finishChangingSunlight();
            }
        }
    }

    @Override
    public void cleanup() {
        chunkList.forEach(IChunk::dispose);
        modelBatch.dispose();
        modelBatch = null;
        skyboxTexture.dispose();
        skyboxTexture = null;
        skyboxModel.dispose();
        skyboxModel = null;
    }

    @Override
    public void rerenderChunks() {
        // If the chunk is in range, rerender it
        chunkList.stream().filter(c -> RadixClient.getInstance().getPlayer().getPosition().planeDistance(c.getStartPosition().x, c.getStartPosition().z) <=
                RadixClient.getInstance().getSettingsManager().getVisualSettings().getViewDistance() * CHUNK_SIZE).forEach(this::rerenderChunk);
    }

    @Override
    public void rmChunk(IChunk chunk) {
        if(chunk == null)return;
        removeChunkFromMap(chunk.getStartPosition());
        this.chunkList.remove(chunk);
    }

    @Override
    public void addToMeshQueue(Runnable updateFaces) {
        chunkMeshQueue.add(updateFaces);
        synchronized (chunkMeshQueue) {
            chunkMeshQueue.notify();
        }
    }

    @Override
    public void addToChunkUploadQueue(Runnable upload) {
        chunkUploadQueue.add(upload);
    }

    private ModelInstance createSkybox() {
        if(skyboxModel != null) {
            skyboxModel.dispose();
        }
        if(skyboxTexture == null) {
            skyboxTexture = new Texture(Gdx.files.internal("textures/world/skybox.png"));
        }

        MeshBuilder builder = new MeshBuilder();
        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);

        int x1 = -256;
        int y1 = -256;
        int z1 = -256;
        int x2 = 256;
        int y2 = 256;
        int z2 = 256;
        float sideU1 = 0;
        float sideU2 = 1/3f;
        float sideV1 = 0;
        float sideV2 = 1;
        float topU1 = sideU2;
        float topU2 = topU1+sideU2;
        float bottomU1 = topU2+sideU2;
        float bottomU2 = bottomU1+sideU2;
        float topV1 = 0;
        float topV2 = 1;
        float bottomV1 = 0;
        float bottomV2 = 1;
        MeshPartBuilder.VertexInfo bottomLeftBack = new MeshPartBuilder.VertexInfo().setPos(x1, y1, z1).setUV(sideU1, sideV2);
        MeshPartBuilder.VertexInfo bottomRightBack = new MeshPartBuilder.VertexInfo().setPos(x2, y1, z1).setUV(sideU2, sideV2);
        MeshPartBuilder.VertexInfo bottomRightFront = new MeshPartBuilder.VertexInfo().setPos(x2, y1, z2).setUV(sideU1, sideV2);
        MeshPartBuilder.VertexInfo bottomLeftFront = new MeshPartBuilder.VertexInfo().setPos(x1, y1, z2).setUV(sideU2, sideV2);
        MeshPartBuilder.VertexInfo topLeftBack = new MeshPartBuilder.VertexInfo().setPos(x1, y2, z1).setUV(sideU1, sideV1);
        MeshPartBuilder.VertexInfo topRightBack = new MeshPartBuilder.VertexInfo().setPos(x2, y2, z1).setUV(sideU2, sideV1);
        MeshPartBuilder.VertexInfo topRightFront = new MeshPartBuilder.VertexInfo().setPos(x2, y2, z2).setUV(sideU1, sideV1);
        MeshPartBuilder.VertexInfo topLeftFront = new MeshPartBuilder.VertexInfo().setPos(x1, y2, z2).setUV(sideU2, sideV1);
        // Negative Z
        builder.rect(bottomLeftBack, bottomRightBack, topRightBack, topLeftBack);
        // Positive Z
        builder.rect(topLeftFront, topRightFront, bottomRightFront, bottomLeftFront);
        // Negative X
        builder.rect(bottomLeftBack, topLeftBack, topLeftFront, bottomLeftFront);
        // Positive X
        builder.rect(bottomRightFront, topRightFront, topRightBack, bottomRightBack);
        // Positive Y
        builder.rect(topLeftBack.setUV(topU1, topV1),
                topRightBack.setUV(topU2, topV1),
                topRightFront.setUV(topU2, topV2),
                topLeftFront.setUV(topU1, topV2));
        // Negative Y
        builder.rect(bottomLeftFront.setUV(bottomU1, bottomV2),
                bottomRightFront.setUV(bottomU2, bottomV2),
                bottomRightBack.setUV(bottomU2, bottomV1),
                bottomLeftBack.setUV(bottomU1, bottomV1));
        Mesh skybox = builder.end();

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        modelBuilder.part("skybox", skybox, GL20.GL_TRIANGLES, new Material(
                TextureAttribute.createDiffuse(skyboxTexture)));
        return new ModelInstance(skyboxModel = modelBuilder.end());
    }

    private void setupLighting(IChunk c) {
        try {
            for (int x = 0; x < CHUNK_SIZE; x++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    int y = WORLD_HEIGHT - 1;
                    if (c.getBlockId(x, y, z) > 0) {
                        break;
                    }
                    sunlightQueue.add(new int[]{c.getStartPosition().x + x, y, c.getStartPosition().z + z});
                    c.setSunlight(x, y, z, c.getMaxLightLevel());

                    for (int cy = 0; cy < getHeight(); cy++) {
                        int id = c.getBlockId(x, cy, z);
                        if (id > 0) {
                            Block blk = RadixAPI.instance.getBlock(id);
                            if (blk.getLightValue() > 0) {
                                c.setBlocklight(x, cy, z, blk.getLightValue());
                                addToBlocklightQueue(c.getStartPosition().x + x, c.getStartPosition().y + cy, c.getStartPosition().z + z);
                            }
                        }
                    }
                }
            }
            synchronized (sunlightQueue) {
                sunlightQueue.notifyAll();
            }
            synchronized (blocklightQueue) {
                blocklightQueue.notifyAll();
            }
        } catch (CoordinatesOutOfBoundsException ex) {
            ex.printStackTrace();
        }
    }

    private void processChunkUploadQueue() {
        while(!chunkUploadQueue.isEmpty()) {
            chunkUploadQueue.poll().run();
            if(RadixClient.getInstance().getSettingsManager().getVisualSettings().getSmoothChunkLoad().getValue())
                break; // distribute chunk uploads across frames (one chunkload per frame)
        }
    }

}
