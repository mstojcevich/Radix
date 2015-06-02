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


// TODO not meshing the chunk sides causes holes after the neighbor chunk is added.
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
    public Mesh meshVoxels(MeshBuilder builder, Block[][][] voxels, short[][][] metadata, float[][][] lightLevels) {
        List<Face> faces = getFaces(voxels, metadata, lightLevels);

        return meshFaces(faces, builder);
    }

    public List<Face> getFaces(Block[][][] voxels, short[][][] metadata, float[][][] lightLevels, OccludeCondition ocCond) {
        List<Face> faces = new ArrayList<>();

        // Top, bottom
        for (int y = 0; y < voxels[0].length; y++) {
            int[][] topBlocks = new int[voxels.length][voxels[0][0].length];
            short[][] topMeta = new short[voxels.length][voxels[0][0].length];
            float[][] topLightLevels = new float[voxels.length][voxels[0][0].length];
            int[][] btmBlocks = new int[voxels.length][voxels[0][0].length];
            short[][] btmMeta = new short[voxels.length][voxels[0][0].length];
            float[][] btmLightLevels = new float[voxels.length][voxels[0][0].length];
            for (int x = 0; x < voxels.length; x++) {
                for (int z = 0; z < voxels[0][0].length; z++) {
                    Block curBlock = voxels[x][y][z];
                    if (curBlock == null) continue;
                    if (y < voxels[0].length - 1) {
                        if (!ocCond.shouldOcclude(curBlock, voxels[x][y + 1][z])) {
                            topBlocks[x][z] = curBlock.getID();
                            topMeta[x][z] = metadata[x][y][z];
                            topLightLevels[x][z] = lightLevels[x][y + 1][z];
                        }
                    } else {
                        topBlocks[x][z] = curBlock.getID();
                        topLightLevels[x][z] = 1;
                    }
                    if (y > 0) {
                        if(!ocCond.shouldOcclude(curBlock, voxels[x][y - 1][z])) {
                            btmBlocks[x][z] = curBlock.getID();
                            btmMeta[x][z] = metadata[x][y][z];
                            btmLightLevels[x][z] = lightLevels[x][y - 1][z];
                        }
                    } else {
                        btmLightLevels[x][z] = 1;
                        continue;
                    }
                }
            }
            greedy(faces, Side.TOP, topBlocks, topMeta, topLightLevels, y + chunk.getStartPosition().y, chunk.getStartPosition().x, chunk.getStartPosition().z);
            greedy(faces, Side.BOTTOM, btmBlocks, btmMeta, btmLightLevels, y + chunk.getStartPosition().y, chunk.getStartPosition().x, chunk.getStartPosition().z);
        }

        // East, west
        for (int x = 0; x < voxels.length; x++) {
            int[][] westBlocks = new int[voxels[0][0].length][voxels[0].length];
            short[][] westMeta = new short[voxels[0][0].length][voxels[0].length];
            float[][] westLightLevels = new float[voxels[0][0].length][voxels[0].length];
            int[][] eastBlocks = new int[voxels[0][0].length][voxels[0].length];
            short[][] eastMeta = new short[voxels[0][0].length][voxels[0].length];
            float[][] eastLightLevels = new float[voxels[0][0].length][voxels[0].length];
            for (int z = 0; z < voxels[0][0].length; z++) {
                for (int y = 0; y < voxels[0].length; y++) {
                    Block curBlock = voxels[x][y][z];
                    if (curBlock == null) continue;

                    int westNeighborX = chunk.getStartPosition().x + x - 1;
                    IChunk westNeighborChunk = chunk.getWorld().getChunkAtPosition(westNeighborX, chunk.getStartPosition().z + z);
                    if (westNeighborChunk != null) {
                        Block westNeighborBlk = VoxelGameAPI.instance.getBlockByID(
                                westNeighborChunk.getBlockIdAtPosition(westNeighborX, y, z));
                        if (!ocCond.shouldOcclude(curBlock, westNeighborBlk)) {
                            westBlocks[z][y] = curBlock.getID();
                            westMeta[z][y] = metadata[x][y][z];
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
                        if (!ocCond.shouldOcclude(curBlock, eastNeighborBlk)) {
                            eastBlocks[z][y] = curBlock.getID();
                            eastMeta[z][y] = metadata[x][y][z];
                            eastLightLevels[z][y] = eastNeighborChunk.getLightLevel(eastNeighborX, y, z);
                        }
                    } else {
                        eastLightLevels[z][y] = 1;
                        continue;
                    }
                }
            }

            greedy(faces, Side.EAST, eastBlocks, eastMeta, eastLightLevels, x + chunk.getStartPosition().x, chunk.getStartPosition().z, chunk.getStartPosition().y);
            greedy(faces, Side.WEST, westBlocks, westMeta, westLightLevels, x + chunk.getStartPosition().x, chunk.getStartPosition().z, chunk.getStartPosition().y);
        }

        // North, south
        for (int z = 0; z < voxels[0][0].length; z++) {
            int[][] northBlocks = new int[voxels.length][voxels[0].length];
            short[][] northMeta = new short[voxels.length][voxels[0].length];
            float[][] northLightLevels = new float[voxels.length][voxels[0].length];
            int[][] southBlocks = new int[voxels.length][voxels[0].length];
            short[][] southMeta = new short[voxels.length][voxels[0].length];
            float[][] southLightLevels = new float[voxels.length][voxels[0].length];
            for (int x = 0; x < voxels.length; x++) {
                for (int y = 0; y < voxels[0].length; y++) {
                    Block curBlock = voxels[x][y][z];
                    if (curBlock == null) continue;
                    int northNeighborZ = chunk.getStartPosition().z + z + 1;
                    int southNeighborZ = chunk.getStartPosition().z + z - 1;
                    IChunk northNeighborChunk = chunk.getWorld().getChunkAtPosition(chunk.getStartPosition().x + x, northNeighborZ);
                    IChunk southNeighborChunk = chunk.getWorld().getChunkAtPosition(chunk.getStartPosition().x + x, southNeighborZ);

                    if (northNeighborChunk != null) {
                        Block northNeighborBlock = VoxelGameAPI.instance.getBlockByID(
                                northNeighborChunk.getBlockIdAtPosition(x, y, northNeighborZ));
                        if (!ocCond.shouldOcclude(curBlock, northNeighborBlock)) {
                            northBlocks[x][y] = curBlock.getID();
                            northMeta[x][y] = metadata[x][y][z];
                            northLightLevels[x][y] = northNeighborChunk.getLightLevel(x, y, northNeighborZ);
                        }
                    } else {
                        northLightLevels[x][y] = 1;
                        continue;
                    }

                    if (southNeighborChunk != null) {
                        Block southNeighborBlock = VoxelGameAPI.instance.getBlockByID(
                                southNeighborChunk.getBlockIdAtPosition(x, y, southNeighborZ));
                        if (!ocCond.shouldOcclude(curBlock, southNeighborBlock)) {
                            southBlocks[x][y] = curBlock.getID();
                            southMeta[x][y] = metadata[x][y][z];
                            southLightLevels[x][y] = southNeighborChunk.getLightLevel(x, y, southNeighborZ);
                        }
                    } else {
                        southLightLevels[x][y] = 1;
                        continue;
                    }
                }
            }

            greedy(faces, Side.NORTH, northBlocks, northMeta, northLightLevels, z + chunk.getStartPosition().z, chunk.getStartPosition().x, chunk.getStartPosition().y);
            greedy(faces, Side.SOUTH, southBlocks, southMeta, southLightLevels, z + chunk.getStartPosition().z, chunk.getStartPosition().x, chunk.getStartPosition().y);
        }

        return faces;
    }

    public List<Face> getFaces(Block[][][] voxels, short[][][] metadata, float[][][] lightLevels) {
        return getFaces(voxels, metadata, lightLevels, new OccludeCondition() {
            @Override
            public boolean shouldOcclude(Block curBlock, Block blockToSide) {
                return !(blockToSide == null || (blockToSide.isTranslucent() && !curBlock.isTranslucent()))
                    && (curBlock.occludeCovered() && blockToSide.occludeCovered());
            }
        });
    }

    /**
     * @param outputList List to put faces in
     * @param side       Side being meshed
     * @param blks       Blocks on the plane
     * @param lls        Light levels of the blocks
     * @param z          Depth on the plane
     */
    private void greedy(List<Face> outputList, Side side, int[][] blks, short metadata[][], float lls[][], int z, int offsetX, int offsetY) {
        int width = blks.length;
        int height = blks[0].length;
        boolean[][] used = new boolean[blks.length][blks[0].length];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int blk = blks[x][y];
                if(blk == 0)
                    continue;
                float ll = lls[x][y];
                short meta = metadata[x][y];
                if (!used[x][y] && blk > 0) {
                    used[x][y] = true;
                    int endX = x + 1;
                    int endY = y + 1;
                    while (true) {
                        int newX = endX;
                        if (newX == blks.length) {
                            break;
                        }
                        int newBlk = blks[newX][y];
                        float newll = lls[newX][y];
                        short newMeta = metadata[newX][y];
                        if (((newBlk == blk && newMeta == meta) || (newll == 0 && newBlk != 0)) && newll == ll && !used[newX][y]) {
                            endX++;
                            used[newX][y] = true;
                        } else {
                            while (true) {
                                if (endY == height) break;
                                boolean allPassed = true;
                                for (int lx = x; lx < endX; lx++) {
                                    int lblk = blks[lx][endY];
                                    float llight = lls[lx][endY];
                                    if (((lblk != blk || metadata[lx][endY] != meta) && (llight != 0 || lblk == 0)) || llight != ll || used[lx][endY]) {
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

    public Mesh meshFaces(List<Face> faces, MeshBuilder builder) {
        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal, GL20.GL_TRIANGLES);
        for (Face f : faces) {
            f.render(builder);
        }
        return builder.end();
    }

    @Override
    public void enableAlpha() {
        this.useAlpha = true;
    }

    @Override
    public void disableAlpha() {
        this.useAlpha = false;
    }

    public static class Face {
        private final Side side;
        private final int x1, y1, x2, y2, z;
        private final Block block;
        private final float lightLevel;

        public Face(Side side, int block, float lightLevel, int startX, int startY, int endX, int endY, int z) {
            this.block = VoxelGameAPI.instance.getBlockByID(block);
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
                    renderer.renderTop(block.getTextureIndex(), x1, y1, x2, y2, z + 1, lightLevel, builder);
                    break;
                case BOTTOM:
                    renderer.renderBottom(block.getTextureIndex(), x1, y1, x2, y2, z, lightLevel, builder);
                    break;
                case NORTH:
                    renderer.renderNorth(block.getTextureIndex(), x1, y1, x2, y2, z + 1, lightLevel, builder);
                    break;
                case SOUTH:
                    renderer.renderSouth(block.getTextureIndex(), x1, y1, x2, y2, z, lightLevel, builder);
                    break;
                case EAST:
                    renderer.renderEast(block.getTextureIndex(), x1, y1, x2, y2, z + 1, lightLevel, builder);
                    break;
                case WEST:
                    renderer.renderWest(block.getTextureIndex(), x1, y1, x2, y2, z, lightLevel, builder);
                    break;

            }
        }
    }

    public interface OccludeCondition {
        /**
         * @param curBlock Current block being checked
         * @param blockToSide Block the the side of the current block
         * @return True if the side of the curBlock should be occluded
         */
        boolean shouldOcclude(Block curBlock, Block blockToSide);
    }

}
