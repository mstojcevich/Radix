package sx.lambda.mstojcevich.voxel.world.chunk

import groovy.transform.CompileStatic
import sx.lambda.mstojcevich.voxel.block.Block
import sx.lambda.mstojcevich.voxel.block.NormalBlockRenderer
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.util.Vec3i
import sx.lambda.mstojcevich.voxel.world.IWorld

import static org.lwjgl.opengl.GL11.*

@CompileStatic
public class Chunk implements IChunk {

    private final Block[][][] blockList

    private final transient IWorld parentWorld

    private final int size
    private final int height

    private transient int displayList = -1

    private Vec3i startPosition;

    private int highestPoint

    public Chunk(IWorld world, Vec3i startPosition) {
        this.parentWorld = world
        this.startPosition = startPosition;
        this.size = world.getChunkSize()
        this.height = world.getHeight()
        this.blockList = new Block[size][height][size]

        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                int yMax = world.getSeaLevel() + world.getHeightAboveSeaLevel(startPosition.x + x, startPosition.z + z);
                for (int y = 0; y < yMax; y++) {
                    blockList[x][y][z] = Block.STONE //Set to stone by default
                    highestPoint = Math.max(highestPoint, y + 1)
                }
            }
        }
    }

    @Override
    public void rerender() {
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


    }

    @Override
    public void render() {
        glPushMatrix();

        VoxelGame.getInstance().getTextureManager().bindTexture(NormalBlockRenderer.blockMap.getTextureID())
        glCallList(displayList)

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