package sx.lambda.mstojcevich.voxel.client.render.meshing;

import org.lwjgl.BufferUtils;
import sx.lambda.mstojcevich.voxel.VoxelGame;
import sx.lambda.mstojcevich.voxel.block.Block;
import sx.lambda.mstojcevich.voxel.util.Vec3i;
import sx.lambda.mstojcevich.voxel.world.IWorld;
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk;

import java.nio.FloatBuffer;

/**
 * Regular mesher. Gets the job done.
 * Doesn't use any specific algorithms, just culls faces that are next to others.
 */
public class PlainMesher implements Mesher {

    private final IChunk chunk;
    private boolean useAlpha;

    /**
     * @param chunk Chunk that this will be meshing
     */
    public PlainMesher(IChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public MeshResult meshVoxels(Block[][][] voxels, float[][][] lightLevels) {
        int width = voxels.length;
        int height = voxels[0].length;
        int length = voxels[0][0].length;
        boolean[][][] shouldRenderTop = new boolean[width][height][length];
        boolean[][][] shouldRenderBottom = new boolean[width][height][length];
        boolean[][][] shouldRenderLeft = new boolean[width][height][length];
        boolean[][][] shouldRenderRight = new boolean[width][height][length];
        boolean[][][] shouldRenderFront = new boolean[width][height][length];
        boolean[][][] shouldRenderBack = new boolean[width][height][length];

        int numberFaces = calcShouldRender(voxels,
                shouldRenderTop, shouldRenderBottom,
                shouldRenderLeft, shouldRenderRight,
                shouldRenderFront, shouldRenderBack);

        return draw(voxels,
                shouldRenderTop, shouldRenderBottom,
                shouldRenderLeft, shouldRenderRight,
                shouldRenderFront, shouldRenderBack,
                lightLevels, numberFaces);
    }

    @Override
    public void enableAlpha() {
        this.useAlpha = true;
    }

    @Override
    public void disableAlpha() {
        this.useAlpha = false;
    }

    /**
     * @param blockList 3d array of voxels we're meshing
     * @param shouldRenderTop Empty 3d array of booleans, the size of the chunk. Values get changed by this function.
     * @param shouldRenderBottom Empty 3d array of booleans, the size of the chunk. Values get changed by this function.
     * @param shouldRenderLeft Empty 3d array of booleans, the size of the chunk. Values get changed by this function.
     * @param shouldRenderRight Empty 3d array of booleans, the size of the chunk. Values get changed by this function.
     * @param shouldRenderFront Empty 3d array of booleans, the size of the chunk. Values get changed by this function.
     * @param shouldRenderBack Empty 3d array of booleans, the size of the chunk. Values get changed by this function.
     * @return Number of visible faces
     */
    private int calcShouldRender(Block[][][] blockList,
                                boolean[][][] shouldRenderTop, boolean[][][] shouldRenderBottom,
                                boolean[][][] shouldRenderLeft, boolean[][][] shouldRenderRight,
                                boolean[][][] shouldRenderFront, boolean[][][] shouldRenderBack) {
        int width = blockList.length;
        int height = blockList[0].length;
        int length = blockList[0][0].length;
        
        IWorld world = VoxelGame.getInstance().getWorld();

        int visFaceCount = 0;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                for (int y = height - 1; y >= 0; y--) {
                    Block block = blockList[x][y][z];
                    if (block != null) {
                        shouldRenderTop[x][y][z] = true;
                        visFaceCount++;
                        if (z == length - 1) {
                            Vec3i adjPos = new Vec3i(
                                    x, y, 0);
                            Vec3i askWorld = new Vec3i(chunk.getStartPosition().x, y, chunk.getStartPosition().z + length);
                            IChunk adjChunk = world.getChunkAtPosition(askWorld);
                            if (adjChunk != null) {
                                Block adjBlock = adjChunk.getBlockAtPosition(adjPos);
                                if (adjBlock != null) {
                                    if (!adjBlock.isTransparent() || blockList[x][y][z].isTransparent()) {
                                        shouldRenderTop[x][y][z] = false;
                                        visFaceCount--;
                                    }
                                }
                            }
                        } else if (blockList[x][y][z + 1] != null) {
                            if (!blockList[x][y][z + 1].isTransparent() || blockList[x][y][z].isTransparent()) {
                                shouldRenderTop[x][y][z] = false;
                                visFaceCount--;
                            }
                        }

                        shouldRenderLeft[x][y][z] = true;
                        visFaceCount++;
                        if (x == 0) {
                            Vec3i adjPos = new Vec3i(
                                    width - 1, y, z);
                            Vec3i askWorld = new Vec3i(chunk.getStartPosition().x - 1, y, chunk.getStartPosition().z);
                            IChunk adjChunk = world.getChunkAtPosition(askWorld);
                            if (adjChunk != null) {
                                Block adjBlock = adjChunk.getBlockAtPosition(adjPos);
                                if (adjBlock != null) {
                                    if (!adjBlock.isTransparent() || blockList[x][y][z].isTransparent()) {
                                        shouldRenderLeft[x][y][z] = false;
                                        visFaceCount--;
                                    }
                                }
                            }
                        } else if (blockList[x - 1][y][z] != null) {
                            if (!blockList[x - 1][y][z].isTransparent() || blockList[x][y][z].isTransparent()) {
                                shouldRenderLeft[x][y][z] = false;
                                visFaceCount--;
                            }
                        }

                        shouldRenderRight[x][y][z] = true;
                        visFaceCount++;
                        if (x == width - 1) {
                            Vec3i adjPos = new Vec3i(
                                    0, y, z);
                            Vec3i askWorld = new Vec3i(chunk.getStartPosition().x + width, y, chunk.getStartPosition().z);
                            IChunk adjChunk = world.getChunkAtPosition(askWorld);
                            if (adjChunk != null) {
                                Block adjBlock = adjChunk.getBlockAtPosition(adjPos);
                                if (adjBlock != null) {
                                    if (!adjBlock.isTransparent() || blockList[x][y][z].isTransparent()) {
                                        shouldRenderRight[x][y][z] = false;
                                        visFaceCount--;
                                    }
                                }
                            }
                        } else if (blockList[x + 1][y][z] != null) {
                            if (!blockList[x + 1][y][z].isTransparent() || blockList[x][y][z].isTransparent()) {
                                shouldRenderRight[x][y][z] = false;
                                visFaceCount--;
                            }
                        }

                        shouldRenderFront[x][y][z] = true;
                        visFaceCount++;
                        if (y == 0)
                            shouldRenderFront[x][y][z] = true;
                        else if (blockList[x][y - 1][z] != null) {
                            if (!blockList[x][y - 1][z].isTransparent() || blockList[x][y][z].isTransparent()) {
                                shouldRenderFront[x][y][z] = false;
                                visFaceCount--;
                            }
                        }

                        shouldRenderBack[x][y][z] = true;
                        visFaceCount++;
                        if (y == height - 1)
                            shouldRenderBack[x][y][z] = true;
                        else if (blockList[x][y + 1][z] != null) {
                            if (!blockList[x][y + 1][z].isTransparent() || blockList[x][y][z].isTransparent()) {
                                shouldRenderBack[x][y][z] = false;
                                visFaceCount--;
                            }
                        }

                        shouldRenderBottom[x][y][z] = true;
                        visFaceCount++;
                        if (z == 0) {
                            Vec3i adjPos = new Vec3i(
                                    x, y, length - 1);
                            Vec3i askWorld = new Vec3i(chunk.getStartPosition().x, y, chunk.getStartPosition().z - 1);
                            IChunk adjChunk = world.getChunkAtPosition(askWorld);
                            if (adjChunk != null) {
                                Block adjBlock = adjChunk.getBlockAtPosition(adjPos);
                                if (adjBlock != null) {
                                    if (!adjBlock.isTransparent() || blockList[x][y][z].isTransparent()) {
                                        shouldRenderBottom[x][y][z] = false;
                                        visFaceCount--;
                                    }
                                }
                            }
                        } else if (blockList[x][y][z - 1] != null) {
                            if (!blockList[x][y][z - 1].isTransparent() || blockList[x][y][z].isTransparent()) {
                                shouldRenderBottom[x][y][z] = false;
                                visFaceCount--;
                            }
                        }
                    }
                }
            }
        }

        return visFaceCount;
    }

    /**
     *
     * @param blockList 3d array of voxels to draw.
     * @param shouldRenderTop 3d array of booleans, the size of the chunk.
     * @param shouldRenderBottom 3d array of booleans, the size of the chunk.
     * @param shouldRenderLeft 3d array of booleans, the size of the chunk.
     * @param shouldRenderRight 3d array of booleans, the size of the chunk.
     * @param shouldRenderFront 3d array of booleans, the size of the chunk.
     * @param shouldRenderBack 3d array of booleans, the size of the chunk.
     * @param visibleSideCount Number of faces that are visible
     * @return Result of drawing the mesh
     */
    private MeshResult draw(Block[][][] blockList,
                            boolean[][][] shouldRenderTop, boolean[][][] shouldRenderBottom,
                            boolean[][][] shouldRenderLeft, boolean[][][] shouldRenderRight,
                            boolean[][][] shouldRenderFront, boolean[][][] shouldRenderBack,
                            float[][][] lightLevels, int visibleSideCount) {
        FloatBuffer vertexPosData = BufferUtils.createFloatBuffer(visibleSideCount * 4 * 3);
        FloatBuffer textureData = BufferUtils.createFloatBuffer(visibleSideCount*4*2);
        FloatBuffer normalData = BufferUtils.createFloatBuffer(visibleSideCount*4*3);
        FloatBuffer colorData = BufferUtils.createFloatBuffer(visibleSideCount*4*(useAlpha ? 4 : 3));

        int width = blockList.length;
        int height = blockList[0].length;
        int length = blockList[0][0].length;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                boolean firstBlock = false;
                for (int y = height-1; y >= 0; y--) {
                    Block block = blockList[x][y][z];
                    if(block != null) {
                        if(!firstBlock) {
                            firstBlock = true;
                        }
                        block.getRenderer().renderVBO(chunk, x, y, z, lightLevels,
                                vertexPosData, textureData, normalData, colorData,
                                shouldRenderTop[x][y][z], shouldRenderBottom[x][y][z],
                                shouldRenderLeft[x][y][z], shouldRenderRight[x][y][z],
                                shouldRenderFront[x][y][z], shouldRenderBack[x][y][z]);
                    }
                }
            }
        }
        vertexPosData.flip();
        textureData.flip();
        normalData.flip();
        colorData.flip();

        return new MeshResult(vertexPosData, colorData, useAlpha, normalData, textureData);
    }

}
