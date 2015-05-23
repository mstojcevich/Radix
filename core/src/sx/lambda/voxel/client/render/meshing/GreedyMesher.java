package sx.lambda.voxel.client.render.meshing;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import sx.lambda.voxel.api.VoxelGameAPI;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.block.IBlockRenderer;
import sx.lambda.voxel.block.Side;
import sx.lambda.voxel.world.chunk.IChunk;

import java.util.ArrayList;
import java.util.List;

public class GreedyMesher implements Mesher {

    private final IChunk chunk;
    private boolean useAlpha;

    /**
     * @param chunk Chunk to mesh
     */
    public GreedyMesher(IChunk chunk) {
        this.chunk = chunk;
    }


    @Override
    public Mesh meshVoxels(MeshBuilder builder, Block[][][] voxels, float[][][] lightLevels) {
        List<Face> faces = new ArrayList<>();

        // Top, bottom
        for (int y = 0; y < voxels[0].length; y++) {
            Block[][] topBlocks = new Block[voxels.length][voxels[0][0].length];
            float[][] topLightLevels = new float[voxels.length][voxels[0][0].length];
            Block[][] btmBlocks = new Block[voxels.length][voxels[0][0].length];
            float[][] btmLightLevels = new float[voxels.length][voxels[0][0].length];
            for (int x = 0; x < voxels.length; x++) {
                for (int z = 0; z < voxels[0][0].length; z++) {
                    if (voxels[x][y][z] == null) continue;
                    if (y < voxels[0].length - 1) {
                        if (voxels[x][y + 1][z] == null) {
                            topBlocks[x][z] = voxels[x][y][z];
                            topLightLevels[x][z] = lightLevels[x][y + 1][z];
                        } else if (voxels[x][y + 1][z].isTransparent() && !voxels[x][y][z].isTransparent()) {
                            topBlocks[x][z] = voxels[x][y][z];
                            topLightLevels[x][z] = lightLevels[x][y + 1][z];
                        }
                    } else {
                        topBlocks[x][z] = voxels[x][y][z];
                        topLightLevels[x][z] = 1;
                    }
                    if (y > 0) {
                        if (voxels[x][y - 1][z] == null) {
                            btmBlocks[x][z] = voxels[x][y][z];
                            btmLightLevels[x][z] = lightLevels[x][y - 1][z];
                        } else if (voxels[x][y - 1][z].isTransparent() && !voxels[x][y][z].isTransparent()) {
                            btmBlocks[x][z] = voxels[x][y][z];
                            btmLightLevels[x][z] = lightLevels[x][y - 1][z];
                        }
                    } else {
                        btmLightLevels[x][z] = 1;
                        continue;
                    }
                }
            }
            greedy(faces, Side.TOP, topBlocks, topLightLevels, y + chunk.getStartPosition().y, chunk.getStartPosition().x, chunk.getStartPosition().z);
            greedy(faces, Side.BOTTOM, btmBlocks, btmLightLevels, y + chunk.getStartPosition().y, chunk.getStartPosition().x, chunk.getStartPosition().z);
        }

        // East, west
        for (int x = 0; x < voxels.length; x++) {
            Block[][] westBlocks = new Block[voxels[0][0].length][voxels[0].length];
            float[][] westLightLevels = new float[voxels[0][0].length][voxels[0].length];
            Block[][] eastBlocks = new Block[voxels[0][0].length][voxels[0].length];
            float[][] eastLightLevels = new float[voxels[0][0].length][voxels[0].length];
            for (int z = 0; z < voxels[0][0].length; z++) {
                for (int y = 0; y < voxels[0].length; y++) {
                    if (voxels[x][y][z] == null) continue;

                    int westNeighborX = chunk.getStartPosition().x + x - 1;
                    IChunk westNeighborChunk = chunk.getWorld().getChunkAtPosition(westNeighborX, chunk.getStartPosition().z + z);
                    if (westNeighborChunk != null) {
                        Block westNeighborBlk = VoxelGameAPI.instance.getBlockByID(
                                westNeighborChunk.getBlockIdAtPosition(westNeighborX, y, z));
                        if (westNeighborBlk == null) {
                            westBlocks[z][y] = voxels[x][y][z];
                            westLightLevels[z][y] = westNeighborChunk.getLightLevel(westNeighborX, y, z);
                        } else if (westNeighborBlk.isTransparent() && !voxels[x][y][z].isTransparent()) {
                            westBlocks[z][y] = voxels[x][y][z];
                            westLightLevels[z][y] = westNeighborChunk.getLightLevel(westNeighborX, y, z);
                        }
                    } else {
                        westLightLevels[z][y] = 1;
                        continue;
                    }

                    int eastNeighborX = chunk.getStartPosition().x + x + 1;
                    IChunk eastNeighborChunk = chunk.getWorld().getChunkAtPosition(eastNeighborX, chunk.getStartPosition().z + z);
                    if (eastNeighborChunk != null) {
                        Block eastNeighborBlk = VoxelGameAPI.instance.getBlockByID(
                                eastNeighborChunk.getBlockIdAtPosition(eastNeighborX, y, z));
                        if (eastNeighborBlk == null) {
                            eastBlocks[z][y] = voxels[x][y][z];
                            eastLightLevels[z][y] = eastNeighborChunk.getLightLevel(eastNeighborX, y, z);
                        } else if (eastNeighborBlk.isTransparent() && !voxels[x][y][z].isTransparent()) {
                            eastBlocks[z][y] = voxels[x][y][z];
                            eastLightLevels[z][y] = eastNeighborChunk.getLightLevel(eastNeighborX, y, z);
                        }
                    } else {
                        eastLightLevels[z][y] = 1;
                        continue;
                    }
                }
            }

            greedy(faces, Side.EAST, eastBlocks, eastLightLevels, x + chunk.getStartPosition().x, chunk.getStartPosition().z, chunk.getStartPosition().y);
            greedy(faces, Side.WEST, westBlocks, westLightLevels, x + chunk.getStartPosition().x, chunk.getStartPosition().z, chunk.getStartPosition().y);
        }

        // North, south
        for (int z = 0; z < voxels[0][0].length; z++) {
            Block[][] northBlocks = new Block[voxels.length][voxels[0].length];
            float[][] northLightLevels = new float[voxels.length][voxels[0].length];
            Block[][] southBlocks = new Block[voxels.length][voxels[0].length];
            float[][] southLightLevels = new float[voxels.length][voxels[0].length];
            for (int x = 0; x < voxels.length; x++) {
                for (int y = 0; y < voxels[0].length; y++) {
                    if (voxels[x][y][z] == null) continue;
                    int northNeighborZ = chunk.getStartPosition().z + z + 1;
                    int southNeighborZ = chunk.getStartPosition().z + z - 1;
                    IChunk northNeighborChunk = chunk.getWorld().getChunkAtPosition(chunk.getStartPosition().x + x, northNeighborZ);
                    IChunk southNeighborChunk = chunk.getWorld().getChunkAtPosition(chunk.getStartPosition().x + x, southNeighborZ);

                    if (northNeighborChunk != null) {
                        Block northNeighborBlock = VoxelGameAPI.instance.getBlockByID(
                                northNeighborChunk.getBlockIdAtPosition(x, y, northNeighborZ));
                        if (northNeighborBlock == null) {
                            northBlocks[x][y] = voxels[x][y][z];
                            northLightLevels[x][y] = northNeighborChunk.getLightLevel(x, y, northNeighborZ);
                        } else if (northNeighborBlock.isTransparent() && !voxels[x][y][z].isTransparent()) {
                            northBlocks[x][y] = voxels[x][y][z];
                            northLightLevels[x][y] = northNeighborChunk.getLightLevel(x, y, northNeighborZ);
                        }
                    } else {
                        northBlocks[x][y] = voxels[x][y][z];
                        northLightLevels[x][y] = 1;
                    }

                    if (southNeighborChunk != null) {
                        Block southNeighborBlock = VoxelGameAPI.instance.getBlockByID(
                                southNeighborChunk.getBlockIdAtPosition(x, y, southNeighborZ));
                        if (southNeighborBlock == null) {
                            southBlocks[x][y] = voxels[x][y][z];
                            southLightLevels[x][y] = southNeighborChunk.getLightLevel(x, y, southNeighborZ);
                        } else if (southNeighborBlock.isTransparent() && !voxels[x][y][z].isTransparent()) {
                            southBlocks[x][y] = voxels[x][y][z];
                            southLightLevels[x][y] = southNeighborChunk.getLightLevel(x, y, southNeighborZ);
                        }
                    } else {
                        southLightLevels[x][y] = 1;
                        continue;
                    }
                }
            }

            greedy(faces, Side.NORTH, northBlocks, northLightLevels, z + chunk.getStartPosition().z, chunk.getStartPosition().x, chunk.getStartPosition().y);
            greedy(faces, Side.SOUTH, southBlocks, southLightLevels, z + chunk.getStartPosition().z, chunk.getStartPosition().x, chunk.getStartPosition().y);
        }

        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal, GL20.GL_TRIANGLES);
        for (Face f : faces) {
            f.render(builder);
        }
        return builder.end();
    }

    /**
     * @param outputList List to put faces in
     * @param side       Side being meshed
     * @param blks       Blocks on the plane
     * @param lls        Light levels of the blocks
     * @param z          Depth on the plane
     */
    private void greedy(List<Face> outputList, Side side, Block[][] blks, float lls[][], int z, int offsetX, int offsetY) {
        int width = blks.length;
        int height = blks[0].length;
        boolean[][] used = new boolean[blks.length][blks[0].length];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Block blk = blks[x][y];
                float ll = lls[x][y];
                if (!used[x][y] && blk != null) {
                    used[x][y] = true;
                    int endX = x + 1;
                    int endY = y + 1;
                    while (true) {
                        int newX = endX;
                        if (newX == blks.length) {
                            break;
                        }
                        Block newBlk = blks[newX][y];
                        float newll = lls[newX][y];
                        if (newBlk == blk && newll == ll && !used[newX][y]) {
                            endX++;
                            used[newX][y] = true;
                        } else {
                            while (true) {
                                if (endY == height) break;
                                boolean allPassed = true;
                                for (int lx = x; lx < endX; lx++) {
                                    if (blks[lx][endY] != blk || lls[lx][endY] != ll || used[lx][endY]) {
                                        allPassed = false;
                                    }
                                }
                                if (allPassed) {
                                    for (int lx = x; lx < endX; lx++) {
                                        used[lx][endY] = true;
                                    }
                                } else {
                                    break;
                                }
                                endY++;
                            }
                            break;
                        }
                    }
                    outputList.add(new Face(side, blk, ll, x + offsetX, y + offsetY, endX + offsetX, endY + offsetY, z));
                }
            }
        }
    }

    @Override
    public void enableAlpha() {
        this.useAlpha = true;
    }

    @Override
    public void disableAlpha() {
        this.useAlpha = false;
    }

    private static class Face {
        private final Side side;
        private final int x1, y1, x2, y2, z;
        private final Block block;
        private final float lightLevel;

        public Face(Side side, Block block, float lightLevel, int startX, int startY, int endX, int endY, int z) {
            this.block = block;
            this.lightLevel = lightLevel;
            this.x1 = startX;
            this.y1 = startY;
            this.x2 = endX;
            this.y2 = endY;
            this.z = z;
            this.side = side;
        }

        public void render(MeshBuilder builder) {
            IBlockRenderer renderer = block.getRenderer();

            switch (side) {
                case TOP:
                    renderer.renderTop(x1, y1, x2, y2, z + 1, lightLevel, builder);
                    break;
                case BOTTOM:
                    renderer.renderBottom(x1, y1, x2, y2, z, lightLevel, builder);
                    break;
                case NORTH:
                    renderer.renderNorth(x1, y1, x2, y2, z + 1, lightLevel, builder);
                    break;
                case SOUTH:
                    renderer.renderSouth(x1, y1, x2, y2, z, lightLevel, builder);
                    break;
                case EAST:
                    renderer.renderEast(x1, y1, x2, y2, z + 1, lightLevel, builder);
                    break;
                case WEST:
                    renderer.renderWest(x1, y1, x2, y2, z, lightLevel, builder);
                    break;

            }
        }
    }
}
