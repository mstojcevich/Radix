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

    int opaqueVertexCount = 0
    int transparentVertexCount = 0

    float[][][] lightLevels

    public Chunk(IWorld world, Vec3i startPosition, int[][][] ids) {
        this.parentWorld = world
        this.startPosition = startPosition
        this.size = world.getChunkSize()
        this.height = world.getHeight()

        lightLevelCalculator = new SunAndNeighborLLC(this)

        if(VoxelGame.instance != null) { // We're a client
            mesher = new PlainMesher(this)
        } else {
            mesher = null
        }

        this.loadIdInts(ids)

        lightLevels = new float[size][height][size]
        lightLevelCalculator.calculateLightLevels(blockList, lightLevels)
    }

    public Chunk(IWorld world, Vec3i startPosition) {
        this.parentWorld = world
        this.startPosition = startPosition;
        this.size = world.getChunkSize()
        this.height = world.getHeight()

        lightLevelCalculator = new SunAndNeighborLLC(this)

        if(VoxelGame.instance != null) { // We're a client
            mesher = new PlainMesher(this)
        } else {
            mesher = null
        }

        this.blockList = new Block[size][height][size]
        highestPoint = world.chunkGen.generate(startPosition, blockList)

        lightLevels = new float[size][height][size]
        lightLevelCalculator.calculateLightLevels(blockList, lightLevels)
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

        lightLevelCalculator.calculateLightLevels(blockList, lightLevels)

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

}