package sx.lambda.voxel.world.chunk

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.utils.BufferUtils
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.api.events.render.EventChunkRender
import sx.lambda.voxel.block.Block
import sx.lambda.voxel.block.NormalBlockRenderer
import sx.lambda.voxel.client.render.meshing.Mesher
import sx.lambda.voxel.util.Vec3i
import sx.lambda.voxel.world.IWorld
import sx.lambda.voxel.api.VoxelGameAPI
import sx.lambda.voxel.client.render.meshing.GreedyMesher

import java.nio.IntBuffer

import static com.badlogic.gdx.graphics.GL20.*

@CompileStatic
public class Chunk implements IChunk {

    private int[][][] blockList

    private transient IWorld parentWorld
    private transient final Mesher mesher
    private transient MeshBuilder meshBuilder
    private transient ModelBuilder modelBuilder

    private final int size
    private final int height

    private Vec3i startPosition

    private int highestPoint

    private transient Model opaqueModel, transparentModel, box
    private transient ModelInstance opaqueInstance, transparentInstance, boxInstance

    private transient float[][][] lightLevels
    private transient int[][][] sunlightLevels
    private transient boolean sunlightChanging
    private transient boolean sunlightChanged

    private boolean setup, cleanedUp

    /**
     * Map of light levels (integers 0-16) to brightness multipliers
     */
    private final float[] lightLevelMap = new float[17]

    public Chunk(IWorld world, Vec3i startPosition, int[][][] ids) {
        this.parentWorld = world
        this.startPosition = startPosition
        this.size = world.getChunkSize()
        this.height = world.getHeight()

        for(int i = 0; i < 17; i++) {
            int reduction = 16-i
            lightLevelMap[i] = Math.pow(0.8, reduction) as float
        }
        sunlightLevels = new int[size][height][size]

        if(VoxelGameClient.instance != null) { // We're a client
            mesher = new GreedyMesher(this)
        } else {
            mesher = null
        }

        this.loadIdInts(ids)

        lightLevels = new float[size][height][size]

        setupSunlighting()
    }

    public Chunk(IWorld world, Vec3i startPosition) {
        this.parentWorld = world
        this.startPosition = startPosition;
        this.size = world.getChunkSize()
        this.height = world.getHeight()

        for(int i = 0; i < 17; i++) {
            int reduction = 16-i
            lightLevelMap[i] = Math.pow(0.8, reduction) as float
        }
        sunlightLevels = new int[size][height][size]

        if(VoxelGameClient.instance != null) { // We're a client
            mesher = new GreedyMesher(this)
        } else {
            mesher = null
        }

        this.blockList = new int[size][height][size]
        highestPoint = world.chunkGen.generate(startPosition, blockList)

        lightLevels = new float[size][height][size]

        setupSunlighting()
    }

    @Override
    public void rerender() {
        if(cleanedUp)return;
        if(this.parentWorld == null) {
            if(VoxelGameClient.instance != null) { // We're a client
                this.parentWorld = VoxelGameClient.instance.world
            }
        }

        if(!setup) {
            meshBuilder = new MeshBuilder()
            modelBuilder = new ModelBuilder()

            setup = true
        }

        sunlightChanged = false

        Block[][][] transparent = new Block[size][height][size]
        Block[][][] opaque = new Block[size][height][size]
        eachBlock { Block it, int x, int y, int z ->
            if(it != null) {
                if (it.transparent)
                    transparent[x][y][z] = it
                else
                    opaque[x][y][z] = it
            }
        }
        mesher.disableAlpha()
        Mesh opaqueResult = mesher.meshVoxels(meshBuilder, opaque, lightLevels)
        mesher.enableAlpha()
        Mesh transparentResult = mesher.meshVoxels(meshBuilder, transparent, lightLevels)

        modelBuilder.begin()
        modelBuilder.part(String.format("c-%d,%d", startPosition.x, startPosition.z), opaqueResult, GL_TRIANGLES,
                new Material(TextureAttribute.createDiffuse(NormalBlockRenderer.blockMap)))
        opaqueModel = modelBuilder.end();
        opaqueInstance = new ModelInstance(opaqueModel)

        modelBuilder.begin()
        modelBuilder.part(String.format("c-%d,%d-t", startPosition.x, startPosition.z), transparentResult, GL_TRIANGLES,
                new Material(TextureAttribute.createDiffuse(NormalBlockRenderer.blockMap)))
        transparentModel = modelBuilder.end();
        transparentInstance = new ModelInstance(transparentModel)

        box = modelBuilder.createBox(10, 10, 10, new Material(ColorAttribute.createAmbient(Color.RED)), VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal | VertexAttributes.Usage.Position)
        boxInstance = new ModelInstance(box)

        VoxelGameAPI.instance.eventManager.push(new EventChunkRender(this))
    }

    @Override
    public void render(ModelBatch batch) {
        if(cleanedUp)return;
        if(sunlightChanged && !sunlightChanging) {
            rerender()
        }

        if(setup) {
            Gdx.gl.glDisable(GL_BLEND)

            batch.render(opaqueInstance)

            batch.render(boxInstance)
        }
    }

    @Override
    public void eachBlock(Closure action) {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < size; z++) {
                    Block blk = VoxelGameAPI.instance.getBlockByID(blockList[x][y][z])
                    action(blk, x, y, z)
                }
            }
        }
    }

    public void renderWater(ModelBatch batch) {
        if(cleanedUp)return;
        if(setup) {
            VoxelGameClient.instance.worldShader.enableWaves()
            Gdx.gl.glEnable(GL_BLEND)

            batch.render(transparentInstance)
            VoxelGameClient.instance.worldShader.disableWaves()
        }
    }

    public Block getBlockAtPosition(Vec3i position) {
        int x = position.x % size;
        int y = position.y;
        int z = position.z % size;
        if (x < 0) {
            x += size
        }
        if (z < 0) {
            z += size
        }

        if (y > height - 1) return null
        if (y < 0) return null

        return VoxelGameAPI.instance.getBlockByID(blockList[x][y][z]);
    }

    @Override
    public void removeBlock(Vec3i Vec3i) {
        int x = Vec3i.x % size;
        int y = Vec3i.y;
        int z = Vec3i.z % size;
        if (x < 0) {
            x += size
        }
        if (z < 0) {
            z += size
        }

        if (y > height - 1) return

        blockList[x][y][z] = -1

        this.addNeighborsToSunlightQueue(x, y, z)

    }

    private void addNeighborsToSunlightQueue(int x, int y, int z) { // X Y and Z are relative coords, not world coords
        Vec3i pos = new Vec3i(startPosition.x + x, startPosition.y + y, startPosition.z + z)
        Vec3i negXNeighborPos = pos.translate(-1,0,0);
        Vec3i posXNeighborPos = pos.translate(1,0,0);
        Vec3i negZNeighborPos = pos.translate(0,0,-1);
        Vec3i posZNeighborPos = pos.translate(0,0,1);
        Vec3i posYNeighborPos = pos.translate(0,1,0);
        IChunk negXNeighborChunk = parentWorld.getChunkAtPosition(negXNeighborPos);
        IChunk posXNeighborChunk = parentWorld.getChunkAtPosition(posXNeighborPos);
        IChunk negZNeighborChunk = parentWorld.getChunkAtPosition(negZNeighborPos);
        IChunk posZNeighborChunk = parentWorld.getChunkAtPosition(posZNeighborPos);

        if(negXNeighborChunk != null) {
            int negXSunlight = negXNeighborChunk.getSunlight(negXNeighborPos.x, negXNeighborPos.y, negXNeighborPos.z)
            if(negXSunlight > 1) {
                Block bl = negXNeighborChunk.getBlockAtPosition(negXNeighborPos);
                if (bl == null) {
                    parentWorld.addToSunlightQueue(negXNeighborPos)
                } else if (bl.isTransparent()) {
                    parentWorld.addToSunlightQueue(negXNeighborPos)
                }
            }
        }
        if(posXNeighborChunk != null) {
            int posXSunlight = posXNeighborChunk.getSunlight(posXNeighborPos.x, posXNeighborPos.y, posXNeighborPos.z)
            if(posXSunlight > 1) {
                Block bl = posXNeighborChunk.getBlockAtPosition(posXNeighborPos);
                if (bl == null) {
                    parentWorld.addToSunlightQueue(posXNeighborPos)
                } else if (bl.isTransparent()) {
                    parentWorld.addToSunlightQueue(posXNeighborPos)
                }
            }
        }
        if(negZNeighborChunk != null) {
            int negZSunlight = negZNeighborChunk.getSunlight(negZNeighborPos.x, negZNeighborPos.y, negZNeighborPos.z)
            if(negZSunlight > 1) {
                Block bl = negZNeighborChunk.getBlockAtPosition(negZNeighborPos);
                if (bl == null) {
                    parentWorld.addToSunlightQueue(negZNeighborPos)
                } else if (bl.isTransparent()) {
                    parentWorld.addToSunlightQueue(negZNeighborPos)
                }
            }
        }
        if(posZNeighborChunk != null) {
            int posZSunlight = posZNeighborChunk.getSunlight(posZNeighborPos.x, posZNeighborPos.y, posZNeighborPos.z)
            if(posZSunlight > 1) {
                Block bl = posZNeighborChunk.getBlockAtPosition(posZNeighborPos);
                if (bl == null) {
                    parentWorld.addToSunlightQueue(posZNeighborPos)
                } else if (bl.isTransparent()) {
                    parentWorld.addToSunlightQueue(posZNeighborPos)
                }
            }
        }

        if(y < height-1) {
            Block posYBlock = VoxelGameAPI.instance.getBlockByID(blockList[x][y+1][z])
            if(getSunlight(x, y+1, z) > 1) {
                if (posYBlock == null) {
                    parentWorld.addToSunlightQueue(posYNeighborPos)
                } else if (posYBlock.isTransparent()) {
                    parentWorld.addToSunlightQueue(posYNeighborPos)
                }
            }
        }
    }

    @Override
    void addBlock(int block, Vec3i position) {
        int x = position.x % size;
        int y = position.y;
        int z = position.z % size;
        if (x < 0) {
            x += size
        }
        if (z < 0) {
            z += size
        }

        if (y > height - 1) return

        blockList[x][y][z] = block
        if (block != null) {
            highestPoint = Math.max(highestPoint, y)
        }

        world.addToSunlightRemovalQueue new Vec3i(x + startPosition.x, y + startPosition.y, z + startPosition.z)
    }

    @Override
    public Vec3i getStartPosition() {
        this.startPosition
    }

    @Override
    public float getHighestPoint() { highestPoint }

    @Override
    float getLightLevel(int x, int y, int z) {
        x %= size
        z %= size
        if (x < 0) {
            x += size
        }
        if (z < 0) {
            z += size
        }
        if(y > height-1 || y < 0) {
            return 1
        }

        return lightLevels[x][y][z];
    }

    @Override
    public int[][][] blocksToIdInt() {
        return blockList
    }

    private void loadIdInts(int[][][] ints) {
        blockList = ints
        highestPoint = 0
        for(int x = 0; x < ints.length; x++) {
            for(int z = 0; z < ints[0][0].length; z++) {
                for(int y = 0; y < ints[0].length; y++) {
                    highestPoint = Math.max(y, highestPoint)
                }
            }
        }
    }

    private void setupSunlighting() {
        sunlightLevels = new int[size][height][size]
        for(int x = 0; x < size; x++) {
            for(int z = 0; z < size; z++) {
                sunlightLevels[x][height-1][z] = 16
                parentWorld.addToSunlightQueue(new Vec3i(startPosition.x+x, height-1, startPosition.z+z as int))
            }
        }
    }

    @Override
    public void setSunlight(int x, int y, int z, int level) {
        x %= size
        z %= size
        if (x < 0) {
            x += size
        }
        if (z < 0) {
            z += size
        }

        sunlightLevels[x][y][z] = level
        lightLevels[x][y][z] = lightLevelMap[level]

        sunlightChanging = true
        sunlightChanged = true

        if(x == 0) {
            IChunk xMinNeighbor = world.getChunkAtPosition(startPosition.translate(-1, 0, 0))
            if(xMinNeighbor != null) {
                world.rerenderChunk(xMinNeighbor)
            }
        }
        if(x == size-1) {
            IChunk xPlNeighbor = world.getChunkAtPosition(startPosition.translate(1, 0, 0))
            if(xPlNeighbor != null) {
                world.rerenderChunk(xPlNeighbor)
            }
        }
        if(z == 0) {
            IChunk zMinNeighbor = world.getChunkAtPosition(startPosition.translate(0, 0, -1))
            if(zMinNeighbor != null) {
                world.rerenderChunk(zMinNeighbor)
            }
        }
        if(z == size-1) {
            IChunk zPlNeighbor = world.getChunkAtPosition(startPosition.translate(0, 0, 1))
            if(zPlNeighbor != null) {
                world.rerenderChunk(zPlNeighbor)
            }
        }
    }

    @Override
    public int getSunlight(int x, int y, int z) {
        x %= size
        z %= size
        if (x < 0) {
            x += size
        }
        if (z < 0) {
            z += size
        }
        if(y > height-1 || y < 0) {
            return -1
        }
        return sunlightLevels[x][y][z]
    }

    @Override
    public void finishChangingSunlight() {
        sunlightChanging = false
    }

    @Override
    IWorld getWorld() {
        return this.parentWorld
    }

    @Override
    void cleanup() {
        if(setup) {
            transparentModel.dispose()
            opaqueModel.dispose()
        }
        cleanedUp = true
    }

    int getBlockIdAtPosition(int x, int y, int z) {
        x = x % size;
        y = y;
        z = z % size;
        if (x < 0) {
            x += size
        }
        if (z < 0) {
            z += size
        }

        if (y > height - 1) return 0
        if (y < 0) return 0
        return blockList[x][y][z]
    }
}