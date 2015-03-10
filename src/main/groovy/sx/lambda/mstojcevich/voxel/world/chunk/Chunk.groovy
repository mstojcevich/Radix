package sx.lambda.mstojcevich.voxel.world.chunk

import groovy.transform.CompileStatic
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL15
import sx.lambda.mstojcevich.voxel.api.VoxelGameAPI
import sx.lambda.mstojcevich.voxel.api.events.render.EventChunkRender
import sx.lambda.mstojcevich.voxel.block.Block
import sx.lambda.mstojcevich.voxel.block.NormalBlockRenderer
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.util.Vec3i
import sx.lambda.mstojcevich.voxel.world.IWorld

import java.nio.FloatBuffer
import java.nio.IntBuffer

import static org.lwjgl.opengl.GL11.*

@CompileStatic
public class Chunk implements IChunk {

    private final Block[][][] blockList

    private final transient IWorld parentWorld

    private final int size
    private final int height

    private transient int displayList = -1

    private Vec3i startPosition

    private int highestPoint

    private int blockCount
    private transient int numVisibleSides = 0

    private static final boolean USE_VBO = true
    private transient int vboVertexHandle = -1
    private transient int vboTextureHandle = -1
    private transient int vboNormalHandle = -1
    private transient int vboColorHandle = -1

    public Chunk(IWorld world, Vec3i startPosition) {
        this.parentWorld = world
        this.startPosition = startPosition;
        this.size = world.getChunkSize()
        this.height = world.getHeight()
        this.blockList = new Block[size][height][size]

        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                int distFromSeaLevel = world.getHeightAboveSeaLevel(startPosition.x + x, startPosition.z + z)
                int yMax = world.getSeaLevel() + distFromSeaLevel
                blockCount += Math.max(yMax, world.getSeaLevel())
                if(yMax < world.getSeaLevel()) {
                    for (int y = yMax; y < world.getSeaLevel(); y++) {
                        Block blockType = Block.WATER
                        if(y == yMax) {
                            blockType = Block.SAND
                        }
                        blockList[x][y][z] = blockType
                    }
                }
                for (int y = 0; y < yMax; y++) {
                    Block blockType = Block.STONE

                    if(y == world.getSeaLevel() && y == yMax-1) {
                        blockCount++
                        blockList[x][y+1][z] = Block.SAND
                    }

                    if(y == yMax-1) {
                        blockType = Block.GRASS
                    } else if(y > yMax-5) {
                        blockType = Block.DIRT
                    }
                    blockList[x][y][z] = blockType
                    highestPoint = Math.max(highestPoint, y + 1)
                }
            }
        }
    }

    @Override
    public void rerender() {
        if (USE_VBO) {
            if(vboVertexHandle == -1 || vboVertexHandle == 0) {
                IntBuffer buffer = BufferUtils.createIntBuffer(4)
                GL15.glGenBuffers(buffer)
                vboVertexHandle = buffer.get(0)
                vboTextureHandle = buffer.get(1)
                vboNormalHandle = buffer.get(2)
                vboColorHandle = buffer.get(3)

                glEnableClientState(GL_NORMAL_ARRAY)
                glEnableClientState(GL_VERTEX_ARRAY)
                glEnableClientState(GL_TEXTURE_COORD_ARRAY)
                glEnableClientState(GL_COLOR_ARRAY)
            }

            numVisibleSides = 6*blockCount
            boolean[][][] shouldRenderTop = new boolean[size][height][size];
            boolean[][][] shouldRenderBottom = new boolean[size][height][size];
            boolean[][][] shouldRenderLeft = new boolean[size][height][size];
            boolean[][][] shouldRenderRight = new boolean[size][height][size];
            boolean[][][] shouldRenderFront = new boolean[size][height][size];
            boolean[][][] shouldRenderBack = new boolean[size][height][size];
            float[][][] lightLevels = new float[size][height][size];
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    float lightLevel = 1.0f
                    boolean firstBlock = false
                    for (int y = height-1; y >= 0; y--) {
                        Block block = blockList[x][y][z]
                        if (block != null) {
                            if(!firstBlock) {
                                firstBlock = true
                                lightLevel = 1.0f
                            }
                            int zed = z
                            shouldRenderTop[x][y][z] = true
                            if (z == size - 1)
                                shouldRenderTop[x][y][z] = true
                            else if (blockList[x][y][zed + 1] != null) {
                                shouldRenderTop[x][y][z] = false
                                numVisibleSides--
                            }

                            shouldRenderLeft[x][y][z] = true
                            if (x == 0)
                                shouldRenderLeft[x][y][z] = true
                            else if (blockList[x - 1][y][z] != null) {
                                shouldRenderLeft[x][y][z] = false
                                numVisibleSides--
                            }

                            shouldRenderRight[x][y][z] = true
                            if (x == size - 1)
                                shouldRenderRight[x][y][z] = true
                            else if (blockList[x + 1][y][z] != null) {
                                shouldRenderRight[x][y][z] = false
                                numVisibleSides--
                            }

                            shouldRenderFront[x][y][z] = true
                            if (y == 0)
                                shouldRenderFront[x][y][z] = true
                            else if (blockList[x][y - 1][z] != null) {
                                shouldRenderFront[x][y][z] = false
                                numVisibleSides--
                            }

                            shouldRenderBack[x][y][z] = true
                            if (y == height - 1)
                                shouldRenderBack[x][y][z] = true
                            else if (blockList[x][y + 1][z] != null) {
                                shouldRenderBack[x][y][z] = false
                                numVisibleSides--
                            }

                            shouldRenderBottom[x][y][z] = true
                            if (z == 0)
                                shouldRenderBottom[x][y][z] = true
                            else if (blockList[x][y][zed - 1] != null) {
                                shouldRenderBottom[x][y][z] = false
                                numVisibleSides--
                            }

                            lightLevels[x][y][z] = lightLevel
                        } else if(!firstBlock) {
                            lightLevels[x][y][z] = 1.0f
                        } else {
                            lightLevels[x][y][z] = lightLevel
                        }
                        lightLevel *= 0.8f
                    }
                }
            }

            FloatBuffer vertexPosData = BufferUtils.createFloatBuffer(numVisibleSides*4*3)
            FloatBuffer textureData = BufferUtils.createFloatBuffer(numVisibleSides*4*2)
            FloatBuffer normalData = BufferUtils.createFloatBuffer(numVisibleSides*4*3)
            FloatBuffer colorData = BufferUtils.createFloatBuffer(numVisibleSides*4*3)
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    boolean firstBlock = false
                    for (int y = height-1; y >= 0; y--) {
                        Block block = blockList[x][y][z]
                        if(block != null) {
                            if(!firstBlock) {
                                firstBlock = true
                            }
                            block.renderer.renderVBO(x, y, z, lightLevels,
                                    vertexPosData, textureData, normalData, colorData,
                                    shouldRenderTop[x][y][z], shouldRenderBottom[x][y][z],
                                    shouldRenderLeft[x][y][z], shouldRenderRight[x][y][z],
                                    shouldRenderFront[x][y][z], shouldRenderBack[x][y][z])
                        }
                    }
                }
            }
            vertexPosData.flip()
            textureData.flip()
            normalData.flip()
            colorData.flip()

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertexHandle)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexPosData, GL15.GL_STATIC_DRAW)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTextureHandle)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureData, GL15.GL_STATIC_DRAW)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboNormalHandle)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalData, GL15.GL_STATIC_DRAW)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColorHandle)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorData, GL15.GL_STATIC_DRAW)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        } else {
            if (displayList == -1 || displayList == 0) {
                glDeleteLists(displayList, 1)
                displayList = glGenLists(1)
            }

            glNewList(displayList, GL_COMPILE)
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    for (int y = 0; y < height; y++) {
                        Block block = blockList[x][y][z]
                        if (block == null) continue;
                        //block.getRenderer().prerender()

                        boolean shouldRenderTop = true;
                        if (z == size - 1)
                            shouldRenderTop = true;
                        else if (blockList[x][y][z + 1] != null)
                            shouldRenderTop = false

                        boolean shouldRenderLeft = true
                        if (x == 0)
                            shouldRenderLeft = true
                        else if (blockList[x - 1][y][z] != null)
                            shouldRenderLeft = false

                        boolean shouldRenderRight = true
                        if (x == size - 1)
                            shouldRenderRight = true
                        else if (blockList[x + 1][y][z] != null)
                            shouldRenderRight = false

                        boolean shouldRenderFront = true
                        if (y == 0)
                            shouldRenderFront = true
                        else if (blockList[x][y - 1][z] != null)
                            shouldRenderFront = false

                        boolean shouldRenderBack = true
                        if (y == height - 1)
                            shouldRenderBack = true
                        else if (blockList[x][y + 1][z] != null)
                            shouldRenderBack = false

                        boolean shouldRenderBottom = true
                        if (z == 0)
                            shouldRenderBottom = true
                        else if (blockList[x][y][z - 1] != null)
                            shouldRenderBottom = false;

                        block.getRenderer().render(x, y, z,
                                shouldRenderTop, shouldRenderBottom,
                                shouldRenderLeft, shouldRenderRight,
                                shouldRenderFront, shouldRenderBack)
                    }
                }
            }
            glEndList()

            VoxelGameAPI.instance.eventManager.push(new EventChunkRender(this))
        }
    }

    @Override
    public void render() {
        glPushMatrix();

        VoxelGame.getInstance().getTextureManager().bindTexture(NormalBlockRenderer.blockMap.getTextureID())
        if(USE_VBO) {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertexHandle)
            glVertexPointer(3, GL_FLOAT, 0, 0)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTextureHandle)
            glTexCoordPointer(2, GL_FLOAT, 0, 0)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboNormalHandle)
            glNormalPointer(GL_FLOAT, 0, 0)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColorHandle)
            glColorPointer(3, GL_FLOAT, 0, 0)

            glDrawArrays(GL_QUADS, 0, numVisibleSides*4)
        } else {
            glCallList(displayList)
        }

        glPopMatrix();
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

        blockCount--
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
            highestPoint = Math.max(highestPoint, y + 1)
        }

        blockCount++
    }

    @Override
    void unload() {
        VoxelGame.getInstance().addToGLQueue(new Runnable() {
            @Override
            public void run() {
                glDeleteLists(displayList, 1);
            }
        })
    }

    @Override
    public Vec3i getStartPosition() {
        this.startPosition
    }

    @Override
    public float getHighestPoint() { highestPoint }

}