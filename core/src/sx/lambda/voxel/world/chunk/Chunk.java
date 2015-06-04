package sx.lambda.voxel.world.chunk;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.api.VoxelGameAPI;
import sx.lambda.voxel.api.events.render.EventChunkRender;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.block.NormalBlockRenderer;
import sx.lambda.voxel.block.Side;
import sx.lambda.voxel.client.render.meshing.GreedyMesher;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.IWorld;
import sx.lambda.voxel.world.biome.Biome;

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
    private final short[][][] metadata;
    private final transient IWorld parentWorld;
    private final Biome biome;
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
    private boolean lighted;

    private List<GreedyMesher.Face> translucentFaces;
    private List<GreedyMesher.Face> opaqueFaces;
    private boolean meshing, meshed, meshWhenDone;

    public Chunk(IWorld world, Vec3i startPosition, int[][][] ids, short[][][] meta, Biome biome) {
        this.parentWorld = world;
        this.startPosition = startPosition;
        this.biome = biome;
        this.size = world.getChunkSize();
        this.height = world.getHeight();
        this.metadata = meta;

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
    }

    public Chunk(IWorld world, Vec3i startPosition, Biome biome) {
        this.parentWorld = world;
        this.startPosition = startPosition;
        this.size = world.getChunkSize();
        this.height = world.getHeight();
        this.biome = biome;
        this.metadata = new short[size][height][size];

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
    }

    @Override
    public void rerender() {
        if (cleanedUp)
            return;

        if(sunlightChanging)
            return;

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

    private void addNeighborsToSunlightQueue(int x, int y, int z) {// X Y and Z are relative coords, not world coords
        assert x >= 0 && x < size && z >= 0 && z < size && y >= 0 && y < height;

        int cx = x;
        int cz = z;
        x += startPosition.x;
        z += startPosition.z;

        Side[] sides = Side.values();
        for(Side s : sides) {
            int sx = x; // Side x coord
            int sy = y; // Side y coord
            int sz = z; // Side z coord
            int scx = cx; // Chunk-relative side x coord
            int scz = cz; // Chunk-relative side z coord
            IChunk sChunk = this;

            // Offset values based on side
            switch(s) {
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
            if(sy < 0)
                continue;
            if(sy > height-1)
                continue;

            // Select the correct chunk
            if(scz < 0) {
                scz += size;
                sChunk = parentWorld.getChunk(sx, sz);
            } else if(scz > size-1) {
                scz -= size;
                sChunk = parentWorld.getChunk(sx, sz);
            }
            if(scx < 0) {
                scx += size;
                sChunk = parentWorld.getChunk(sx, sz);
            } else if(scx > size-1) {
                scx -= size;
                sChunk = parentWorld.getChunk(sx, sz);
            }

            if(sChunk == null)
                continue;

            int sSunlight = sChunk.getSunlight(scx, sy, scz);
            if (sSunlight > 1) {
                Block sBlock = sChunk.getBlock(scx, sy, scz);
                if (sBlock == null || sBlock.doesLightPassThrough() || !sBlock.decreasesLight()) {
                    parentWorld.addToSunlightQueue(sx, sy, sz);
                }
            }
        }
    }

    @Override
    public float getLightLevel(int x, int y, int z) {
        assert x >= 0 && x < size && z >= 0 && z < size && y >= 0 && y < height;

        return lightLevels[x][y][z];
    }

    @Override
    //TODO remove entirely in favor of setBlock(0 ?
    public void removeBlock(int x, int y, int z) {
        assert x >= 0 && x < size && z >= 0 && z < size && y >= 0 && y < height;

        if(x == size - 1) {
            getWorld().rerenderChunk(getWorld().getChunk(getStartPosition().x + size, getStartPosition().z));
        } else if(x == 0) {
            getWorld().rerenderChunk(getWorld().getChunk(getStartPosition().x - size, getStartPosition().z));
        }
        if(z == size - 1) {
            getWorld().rerenderChunk(getWorld().getChunk(getStartPosition().x, getStartPosition().z + size));
        } else if(z == 0) {
            getWorld().rerenderChunk(getWorld().getChunk(getStartPosition().x, getStartPosition().z - size));
        }

        blockList[x][y][z] = -1;

        getWorld().rerenderChunk(this);

        this.addNeighborsToSunlightQueue(x, y, z);
    }

    @Override
    public void setBlock(int block, int x, int y, int z) {
        setBlock(block, x, y, z, true);
    }

    @Override
    public void setBlock(int block, int x, int y, int z, boolean updateSunlight) {
        assert x >= 0 && x < size && z >= 0 && z < size && y >= 0 && y < height;

        if(block == 0) {
            removeBlock(x, y, z);
            return;
        }

        blockList[x][y][z] = block;
        if (block > 0) {
            highestPoint = Math.max(highestPoint, y);
        }

        if(updateSunlight)
            getWorld().addToSunlightRemovalQueue(x + startPosition.x, y + startPosition.y, z + startPosition.z);
    }

    @Override
    public void setMeta(short meta, int x, int y, int z) {
        assert x >= 0 && x < size && z >= 0 && z < size && y >= 0 && y < height;

        metadata[x][y][z] = meta;
    }

    @Override
    public short getMeta(int x, int y, int z) {
        assert x >= 0 && x < size && z >= 0 && z < size && y >= 0 && y < height;

        return metadata[x][y][z];
    }

    @Override
    public Vec3i getStartPosition() {
        return this.startPosition;
    }

    @Override
    public int getHighestPoint() {
        return highestPoint;
    }

    private void loadIdInts(int[][][] ints) {
        blockList = ints;
        highestPoint = 0;
        for (int x = 0; x < ints.length; x++) {
            for (int z = 0; z < ints[0][0].length; z++) {
                for (int y = 0; y < ints[0].length; y++) {
                    if (ints[x][y][z] > 0)
                        highestPoint = Math.max(y, highestPoint);
                }
            }
        }
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
                        new BlendingAttribute(),
                        FloatAttribute.createAlphaTest(0.25f)));
        translucentModel = modelBuilder.end();

        opaqueModelInstance = new ModelInstance(opaqueModel) {
            @Override
            public Renderable getRenderable(final Renderable out, final Node node,
                                            final NodePart nodePart) {
                super.getRenderable(out, node, nodePart);
                if(VoxelGameClient.getInstance().isWireframe()) {
                    out.primitiveType = GL20.GL_LINES;
                } else {
                    out.primitiveType = GL20.GL_TRIANGLES;
                }
                return out;
            }
        };
        translucentModelInstance = new ModelInstance(translucentModel) {
            @Override
            public Renderable getRenderable(final Renderable out, final Node node,
                                            final NodePart nodePart) {
                super.getRenderable(out, node, nodePart);
                if(VoxelGameClient.getInstance().isWireframe()) {
                    out.primitiveType = GL20.GL_LINES;
                } else {
                    out.primitiveType = GL20.GL_TRIANGLES;
                }
                return out;
            }
        };
    }

    private void updateFaces() {
        parentWorld.incrChunksMeshing();
        final Block[][][] translucent = new Block[size][height][size];
        final Block[][][] opaque = new Block[size][height][size];
        eachBlock((block, x, y, z) -> {
            if (block != null) {
                if (block.isTranslucent())
                    translucent[x][y][z] = block;
                else
                    opaque[x][y][z] = block;
            }
        });
        opaqueFaces = mesher.getFaces(opaque, metadata, lightLevels);
        translucentFaces = mesher.getFaces(translucent, metadata, lightLevels);
        meshing = false;
        meshed = true;
        parentWorld.decrChunksMeshing();
        VoxelGameAPI.instance.getEventManager().push(new EventChunkRender(Chunk.this));
    }

    @Override
    public void dispose() {
        if(opaqueModel != null)
            opaqueModel.dispose();
        if(translucentModel != null)
            translucentModel.dispose();
        cleanedUp = true;
    }

    @Override
    public void setSunlight(int x, int y, int z, int level) {
        assert x >= 0 && x < size && z >= 0 && z < size && y >= 0 && y < height;
        assert level >= 0 && level < 17;

        sunlightLevels[x][y][z] = level;
        lightLevels[x][y][z] = lightLevelMap[level];

        sunlightChanging = true;
        sunlightChanged = true;
    }

    @Override
    public int getSunlight(int x, int y, int z) {
        assert x >= 0 && x < size && z >= 0 && z < size && y >= 0 && y < height;

        return sunlightLevels[x][y][z];
    }

    @Override
    public int getBlockId(int x, int y, int z) {
        assert x >= 0 && x < size && z >= 0 && z < size && y >= 0 && y < height;

        return blockList[x][y][z];
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        assert x >= 0 && x < size && z >= 0 && z < size && y >= 0 && y < height;

        return VoxelGameAPI.instance.getBlockByID(blockList[x][y][z]);
    }

    @Override
    public void finishChangingSunlight() {
        sunlightChanging = false;
    }

    @Override
    public boolean waitingOnLightFinish() {
        return sunlightChanging;
    }

    @Override
    public boolean hasInitialSun() {
        return lighted;
    }

    @Override
    public void finishAddingSun() {
        this.lighted = true;
    }

    @Override
    public Biome getBiome() {
        return this.biome;
    }

    @Override
    public IWorld getWorld() {
        return this.parentWorld;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.startPosition.x;
        hash = 71 * hash + this.startPosition.z;
        return hash;
    }

}
