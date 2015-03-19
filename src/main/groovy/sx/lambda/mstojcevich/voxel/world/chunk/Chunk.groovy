package sx.lambda.mstojcevich.voxel.world.chunk

import groovy.transform.CompileStatic
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL15
import sx.lambda.mstojcevich.voxel.api.VoxelGameAPI
import sx.lambda.mstojcevich.voxel.api.events.render.EventChunkRender
import sx.lambda.mstojcevich.voxel.block.Block
import sx.lambda.mstojcevich.voxel.block.NormalBlockRenderer
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.client.render.light.LightLevelCalculator
import sx.lambda.mstojcevich.voxel.client.render.light.SunAndNeighborLLC
import sx.lambda.mstojcevich.voxel.client.render.meshing.MeshResult
import sx.lambda.mstojcevich.voxel.client.render.meshing.Mesher
import sx.lambda.mstojcevich.voxel.client.render.meshing.PlainMesher
import sx.lambda.mstojcevich.voxel.util.Vec3i
import sx.lambda.mstojcevich.voxel.world.IWorld

import java.nio.IntBuffer

import static org.lwjgl.opengl.GL11.*

@CompileStatic
public class Chunk implements IChunk {

    private Block[][][] blockList

    private transient IWorld parentWorld
    private transient final Mesher mesher
    private transient final LightLevelCalculator lightLevelCalculator

    private final int size
    private final int height

    private Vec3i startPosition

    private int highestPoint

    private transient int vboVertexHandle = -1
    private transient int vboTextureHandle = -1
    private transient int vboNormalHandle = -1
    private transient int vboColorHandle = -1

    private transient int liquidVboVertexHandle = -1
    private transient int liquidVboTextureHandle = -1
    private transient int liquidVboNormalHandle = -1
    private transient int liquidVboColorHandle = -1

    private transient int opaqueVertexCount = 0
    private transient int transparentVertexCount = 0

    private transient float[][][] lightLevels
    private transient int[][][] sunlightLevels
    private transient boolean sunlightChanging
    private transient boolean sunlightChanged

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

        lightLevelCalculator = new SunAndNeighborLLC(this)

        if(VoxelGame.instance != null) { // We're a client
            mesher = new PlainMesher(this)
        } else {
            mesher = null
        }

        this.loadIdInts(ids)

        lightLevels = new float[size][height][size]
        //lightLevelCalculator.calculateLightLevels(blockList, lightLevels)

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

        lightLevelCalculator = new SunAndNeighborLLC(this)

        if(VoxelGame.instance != null) { // We're a client
            mesher = new PlainMesher(this)
        } else {
            mesher = null
        }

        this.blockList = new Block[size][height][size]
        highestPoint = world.chunkGen.generate(startPosition, blockList)

        lightLevels = new float[size][height][size]

        setupSunlighting()
    }

    @Override
    public void rerender() {
        if(this.parentWorld == null) {
            if(VoxelGame.instance != null) { // We're a client
                this.parentWorld = VoxelGame.instance.world
            }
        }

        if(vboVertexHandle == -1 || vboVertexHandle == 0) {
            IntBuffer buffer = BufferUtils.createIntBuffer(8)
            GL15.glGenBuffers(buffer)
            vboVertexHandle = buffer.get(0)
            vboTextureHandle = buffer.get(1)
            vboNormalHandle = buffer.get(2)
            vboColorHandle = buffer.get(3)
            liquidVboVertexHandle = buffer.get(4)
            liquidVboTextureHandle = buffer.get(5)
            liquidVboNormalHandle = buffer.get(6)
            liquidVboColorHandle = buffer.get(7)

            glEnableClientState(GL_NORMAL_ARRAY)
            glEnableClientState(GL_VERTEX_ARRAY)
            glEnableClientState(GL_TEXTURE_COORD_ARRAY)
            glEnableClientState(GL_COLOR_ARRAY)
        }

        if(sunlightChanged) {
            for(int x = 0; x < size; x++) {
                for (int y = 0; y < height; y++) {
                    for (int z = 0; z < size; z++) {
                        lightLevels[x][y][z] = lightLevelMap[sunlightLevels[x][y][z]]
                    }
                }
            }
            sunlightChanged = false
        }

        //lightLevelCalculator.calculateLightLevels(blockList, lightLevels)

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
        MeshResult opaqueResult = mesher.meshVoxels(opaque, lightLevels)
        mesher.enableAlpha()
        MeshResult transparentResult = mesher.meshVoxels(transparent, lightLevels)

        opaqueVertexCount = (int)(opaqueResult.vertices.capacity()/3)
        transparentVertexCount = (int)(transparentResult.vertices.capacity()/3)

        opaqueResult.putInVBO(vboVertexHandle, vboColorHandle, vboTextureHandle, vboNormalHandle)
        transparentResult.putInVBO(liquidVboVertexHandle, liquidVboColorHandle, liquidVboTextureHandle, liquidVboNormalHandle)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)

        VoxelGameAPI.instance.eventManager.push(new EventChunkRender(this))
    }

    @Override
    public void render() {
        if(sunlightChanged && !sunlightChanging) {
            rerender()
        }

        VoxelGame.getInstance().getTextureManager().bindTexture(NormalBlockRenderer.blockMap.getTextureID())
        if(vboVertexHandle > 0) {
            glDisable(GL_BLEND)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertexHandle)
            glVertexPointer(3, GL_FLOAT, 0, 0)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTextureHandle)
            glTexCoordPointer(2, GL_FLOAT, 0, 0)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboNormalHandle)
            glNormalPointer(GL_FLOAT, 0, 0)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColorHandle)
            glColorPointer(3, GL_FLOAT, 0, 0)

            glDrawArrays(GL_QUADS, 0, opaqueVertexCount)
        }
    }

    @Override
    public void eachBlock(Closure action) {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < size; z++) {
                    Block blk = blockList[x][y][z]
                    action(blk, x, y, z)
                }
            }
        }
    }

    public void renderWater() {
        if(liquidVboVertexHandle > 0) {
            VoxelGame.instance.shaderManager.setChunkOffset(startPosition.x, startPosition.y, startPosition.z)

            VoxelGame.instance.shaderManager.enableWave()
            glEnable(GL_BLEND)
            glPolygonOffset(1f, 0.7f)
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, liquidVboVertexHandle)
            glVertexPointer(3, GL_FLOAT, 0, 0)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, liquidVboTextureHandle)
            glTexCoordPointer(2, GL_FLOAT, 0, 0)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, liquidVboNormalHandle)
            glNormalPointer(GL_FLOAT, 0, 0)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, liquidVboColorHandle)
            glColorPointer(4, GL_FLOAT, 0, 0)

            glDrawArrays(GL_QUADS, 0, transparentVertexCount)
            glDisable(GL_BLEND)
            VoxelGame.instance.shaderManager.disableWave()
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

        return blockList[x][y][z];
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

        blockList[x][y][z] = null

        addNeighborsToSunlightQueue(x, y, z)

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
            int posZSunlight = posZNeighborChunk.getSunlight(posZNeighborPos.x, posXNeighborPos.y, posXNeighborPos.z)
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
            Block posYBlock = blockList[x][y+1][z]
            if(getSunlight(x, y+1, z) > 1) {
                if (posYBlock == null) {
                    parentWorld.addToSunlightQueue(posYNeighborPos)
                } else if (blockList[x][y + 1][z].isTransparent()) {
                    parentWorld.addToSunlightQueue(posYNeighborPos)
                }
            }
        }
    }

    @Override
    void addBlock(Block block, Vec3i position) {
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
    }

    @Override
    void unload() {
        VoxelGame.getInstance().addToGLQueue(new Runnable() {
            @Override
            public void run() {
                GL15.glDeleteBuffers(vboVertexHandle)
                GL15.glDeleteBuffers(vboColorHandle)
                GL15.glDeleteBuffers(vboNormalHandle)
                GL15.glDeleteBuffers(vboTextureHandle)

                GL15.glDeleteBuffers(liquidVboVertexHandle)
                GL15.glDeleteBuffers(liquidVboColorHandle)
                GL15.glDeleteBuffers(liquidVboNormalHandle)
                GL15.glDeleteBuffers(liquidVboTextureHandle)
            }
        })
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
        int[][][] ints = new int[size][highestPoint+1][size]
        for(int x = 0; x < size; x++) {
            for(int y = 0; y <= highestPoint; y++) {
                for(int z = 0; z < size; z++) {
                    Block b = blockList[x][y][z]
                    if(b == null) {
                        ints[x][y][z] = -1
                    } else {
                        ints[x][y][z] = b.ID
                    }
                }
            }
        }
        return ints
    }

    private void loadIdInts(int[][][] ints) {
        int width = ints.length
        int ht = ints[0].length
        int length = ints[0][0].length
        Block[][][] blocks = new Block[size][height][size]
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < ht; y++) {
                for(int z = 0; z < length; z++) {
                    int id = ints[x][y][z]
                    if(id > -1) {
                        for(Block b : Block.values()) {
                            if(b.ID == id) {
                                highestPoint = Math.max(highestPoint, y)
                                blocks[x][y][z] = b
                            }
                        }
                    }
                }
            }
        }

        this.blockList = blocks
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

        sunlightChanging = true
        sunlightChanged = true
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
}