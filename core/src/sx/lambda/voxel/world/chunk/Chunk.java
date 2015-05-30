package sx.lambda.voxel.world.chunk;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.api.VoxelGameAPI;
import sx.lambda.voxel.api.events.render.EventChunkRender;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.block.NormalBlockRenderer;
import sx.lambda.voxel.client.render.meshing.GreedyMesher;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.IWorld;

import java.util.List;

public class Chunk implements IChunk {

    private final transient GreedyMesher mesher;
    private final int size;
    private final int height;
    /**
     * Map of light levels (ints 0-16) to brightness multipliers
     */
    private final float[] lightLevelMap = new float[17];
    private int[][][] blockList;
    private transient IWorld parentWorld;
    private transient MeshBuilder meshBuilder;
    private transient ModelBuilder modelBuilder;
    private transient Model opaqueModel, translucentModel;
    private transient ModelInstance opaqueModelInstance, translucentModelInstance;
    private final Vec3i startPosition;
    private int highestPoint;
    private final transient float[][][] lightLevels;
    private transient int[][][] sunlightLevels;
    private transient boolean sunlightChanging;
    private transient boolean sunlightChanged;
    private boolean setup;
    private boolean cleanedUp;

    private List<GreedyMesher.Face> translucentFaces;
    private List<GreedyMesher.Face> opaqueFaces;
    private boolean meshing, meshed, meshWhenDone;

    public Chunk(IWorld world, Vec3i startPosition, int[][][] ids) {
        this.parentWorld = world;
        this.startPosition = startPosition;
        this.size = world.getChunkSize();
        this.height = world.getHeight();

        for (int i = 0; i < 17; i++) {
            int reduction = 16 - i;
            lightLevelMap[i] = (float) Math.pow(0.8, reduction);
        }

        sunlightLevels = new int[size][height][size];

        if (VoxelGameClient.getInstance() != null) {// We're a client
            mesher = new GreedyMesher(this);
        } else {
            mesher = null;
        }


        this.loadIdInts(ids);

        lightLevels = new float[size][height][size];

        setupSunlighting();
    }

    public Chunk(IWorld world, Vec3i startPosition) {
        this.parentWorld = world;
        this.startPosition = startPosition;
        this.size = world.getChunkSize();
        this.height = world.getHeight();

        for (int i = 0; i < 17; i++) {
            int reduction = 16 - i;
            lightLevelMap[i] = (float) Math.pow(0.8, reduction);
        }

        sunlightLevels = new int[size][height][size];

        if (VoxelGameClient.getInstance() != null) {// We're a client
            mesher = new GreedyMesher(this);
        } else {
            mesher = null;
        }


        this.blockList = new int[size][height][size];
        highestPoint = world.getChunkGen().generate(startPosition, blockList);

        lightLevels = new float[size][height][size];

        setupSunlighting();
    }

    @Override
    public void rerender() {
        if (cleanedUp) return;

        if (this.parentWorld == null) {
            if (VoxelGameClient.getInstance() != null) {// We're a client
                this.parentWorld = VoxelGameClient.getInstance().getWorld();
            }
        }

        if (!setup) {
            meshBuilder = new MeshBuilder();
            modelBuilder = new ModelBuilder();

            setup = true;
        }

        sunlightChanged = false;

        if(meshing || parentWorld.getNumChunksMeshing() >= 2) {
            meshWhenDone = true;
        } else {
            meshing = true;
            new Thread("Chunk Meshing") {
                @Override
                public void run() {
                    updateFaces();
                }
            }.start();
        }
    }

    @Override
    public void render(ModelBatch batch) {
        if (cleanedUp) return;

        if(!meshing && meshed) {
            updateModelInstances();
            meshed = false;
        }

        if (sunlightChanged && !sunlightChanging || (!meshing && !meshed && meshWhenDone)) {
            meshWhenDone = false;
            rerender();
        }

        if(opaqueModelInstance != null) {
            batch.render(opaqueModelInstance);
            batch.render(translucentModelInstance);
        }
    }

    @Override
    public void renderTranslucent(ModelBatch batch) {

    }

    @Override
    public void eachBlock(EachBlockCallee callee) {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < size; z++) {
                    Block blk = VoxelGameAPI.instance.getBlockByID(blockList[x][y][z]);
                    callee.call(blk, x, y, z);
                }
            }
        }
    }

    public Block getBlockAtPosition(Vec3i position) {
        int x = position.x % size;
        int y = position.y;
        int z = position.z % size;
        if (x < 0) {
            x += size;
        }

        if (z < 0) {
            z += size;
        }


        if (y > height - 1) return null;
        if (y < 0) return null;

        return VoxelGameAPI.instance.getBlockByID(blockList[x][y][z]);
    }

    @Override
    public void removeBlock(int x, int y, int z) {
        x = x % size;
        z = z % size;
        if (x < 0) {
            x += size;
        }

        if (z < 0) {
            z += size;
        }

        if (y > height - 1) return;

        blockList[x][y][z] = -1;

        getWorld().rerenderChunk(this);

        this.addNeighborsToSunlightQueue(x, y, z);
    }

    private void addNeighborsToSunlightQueue(int x, int y, int z) {// X Y and Z are relative coords, not world coords
        x += startPosition.x;
        z += startPosition.z;
        int negX = x-1;
        int posX = x+1;
        int negZ = z-1;
        int posZ = z+1;
        int posY = y+1;
        IChunk negXNeighborChunk = parentWorld.getChunkAtPosition(negX, z);
        IChunk posXNeighborChunk = parentWorld.getChunkAtPosition(posX, z);
        IChunk negZNeighborChunk = parentWorld.getChunkAtPosition(x, negZ);
        IChunk posZNeighborChunk = parentWorld.getChunkAtPosition(x, posZ);

        if (negXNeighborChunk != null) {
            int negXSunlight = negXNeighborChunk.getSunlight(negX, y, z);
            if (negXSunlight > 1) {
                Block bl = negXNeighborChunk.getBlockAtPosition(negX, y, z);
                if (bl == null) {
                    parentWorld.addToSunlightQueue(new int[]{negX, y, z});
                } else if (bl.isTranslucent()) {
                    parentWorld.addToSunlightQueue(new int[]{negX, y, z});
                }
            }
        }

        if (posXNeighborChunk != null) {
            int posXSunlight = posXNeighborChunk.getSunlight(posX, y, z);
            if (posXSunlight > 1) {
                Block bl = posXNeighborChunk.getBlockAtPosition(posX, y, z);
                if (bl == null) {
                    parentWorld.addToSunlightQueue(new int[]{posX, y, z});
                } else if (bl.isTranslucent()) {
                    parentWorld.addToSunlightQueue(new int[]{posX, y, z});
                }
            }
        }

        if (negZNeighborChunk != null) {
            int negZSunlight = negZNeighborChunk.getSunlight(x, y, negZ);
            if (negZSunlight > 1) {
                Block bl = negZNeighborChunk.getBlockAtPosition(x, y, negZ);
                if (bl == null) {
                    parentWorld.addToSunlightQueue(new int[]{x, y, negZ});
                } else if (bl.isTranslucent()) {
                    parentWorld.addToSunlightQueue(new int[]{x, y, negZ});
                }
            }
        }

        if (posZNeighborChunk != null) {
            int posZSunlight = posZNeighborChunk.getSunlight(x, y, posZ);
            if (posZSunlight > 1) {
                Block bl = posZNeighborChunk.getBlockAtPosition(x, y, posZ);
                if (bl == null) {
                    parentWorld.addToSunlightQueue(new int[]{x, y, posZ});
                } else if (bl.isTranslucent()) {
                    parentWorld.addToSunlightQueue(new int[]{x, y, posZ});
                }
            }
        }

        if (y < height - 1) {
            Block posYBlock = VoxelGameAPI.instance.getBlockByID(blockList[x - startPosition.x][posY][z - startPosition.z]);
            if (getSunlight(x, y + 1, z) > 1) {
                if (posYBlock == null) {
                    parentWorld.addToSunlightQueue(new int[]{x, posY, z});
                } else if (posYBlock.isTranslucent()) {
                    parentWorld.addToSunlightQueue(new int[]{x, posY, z});
                }
            }
        }

    }

    @Override
    public void addBlock(int block, int x, int y, int z) {
        addBlock(block, x, y, z, true);
    }

    @Override
    public void addBlock(int block, int x, int y, int z, boolean updateSunlight) {
        x = x % size;
        z = z % size;
        if (x < 0) {
            x += size;
        }

        if (z < 0) {
            z += size;
        }

        if (y > height - 1) return;

        blockList[x][y][z] = block;
        if (block > 0) {
            highestPoint = Math.max(highestPoint, y);
        }

        if(updateSunlight)
            getWorld().addToSunlightRemovalQueue(new int[]{x + startPosition.x, y + startPosition.y, z + startPosition.z});
    }

    @Override
    public Vec3i getStartPosition() {
        return this.startPosition;
    }

    @Override
    public float getHighestPoint() {
        return highestPoint;
    }

    @Override
    public float getLightLevel(int x, int y, int z) {
        x %= size;
        z %= size;
        if (x < 0) {
            x += size;
        }

        if (z < 0) {
            z += size;
        }

        if (y > height - 1 || y < 0) {
            return 1;
        }


        return lightLevels[x][y][z];
    }

    @Override
    public int[][][] blocksToIdInt() {
        return blockList;
    }

    private void loadIdInts(int[][][] ints) {
        blockList = ints;
        highestPoint = 0;
        for (int x = 0; x < ints.length; x++) {
            for (int z = 0; z < ints[0][0].length; z++) {
                for (int y = 0; y < ints[0].length; y++) {
                    if(ints[x][y][z] > 0)
                        highestPoint = Math.max(y, highestPoint);
                }
            }
        }
    }

    private void setupSunlighting() {
        sunlightLevels = new int[size][height][size];
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                sunlightLevels[x][height - 1][z] = 16;
                parentWorld.addToSunlightQueue(new int[]{startPosition.x + x, height - 1, startPosition.z + z});
            }
        }
    }

    @Override
    public void setSunlight(int x, int y, int z, int level) {
        setSunlight(x, y, z, level, true);
    }

    @Override
    public void setSunlight(int x, int y, int z, int level, boolean updateNeighbors) {
        x %= size;
        z %= size;
        if (x < 0) {
            x += size;
        }

        if (z < 0) {
            z += size;
        }



        sunlightLevels[x][y][z] = level;
        lightLevels[x][y][z] = lightLevelMap[level];

        sunlightChanging = true;
        sunlightChanged = true;

        if(updateNeighbors) {
            if (x == 0) {
                IChunk xMinNeighbor = getWorld().getChunkAtPosition(startPosition.x - 1, startPosition.z);
                if (xMinNeighbor != null) {
                    getWorld().rerenderChunk(xMinNeighbor);
                }

            }

            if (x == size - 1) {
                IChunk xPlNeighbor = getWorld().getChunkAtPosition(startPosition.x + 1, startPosition.z);
                if (xPlNeighbor != null) {
                    getWorld().rerenderChunk(xPlNeighbor);
                }

            }

            if (z == 0) {
                IChunk zMinNeighbor = getWorld().getChunkAtPosition(startPosition.x, startPosition.z - 1);
                if (zMinNeighbor != null) {
                    getWorld().rerenderChunk(zMinNeighbor);
                }

            }

            if (z == size - 1) {
                IChunk zPlNeighbor = getWorld().getChunkAtPosition(startPosition.x, startPosition.z + 1);
                if (zPlNeighbor != null) {
                    getWorld().rerenderChunk(zPlNeighbor);
                }
            }
        }

    }

    @Override
    public int getSunlight(int x, int y, int z) {
        x %= size;
        z %= size;
        if (x < 0) {
            x += size;
        }

        if (z < 0) {
            z += size;
        }

        if (y > height - 1 || y < 0) {
            return -1;
        }

        return (sunlightLevels[x][y][z]);
    }

    @Override
    public void finishChangingSunlight() {
        sunlightChanging = false;
    }

    @Override
    public IWorld getWorld() {
        return this.parentWorld;
    }

    @Override
    public void cleanup() {
        if(opaqueModel != null)
            opaqueModel.dispose();
        if(translucentModel != null)
            translucentModel.dispose();
        cleanedUp = true;
    }

    public int getBlockIdAtPosition(int x, int y, int z) {
        x = x % size;
        z = z % size;
        if (x < 0) {
            x += size;
        }

        if (z < 0) {
            z += size;
        }


        if (y > height - 1) return 0;
        if (y < 0) return 0;
        return blockList[x][y][z];
    }

    @Override
    public Block getBlockAtPosition(int x, int y, int z) {
        x = x % size;
        z = z % size;
        if (x < 0) {
            x += size;
        }

        if (z < 0) {
            z += size;
        }
        
        if (y > height - 1) return null;
        if (y < 0) return null;
        return VoxelGameAPI.instance.getBlockByID(blockList[x][y][z]);
    }

    private void updateModelInstances() {
        if(opaqueModel != null)
            opaqueModel.dispose();
        if(translucentModel != null)
            translucentModel.dispose();

        Mesh opaqueMesh = mesher.meshFaces(opaqueFaces, meshBuilder);
        Mesh translucentMesh = mesher.meshFaces(translucentFaces, meshBuilder);
        modelBuilder.begin();
        modelBuilder.part(String.format("c-%d,%d", startPosition.x, startPosition.z), opaqueMesh, GL20.GL_TRIANGLES,
                new Material(TextureAttribute.createDiffuse(NormalBlockRenderer.getBlockMap())));
        opaqueModel = modelBuilder.end();
        modelBuilder.begin();
        modelBuilder.part(String.format("c-%d,%d-t", startPosition.x, startPosition.z), translucentMesh, GL20.GL_TRIANGLES,
                new Material(TextureAttribute.createDiffuse(NormalBlockRenderer.getBlockMap()),
                        new BlendingAttribute()));
        translucentModel = modelBuilder.end();

        opaqueModelInstance = new ModelInstance(opaqueModel);
        translucentModelInstance = new ModelInstance(translucentModel);
    }

    private void updateFaces() {
        parentWorld.incrChunksMeshing();
        final Block[][][] translucent = new Block[size][height][size];
        final Block[][][] opaque = new Block[size][height][size];
        eachBlock(new EachBlockCallee() {
            @Override
            public void call(Block block, int x, int y, int z) {
                if (block != null) {
                    if (block.isTranslucent())
                        translucent[x][y][z] = block;
                    else
                        opaque[x][y][z] = block;
                }
            }
        });
        opaqueFaces = mesher.getFaces(opaque, lightLevels);
        translucentFaces = mesher.getFaces(translucent, lightLevels);
        meshing = false;
        meshed = true;
        parentWorld.decrChunksMeshing();
        VoxelGameAPI.instance.getEventManager().push(new EventChunkRender(Chunk.this));
    }

}
