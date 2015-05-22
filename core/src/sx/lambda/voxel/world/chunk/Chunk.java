package sx.lambda.voxel.world.chunk;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import groovy.lang.Closure;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.api.VoxelGameAPI;
import sx.lambda.voxel.api.events.render.EventChunkRender;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.client.render.meshing.GreedyMesher;
import sx.lambda.voxel.client.render.meshing.Mesher;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.IWorld;

public class Chunk implements IChunk {
    private final transient Mesher mesher;
    private final int size;
    private final int height;
    /**
     * Map of light levels (ints 0-16) to brightness multipliers
     */
    private final float[] lightLevelMap = new float[17];
    private int[][][] blockList;
    private transient IWorld parentWorld;
    private transient MeshBuilder meshBuilder;
    private final Vec3i startPosition;
    private int highestPoint;
    private Mesh opaqueMesh;
    private Mesh transparentMesh;
    private final transient float[][][] lightLevels;
    private transient int[][][] sunlightLevels;
    private transient boolean sunlightChanging;
    private transient boolean sunlightChanged;
    private boolean setup;
    private boolean cleanedUp;
    private boolean rerenderNext;

    public Chunk(IWorld world, Vec3i startPosition, int[][][] ids) {
        this.parentWorld = world;
        this.startPosition = startPosition;
        this.size = world.getChunkSize();
        this.height = world.getHeight();

        for (int i = 0; i < 17; i++) {
            int reduction = 16 - i;
            lightLevelMap[i] = (float)Math.pow(0.8, reduction);
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
            lightLevelMap[i] = (float)Math.pow(0.8, reduction);
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

            setup = true;
        }


        sunlightChanged = false;

        final Block[][][] transparent = new Block[size][height][size];
        final Block[][][] opaque = new Block[size][height][size];
        eachBlock(new Closure<Block>(this, this) {
            public Block doCall(Block it, int x, int y, int z) {
                if (it != null) {
                    if (it.isTransparent()) return transparent[x][y][z] = it;
                    else return opaque[x][y][z] = it;
                } else {
                    return null;
                }
            }
        });
        mesher.disableAlpha();
        opaqueMesh = mesher.meshVoxels(meshBuilder, opaque, lightLevels);
        mesher.enableAlpha();
        transparentMesh = mesher.meshVoxels(meshBuilder, transparent, lightLevels);

        VoxelGameAPI.instance.getEventManager().push(new EventChunkRender(this));
    }

    @Override
    public void render() {
        if (cleanedUp) return;

        if ((sunlightChanged && !sunlightChanging) || rerenderNext) {
            rerender();
            rerenderNext = false;
        }

    }

    @Override
    public void eachBlock(Closure action) {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < size; z++) {
                    Block blk = VoxelGameAPI.instance.getBlockByID(blockList[x][y][z]);
                    action.call(blk, x, y, z);
                }

            }

        }

    }

    public void renderWater() {
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
    public void removeBlock(Vec3i Vec3i) {
        int x = Vec3i.x % size;
        int y = Vec3i.y;
        int z = Vec3i.z % size;
        if (x < 0) {
            x += size;
        }

        if (z < 0) {
            z += size;
        }


        if (y > height - 1) return;


        blockList[x][y][z] = -1;

        this.addNeighborsToSunlightQueue(x, y, z);

    }

    private void addNeighborsToSunlightQueue(int x, int y, int z) {// X Y and Z are relative coords, not world coords
        Vec3i pos = new Vec3i(startPosition.x + x, startPosition.y + y, startPosition.z + z);
        Vec3i negXNeighborPos = pos.translate(-1, 0, 0);
        Vec3i posXNeighborPos = pos.translate(1, 0, 0);
        Vec3i negZNeighborPos = pos.translate(0, 0, -1);
        Vec3i posZNeighborPos = pos.translate(0, 0, 1);
        Vec3i posYNeighborPos = pos.translate(0, 1, 0);
        IChunk negXNeighborChunk = parentWorld.getChunkAtPosition(negXNeighborPos);
        IChunk posXNeighborChunk = parentWorld.getChunkAtPosition(posXNeighborPos);
        IChunk negZNeighborChunk = parentWorld.getChunkAtPosition(negZNeighborPos);
        IChunk posZNeighborChunk = parentWorld.getChunkAtPosition(posZNeighborPos);

        if (negXNeighborChunk != null) {
            int negXSunlight = negXNeighborChunk.getSunlight(negXNeighborPos.x, negXNeighborPos.y, negXNeighborPos.z);
            if (negXSunlight > 1) {
                Block bl = negXNeighborChunk.getBlockAtPosition(negXNeighborPos);
                if (bl == null) {
                    parentWorld.addToSunlightQueue(negXNeighborPos);
                } else if (bl.isTransparent()) {
                    parentWorld.addToSunlightQueue(negXNeighborPos);
                }

            }

        }

        if (posXNeighborChunk != null) {
            int posXSunlight = posXNeighborChunk.getSunlight(posXNeighborPos.x, posXNeighborPos.y, posXNeighborPos.z);
            if (posXSunlight > 1) {
                Block bl = posXNeighborChunk.getBlockAtPosition(posXNeighborPos);
                if (bl == null) {
                    parentWorld.addToSunlightQueue(posXNeighborPos);
                } else if (bl.isTransparent()) {
                    parentWorld.addToSunlightQueue(posXNeighborPos);
                }

            }

        }

        if (negZNeighborChunk != null) {
            int negZSunlight = negZNeighborChunk.getSunlight(negZNeighborPos.x, negZNeighborPos.y, negZNeighborPos.z);
            if (negZSunlight > 1) {
                Block bl = negZNeighborChunk.getBlockAtPosition(negZNeighborPos);
                if (bl == null) {
                    parentWorld.addToSunlightQueue(negZNeighborPos);
                } else if (bl.isTransparent()) {
                    parentWorld.addToSunlightQueue(negZNeighborPos);
                }

            }

        }

        if (posZNeighborChunk != null) {
            int posZSunlight = posZNeighborChunk.getSunlight(posZNeighborPos.x, posZNeighborPos.y, posZNeighborPos.z);
            if (posZSunlight > 1) {
                Block bl = posZNeighborChunk.getBlockAtPosition(posZNeighborPos);
                if (bl == null) {
                    parentWorld.addToSunlightQueue(posZNeighborPos);
                } else if (bl.isTransparent()) {
                    parentWorld.addToSunlightQueue(posZNeighborPos);
                }

            }

        }


        if (y < height - 1) {
            Block posYBlock = VoxelGameAPI.instance.getBlockByID(blockList[x][y + 1][z]);
            if (getSunlight(x, y + 1, z) > 1) {
                if (posYBlock == null) {
                    parentWorld.addToSunlightQueue(posYNeighborPos);
                } else if (posYBlock.isTransparent()) {
                    parentWorld.addToSunlightQueue(posYNeighborPos);
                }

            }

        }

    }

    @Override
    public void addBlock(int block, Vec3i position) {
        int x = position.x % size;
        int y = position.y;
        int z = position.z % size;
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


        getWorld().addToSunlightRemovalQueue(new Vec3i(x + startPosition.x, y + startPosition.y, z + startPosition.z));
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
                parentWorld.addToSunlightQueue(new Vec3i(startPosition.x + x, height - 1, startPosition.z + z));
            }

        }

    }

    @Override
    public void setSunlight(int x, int y, int z, int level) {
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

        if (x == 0) {
            IChunk xMinNeighbor = getWorld().getChunkAtPosition(startPosition.translate(-1, 0, 0));
            if (xMinNeighbor != null) {
                getWorld().rerenderChunk(xMinNeighbor);
            }

        }

        if (x == size - 1) {
            IChunk xPlNeighbor = getWorld().getChunkAtPosition(startPosition.translate(1, 0, 0));
            if (xPlNeighbor != null) {
                getWorld().rerenderChunk(xPlNeighbor);
            }

        }

        if (z == 0) {
            IChunk zMinNeighbor = getWorld().getChunkAtPosition(startPosition.translate(0, 0, -1));
            if (zMinNeighbor != null) {
                getWorld().rerenderChunk(zMinNeighbor);
            }

        }

        if (z == size - 1) {
            IChunk zPlNeighbor = getWorld().getChunkAtPosition(startPosition.translate(0, 0, 1));
            if (zPlNeighbor != null) {
                getWorld().rerenderChunk(zPlNeighbor);
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
        rerenderNext = true;
    }

    @Override
    public IWorld getWorld() {
        return this.parentWorld;
    }

    @Override
    public void cleanup() {
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

    public Mesh getMesh() {
        return opaqueMesh;
    }

    public Mesh getTransparentMesh() {
        return transparentMesh;
    }
}
