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
import com.badlogic.gdx.math.MathUtils;
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
import sx.lambda.voxel.world.chunk.BlockStorage.CoordinatesOutOfBoundsException;

import java.util.List;

public class Chunk implements IChunk {

    private static final int MAX_LIGHT_LEVEL = 15;

    private final transient GreedyMesher mesher;
    private final int size;
    private final int height;
    private final BlockStorage blockStorage;
    /**
     * Map of light levels (ints 0-15) to brightness multipliers
     */
    private final float[] lightLevelMap = new float[MAX_LIGHT_LEVEL+1];
    private final transient IWorld parentWorld;
    private final Biome biome;
    private transient MeshBuilder meshBuilder;
    private transient ModelBuilder modelBuilder;
    private transient Model opaqueModel, translucentModel;
    private transient ModelInstance opaqueModelInstance, translucentModelInstance;
    private final Vec3i startPosition;
    private int highestPoint;
    private transient boolean sunlightChanging;
    private transient boolean sunlightChanged;
    private boolean setup;
    private boolean cleanedUp;
    private boolean lighted;

    private List<GreedyMesher.Face> translucentFaces;
    private List<GreedyMesher.Face> opaqueFaces;
    private boolean meshing, meshed, meshWhenDone;

    public Chunk(IWorld world, Vec3i startPosition, Biome biome, boolean local) {
        this.parentWorld = world;
        this.startPosition = startPosition;
        this.biome = biome;
        this.size = world.getChunkSize();
        this.height = world.getHeight();

        this.blockStorage = new FlatBlockStorage(size, height, size);

        for (int i = 0; i <= MAX_LIGHT_LEVEL; i++) {
            int reduction = MAX_LIGHT_LEVEL - i;
            lightLevelMap[i] = (float) Math.pow(0.8, reduction);
        }

        if (VoxelGameClient.getInstance() != null) {// We're a client
            mesher = new GreedyMesher(this, VoxelGameClient.getInstance().getSettingsManager().getVisualSettings().perCornerLightEnabled());
        } else {
            mesher = null;
        }

        if(local)
            highestPoint = world.getChunkGen().generate(startPosition, blockStorage);
    }

    @Override
    public void rerender() {
        if (cleanedUp)
            return;

        boolean neighborSunlightChanging = false;
        for(int x = startPosition.x - 16; x <= startPosition.x + 16; x += 16) {
            for(int z = startPosition.z - 16; z <= startPosition.z + 16; z += 16) {
                IChunk c = getWorld().getChunk(x, z);
                if(c != null && c.waitingOnLightFinish()) {
                    neighborSunlightChanging = true;
                }
            }
        }
        if(neighborSunlightChanging)
            return;

        if (!setup) {
            meshBuilder = new MeshBuilder();
            modelBuilder = new ModelBuilder();

            setup = true;
        }

        sunlightChanged = false;

        if(meshing) {
            meshWhenDone = true;
        } else {
            meshing = true;
            parentWorld.addToMeshQueue(this::updateFaces);
        }
    }

    @Override
    public void render(ModelBatch batch) {
        if (cleanedUp) return;

        if(!meshing && meshed) {
            getWorld().addToChunkUploadQueue(this::updateModelInstances);
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
                    try {
                        Block blk = blockStorage.getBlock(x, y, z);
                        callee.call(blk, x, y, z);
                    } catch (CoordinatesOutOfBoundsException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private void addNeighborsToLightQueues(int x, int y, int z) {// X Y and Z are relative coords, not world coords
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

            try {
                int sSunlight = sChunk.getSunlight(scx, sy, scz);
                int sBlocklight = sChunk.getBlocklight(scx, sy, scz);
                if (sSunlight > 0 || sBlocklight > 0) {
                    Block sBlock = sChunk.getBlock(scx, sy, scz);
                    if (sBlock == null || sBlock.doesLightPassThrough() || !sBlock.decreasesLight()) {
                        if (sSunlight > 0)
                            parentWorld.addToSunlightQueue(sx, sy, sz);
                        if (sBlocklight > 0)
                            parentWorld.addToBlocklightQueue(sx, sy, sz);
                    }
                }
            } catch (CoordinatesOutOfBoundsException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    //TODO remove entirely in favor of setBlock(0 ?
    public void removeBlock(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        if(x < 0 || x >= size || z < 0 || z >= size || y < 0 || y >= height)
            throw new CoordinatesOutOfBoundsException();

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

        blockStorage.setBlock(x, y, z, null);
        blockStorage.setId(x, y, z, 0);
        blockStorage.setMeta(x, y, z, 0);
        blockStorage.setSunlight(x, y, z, 0);
        blockStorage.setBlocklight(x, y, z, 0);
        // TODO XXX LIGHTING add to block light removal queue

        getWorld().rerenderChunk(this);

        this.addNeighborsToLightQueues(x, y, z);
    }

    @Override
    public void setBlock(int block, int x, int y, int z) throws CoordinatesOutOfBoundsException {
        setBlock(block, x, y, z, true);
    }

    @Override
    public void setBlock(int block, int x, int y, int z, boolean updateSunlight) throws CoordinatesOutOfBoundsException {
        if(x < 0 || x >= size || z < 0 || z >= size || y < 0 || y >= height)
            throw new CoordinatesOutOfBoundsException();

        if(block == 0) {
            removeBlock(x, y, z);
            return;
        }

        int oldBlock = blockStorage.getId(x, y, z);
        Block blk = VoxelGameAPI.instance.getBlockByID(block);
        int newBlocklightVal = blk.getLightValue();
        blockStorage.setBlocklight(x, y, z, newBlocklightVal);
        if(oldBlock > 0) {
            Block oldBlk = VoxelGameAPI.instance.getBlockByID(oldBlock);
            int oldBlocklightVal = oldBlk.getLightValue();
            if(newBlocklightVal > oldBlocklightVal) {
                parentWorld.addToBlocklightQueue(startPosition.x + x, startPosition.y + y, startPosition.z + z);
            } else if(oldBlocklightVal > newBlocklightVal) {
                // TODO XXX LIGTHTING add to blocklight removal queue
            }
        } else {
            if(newBlocklightVal > 0) {
                parentWorld.addToBlocklightQueue(startPosition.x + x, startPosition.y + y, startPosition.z + z);
            }
        }

        blockStorage.setId(x, y, z, block);
        blockStorage.setBlock(x, y, z, blk);
        highestPoint = Math.max(highestPoint, y);

        if(updateSunlight)
            getWorld().addToSunlightRemovalQueue(x + startPosition.x, y + startPosition.y, z + startPosition.z);
    }

    @Override
    public void setMeta(short meta, int x, int y, int z) throws CoordinatesOutOfBoundsException {
        blockStorage.setMeta(x, y, z, meta);
    }

    @Override
    public short getMeta(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        return blockStorage.getMeta(x, y, z);
    }

    @Override
    public float getLightLevel(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        int sunlight = blockStorage.getSunlight(x, y, z);
        int blocklight = blockStorage.getBlocklight(x, y, z);
        return lightLevelMap[MathUtils.clamp(sunlight+blocklight, 0, MAX_LIGHT_LEVEL)];
    }

    @Override
    public Vec3i getStartPosition() {
        return this.startPosition;
    }

    @Override
    public int getHighestPoint() {
        return highestPoint;
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
        opaqueFaces = mesher.getFaces(blockStorage, block -> !block.isTranslucent());
        translucentFaces = mesher.getFaces(blockStorage, Block::isTranslucent);
        meshing = false;
        meshed = true;
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
    public void setSunlight(int x, int y, int z, int level) throws CoordinatesOutOfBoundsException {
        assert level >= 0 && level <= MAX_LIGHT_LEVEL;

        blockStorage.setSunlight(x, y, z, level);

        sunlightChanging = true;
        sunlightChanged = true;
    }

    @Override
    public int getSunlight(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        return blockStorage.getSunlight(x, y, z);
    }

    @Override
    public void setBlocklight(int x, int y, int z, int level) throws CoordinatesOutOfBoundsException {
        assert level >= 0 && level <= MAX_LIGHT_LEVEL;

        blockStorage.setBlocklight(x, y, z, level);
    }

    @Override
    public int getBlocklight(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        return blockStorage.getBlocklight(x, y, z);
    }

    @Override
    public int getBlockId(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        return blockStorage.getId(x, y, z);
    }

    @Override
    public Block getBlock(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        return blockStorage.getBlock(x, y, z);
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
    public int getMaxLightLevel() {
        return MAX_LIGHT_LEVEL;
    }

    @Override
    public float getBrightness(int lightLevel) {
        if(lightLevel > getMaxLightLevel())
            return 1;
        if(lightLevel < 0)
            return 0;
        return lightLevelMap[lightLevel];
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.startPosition.x;
        hash = 71 * hash + this.startPosition.z;
        return hash;
    }

}
