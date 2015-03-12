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

    private Block[][][] blockList

    private transient IWorld parentWorld

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

    private int liquidBlockCount
    private transient int liquidVisibleSides = 0
    private transient int liquidVboVertexHandle = -1
    private transient int liquidVboTextureHandle = -1
    private transient int liquidVboNormalHandle = -1
    private transient int liquidVboColorHandle = -1

    float[][][] lightLevels

    public Chunk(IWorld world, Vec3i startPosition, int[][][] ids) {
        this.parentWorld = world
        this.startPosition = startPosition
        this.size = world.getChunkSize()
        this.height = world.getHeight()

        this.loadIdInts(ids)

        lightLevels = new float[size][height][size]
        calcLightLevels(lightLevels)
    }

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
                highestPoint = Math.max(highestPoint, world.getSeaLevel())
                if(yMax < world.getSeaLevel()) {
                    for (int y = yMax; y < world.getSeaLevel(); y++) {
                        Block blockType = Block.WATER
                        if(y == yMax) {
                            blockType = Block.SAND
                            blockCount++
                        } else {
                            liquidBlockCount++
                        }
                        blockList[x][y][z] = blockType
                    }
                }
                for (int y = 0; y < yMax; y++) {
                    Block blockType = Block.STONE

                    if(y == world.getSeaLevel()-1 && y == yMax-1) {
                        blockCount++
                        blockList[x][y+1][z] = Block.SAND
                    }

                    if(y == yMax-1) {
                        blockType = Block.GRASS
                    } else if(y > yMax-5) {
                        blockType = Block.DIRT
                    }
                    blockCount++
                    blockList[x][y][z] = blockType
                    highestPoint = Math.max(highestPoint, y + 1)
                }
            }
        }

        lightLevels = new float[size][height][size]
        calcLightLevels(lightLevels)
    }

    @Override
    public void rerender() {
        if(this.parentWorld == null) {
            if(VoxelGame.instance != null) {
                this.parentWorld = VoxelGame.instance.world
            }
        }

        if (USE_VBO) {
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

            calcLightLevels(lightLevels)
            drawBlocksVbo(lightLevels)
            drawLiquidsVbo(lightLevels)

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
        VoxelGame.getInstance().getTextureManager().bindTexture(NormalBlockRenderer.blockMap.getTextureID())
        if(USE_VBO) {
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

                glDrawArrays(GL_QUADS, 0, numVisibleSides * 4)
            }
        } else {
            glCallList(displayList)
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

            glDrawArrays(GL_QUADS, 0, liquidVisibleSides * 4)
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

        Block block = blockList[x][y][z]
        if(block == Block.WATER) {
            liquidBlockCount--
        } else {
            blockCount--
        }

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
            highestPoint = Math.max(highestPoint, y + 1)
        }

        if(block == Block.WATER) {
            liquidBlockCount++
        } else {
            blockCount++
        }
    }

    @Override
    void unload() {
        VoxelGame.getInstance().addToGLQueue(new Runnable() {
            @Override
            public void run() {
                if(Chunk.USE_VBO) {
                    GL15.glDeleteBuffers(vboVertexHandle)
                    GL15.glDeleteBuffers(vboColorHandle)
                    GL15.glDeleteBuffers(vboNormalHandle)
                    GL15.glDeleteBuffers(vboTextureHandle)

                    GL15.glDeleteBuffers(liquidVboVertexHandle)
                    GL15.glDeleteBuffers(liquidVboColorHandle)
                    GL15.glDeleteBuffers(liquidVboNormalHandle)
                    GL15.glDeleteBuffers(liquidVboTextureHandle)
                } else {
                    glDeleteLists(displayList, 1);
                }
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

    private void drawBlocksVbo(float[][][] lightLevels) {
        numVisibleSides = 6*blockCount

        boolean[][][] shouldRenderTop = new boolean[size][height][size];
        boolean[][][] shouldRenderBottom = new boolean[size][height][size];
        boolean[][][] shouldRenderLeft = new boolean[size][height][size];
        boolean[][][] shouldRenderRight = new boolean[size][height][size];
        boolean[][][] shouldRenderFront = new boolean[size][height][size];
        boolean[][][] shouldRenderBack = new boolean[size][height][size];

        numVisibleSides = calcShouldRender({block -> block != Block.WATER}, numVisibleSides,
                shouldRenderTop, shouldRenderBottom,
                shouldRenderLeft, shouldRenderRight,
                shouldRenderFront, shouldRenderBack)

        renderBlocks({block -> block != Block.WATER}, numVisibleSides, false, lightLevels,
                shouldRenderTop, shouldRenderBottom,
                shouldRenderLeft, shouldRenderRight,
                shouldRenderFront, shouldRenderBack,
                vboVertexHandle, vboTextureHandle, vboNormalHandle, vboColorHandle)
    }

    private void drawLiquidsVbo(float[][][] lightLevels) {
        liquidVisibleSides = 6*liquidBlockCount

        boolean[][][] shouldRenderTop = new boolean[size][height][size];
        boolean[][][] shouldRenderBottom = new boolean[size][height][size];
        boolean[][][] shouldRenderLeft = new boolean[size][height][size];
        boolean[][][] shouldRenderRight = new boolean[size][height][size];
        boolean[][][] shouldRenderFront = new boolean[size][height][size];
        boolean[][][] shouldRenderBack = new boolean[size][height][size];

        liquidVisibleSides = calcShouldRender({block -> block == Block.WATER}, liquidVisibleSides,
                shouldRenderTop, shouldRenderBottom,
                shouldRenderLeft, shouldRenderRight,
                shouldRenderFront, shouldRenderBack)

        renderBlocks({block -> block == Block.WATER}, liquidVisibleSides, true, lightLevels,
                shouldRenderTop, shouldRenderBottom,
                shouldRenderLeft, shouldRenderRight,
                shouldRenderFront, shouldRenderBack,
                liquidVboVertexHandle, liquidVboTextureHandle, liquidVboNormalHandle, liquidVboColorHandle)
    }

    private void calcLightLevels(float[][][] lightLevels) {
        // First pass, sunlight
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                float lightLevel = 1.0f
                boolean firstBlock = false
                for (int y = height - 1; y >= 0; y--) {
                    Block block = blockList[x][y][z]
                    if (block != null) {
                        if (!firstBlock) {
                            firstBlock = true
                        }
                        lightLevels[x][y][z] = lightLevel
                    } else if (!firstBlock) {
                        lightLevels[x][y][z] = 1.0f
                        lightLevel = 1.0f
                    } else {
                        lightLevels[x][y][z] = lightLevel
                    }
                    lightLevel *= 0.8f
                }
            }
        }

        // Second pass, neighbors
        for(int i = 0; i < 2; i++) {
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    for (int y = height - 1; y >= 0; y--) {
                        if (blockList[x][y][z] == null) {
                            float current = lightLevels[x][y][z]

                            float brightestNeighbor = current

                            if (x > 0) {
                                if (blockList[x - 1][y][z] == null) {
                                    brightestNeighbor = Math.max(brightestNeighbor, lightLevels[x - 1][y][z])
                                }
                            } else {
                                int askx = startPosition.x - 1 - x
                                int asky = startPosition.y
                                int askz = startPosition.z
                                Vec3i askWorld = new Vec3i(askx, asky, askz)
                                IChunk chunk = parentWorld.getChunkAtPosition(askWorld)
                                if (chunk != null) {
                                    brightestNeighbor = Math.max(brightestNeighbor, chunk.getLightLevel(size - 1 - x, y, z))
                                }
                            }

                            if (x < size - 1) {
                                if (blockList[x + 1][y][z] == null) {
                                    brightestNeighbor = Math.max(brightestNeighbor, lightLevels[x + 1][y][z])
                                }
                            } else {
                                int askx = startPosition.x + size
                                int asky = startPosition.y
                                int askz = startPosition.z
                                Vec3i askWorld = new Vec3i(askx, asky, askz)
                                IChunk chunk = parentWorld.getChunkAtPosition(askWorld)
                                if (chunk != null) {
                                    brightestNeighbor = Math.max(brightestNeighbor, chunk.getLightLevel(x - (size - 1), y, z))
                                }
                            }

                            if (z > 0) {
                                if (blockList[x][y][z - 1] == null) {
                                    brightestNeighbor = Math.max(brightestNeighbor, lightLevels[x][y][z - 1])
                                }
                            } else {
                                int askx = startPosition.x
                                int asky = startPosition.y
                                int askz = startPosition.z - 1 - z
                                Vec3i askWorld = new Vec3i(askx, asky, askz)
                                IChunk chunk = parentWorld.getChunkAtPosition(askWorld)
                                if (chunk != null) {
                                    brightestNeighbor = Math.max(brightestNeighbor, chunk.getLightLevel(x, y, size - 1 - z))
                                }
                            }

                            if (z < size - 1) {
                                if (blockList[x][y][z + 1] == null) {
                                    brightestNeighbor = Math.max(brightestNeighbor, lightLevels[x][y][z + 1])
                                }
                            } else {
                                int askx = startPosition.x
                                int asky = startPosition.y
                                int askz = startPosition.z + size
                                Vec3i askWorld = new Vec3i(askx, asky, askz)
                                IChunk chunk = parentWorld.getChunkAtPosition(askWorld)
                                if (chunk != null) {
                                    brightestNeighbor = Math.max(brightestNeighbor, chunk.getLightLevel(x, y, z - (size - 1)))
                                }
                            }

                            float targetBrightness = brightestNeighbor * 0.8
                            if (targetBrightness > current) {
                                lightLevels[x][y][z] = targetBrightness
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * @param condition Check to see if block should be rendered
     * @param visibleSideCount Number of sides on target blocks
     * @param hasAlpha Whether you use the alpha channel when setting colors
     * @return New number of visible sides
     */
    private int calcShouldRender(Closure condition, int visibleSideCount,
                                 boolean[][][] shouldRenderTop, boolean[][][] shouldRenderBottom,
                                 boolean[][][] shouldRenderLeft, boolean[][][] shouldRenderRight,
                                 boolean[][][] shouldRenderFront, boolean[][][] shouldRenderBack) {
        int newVisSideCount = visibleSideCount
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                for (int y = height - 1; y >= 0; y--) {
                    Block block = blockList[x][y][z]
                    if (!condition.call(block)) continue;
                    if (block != null) {
                        int zed = z
                        shouldRenderTop[x][y][z] = true
                        if (z == size - 1) {
                            Vec3i adjPos = new Vec3i(
                                    x, y, 0)
                            Vec3i askWorld = new Vec3i(startPosition.x, y, startPosition.z + size)
                            IChunk adjChunk = parentWorld.getChunkAtPosition(askWorld)
                            if (adjChunk != null) {
                                Block adjBlock = adjChunk.getBlockAtPosition(adjPos)
                                if (adjBlock != null) {
                                    if (!adjBlock.isTransparent() || blockList[x][y][z].isTransparent()) {
                                        shouldRenderTop[x][y][z] = false
                                        newVisSideCount--
                                    }
                                }
                            }
                        } else if (blockList[x][y][zed + 1] != null) {
                            if (!blockList[x][y][zed + 1].isTransparent() || blockList[x][y][z].isTransparent()) {
                                shouldRenderTop[x][y][z] = false
                                newVisSideCount--
                            }
                        }

                        shouldRenderLeft[x][y][z] = true
                        if (x == 0) {
                            Vec3i adjPos = new Vec3i(
                                    size - 1, y, z)
                            Vec3i askWorld = new Vec3i(startPosition.x - 1, y, startPosition.z)
                            IChunk adjChunk = parentWorld.getChunkAtPosition(askWorld)
                            if (adjChunk != null) {
                                Block adjBlock = adjChunk.getBlockAtPosition(adjPos)
                                if (adjBlock != null) {
                                    if (!adjBlock.isTransparent() || blockList[x][y][z].isTransparent()) {
                                        shouldRenderLeft[x][y][z] = false
                                        newVisSideCount--
                                    }
                                }
                            }
                        } else if (blockList[x - 1][y][z] != null) {
                            if (!blockList[x - 1][y][z].isTransparent() || blockList[x][y][z].isTransparent()) {
                                shouldRenderLeft[x][y][z] = false
                                newVisSideCount--
                            }
                        }

                        shouldRenderRight[x][y][z] = true
                        if (x == size - 1) {
                            Vec3i adjPos = new Vec3i(
                                    0, y, z)
                            Vec3i askWorld = new Vec3i(startPosition.x + size, y, startPosition.z)
                            IChunk adjChunk = parentWorld.getChunkAtPosition(askWorld)
                            if (adjChunk != null) {
                                Block adjBlock = adjChunk.getBlockAtPosition(adjPos)
                                if (adjBlock != null) {
                                    if (!adjBlock.isTransparent() || blockList[x][y][z].isTransparent()) {
                                        shouldRenderRight[x][y][z] = false
                                        newVisSideCount--
                                    }
                                }
                            }
                        } else if (blockList[x + 1][y][z] != null) {
                            if (!blockList[x + 1][y][z].isTransparent() || blockList[x][y][z].isTransparent()) {
                                shouldRenderRight[x][y][z] = false
                                newVisSideCount--
                            }
                        }

                        shouldRenderFront[x][y][z] = true
                        if (y == 0)
                            shouldRenderFront[x][y][z] = true
                        else if (blockList[x][y - 1][z] != null) {
                            if (!blockList[x][y - 1][z].isTransparent() || blockList[x][y][z].isTransparent()) {
                                shouldRenderFront[x][y][z] = false
                                newVisSideCount--
                            }
                        }

                        shouldRenderBack[x][y][z] = true
                        if (y == height - 1)
                            shouldRenderBack[x][y][z] = true
                        else if (blockList[x][y + 1][z] != null) {
                            if (!blockList[x][y + 1][z].isTransparent() || blockList[x][y][z].isTransparent()) {
                                shouldRenderBack[x][y][z] = false
                                newVisSideCount--
                            }
                        }

                        shouldRenderBottom[x][y][z] = true
                        if (z == 0) {
                            Vec3i adjPos = new Vec3i(
                                    x, y, size - 1)
                            Vec3i askWorld = new Vec3i(startPosition.x, y, startPosition.z - 1)
                            IChunk adjChunk = parentWorld.getChunkAtPosition(askWorld)
                            if (adjChunk != null) {
                                Block adjBlock = adjChunk.getBlockAtPosition(adjPos)
                                if (adjBlock != null) {
                                    if (!adjBlock.isTransparent() || blockList[x][y][z].isTransparent()) {
                                        shouldRenderBottom[x][y][z] = false
                                        newVisSideCount--
                                    }
                                }
                            }
                        } else if (blockList[x][y][zed - 1] != null) {
                            if (!blockList[x][y][zed - 1].isTransparent() || blockList[x][y][z].isTransparent()) {
                                shouldRenderBottom[x][y][z] = false
                                newVisSideCount--
                            }
                        }
                    }
                }
            }
        }

        return newVisSideCount
    }

    public void renderBlocks(Closure condition, int visibleSideCount, boolean useAlpha, float[][][] lightLevels,
                             boolean[][][] shouldRenderTop, boolean[][][] shouldRenderBottom,
                             boolean[][][] shouldRenderLeft, boolean[][][] shouldRenderRight,
                             boolean[][][] shouldRenderFront, boolean[][][] shouldRenderBack,
                             int vertexVbo, int textureVbo, int normalVbo, int colorVbo) {
        FloatBuffer vertexPosData = BufferUtils.createFloatBuffer(visibleSideCount*4*3)
        FloatBuffer textureData = BufferUtils.createFloatBuffer(visibleSideCount*4*2)
        FloatBuffer normalData = BufferUtils.createFloatBuffer(visibleSideCount*4*3)
        FloatBuffer colorData = BufferUtils.createFloatBuffer(visibleSideCount*4*(useAlpha ? 4 : 3))

        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                boolean firstBlock = false
                for (int y = height-1; y >= 0; y--) {
                    Block block = blockList[x][y][z]
                    if(!condition.call(block))continue;
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

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexPosData, GL15.GL_STATIC_DRAW)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, textureVbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureData, GL15.GL_STATIC_DRAW)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalVbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalData, GL15.GL_STATIC_DRAW)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorData, GL15.GL_STATIC_DRAW)
    }

    @Override
    public int[][][] blocksToIdInt() {
        blockCount = 0
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
                                if(b == Block.WATER) {
                                    liquidBlockCount++
                                } else {
                                    blockCount++
                                }
                            }
                        }
                    }
                }
            }
        }

        this.blockList = blocks
    }

}