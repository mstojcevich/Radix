package sx.lambda.mstojcevich.voxel.client.render.meshing;

import sx.lambda.mstojcevich.voxel.block.Block;
import sx.lambda.mstojcevich.voxel.util.Vec3i;
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class GreedyMesher implements Mesher {

    private final IChunk chunk;
    private boolean useAlpha;

    private static enum Side { TOP, BOTTOM, WEST, EAST, NORTH, SOUTH }

    /**
     * @param chunk Chunk to mesh
     */
    public GreedyMesher(IChunk chunk) {
        this.chunk = chunk;
    }


    @Override
    public MeshResult meshVoxels(Block[][][] voxels, float[][][] lightLevels) {
        List<Face> faces = new ArrayList<Face>();

        // Top, bottom
        for(int y = 0; y < voxels[0].length; y++) {
            Block[][] topBlocks = new Block[voxels.length][voxels[0][0].length];
            float[][] topLightLevels = new float[voxels.length][voxels[0][0].length];
            Block[][] btmBlocks = new Block[voxels.length][voxels[0][0].length];
            float[][] btmLightLevels = new float[voxels.length][voxels[0][0].length];
            for(int x = 0; x < voxels.length; x++) {
                for(int z = 0; z < voxels[0][0].length; z++) {
                    if(y < voxels[0].length-1) {
                        if(voxels[x][y+1][z] == null) {
                            topBlocks[x][z] = voxels[x][y][z];
                            topLightLevels[x][z] = lightLevels[x][y][z];
                        } else if(voxels[x][y+1][z].isTransparent()) {
                            topBlocks[x][z] = voxels[x][y][z];
                            topLightLevels[x][z] = lightLevels[x][y][z];
                        }
                    } else {
                        topBlocks[x][z] = voxels[x][y][z];
                        topLightLevels[x][z] = lightLevels[x][y][z];
                    }
                    if(y > 0) {
                        if(voxels[x][y-1][z] == null) {
                            btmBlocks[x][z] = voxels[x][y][z];
                            btmLightLevels[x][z] = lightLevels[x][y][z];
                        } else if(voxels[x][y-1][z].isTransparent()) {
                            btmBlocks[x][z] = voxels[x][y][z];
                            btmLightLevels[x][z] = lightLevels[x][y][z];
                        }
                    } else {
                        btmBlocks[x][z] = voxels[x][y][z];
                        btmLightLevels[x][z] = lightLevels[x][y][z];
                    }
                }
            }
            greedy(faces, Side.TOP, topBlocks, topLightLevels, y);
            greedy(faces, Side.BOTTOM, btmBlocks, btmLightLevels, y);
        }

        // East, west
        for(int x = 0; x < voxels.length; x++) {
            Block[][] westBlocks = new Block[voxels[0][0].length][voxels[0].length];
            float[][] westLightLevels = new float[voxels[0][0].length][voxels[0].length];
            Block[][] eastBlocks = new Block[voxels[0][0].length][voxels[0].length];
            float[][] eastLightLevels = new float[voxels[0][0].length][voxels[0].length];
            for(int z = 0; z < voxels[0][0].length; z++) {
                for(int y = 0; y < voxels[0].length; y++) {
                    Vec3i westNeighborPos = chunk.getStartPosition().translate(x, y, z);
                    IChunk westNeighborChunk = chunk.getWorld().getChunkAtPosition(westNeighborPos);
                    if(westNeighborChunk != null) {
                        Block westNeighborBlk = westNeighborChunk.getBlockAtPosition(westNeighborPos);
                        if(westNeighborBlk == null) {
                            westBlocks[z][y] = voxels[x][y][z];
                            westLightLevels[z][y] = lightLevels[x][y][z];
                        } else if(westNeighborBlk.isTransparent()) {
                            westBlocks[z][y] = voxels[x][y][z];
                            westLightLevels[z][y] = lightLevels[x][y][z];
                        }
                    } else {
                        westBlocks[z][y] = voxels[x][y][z];
                        westLightLevels[z][y] = lightLevels[x][y][z];
                    }

                    Vec3i eastNeighborPos = chunk.getStartPosition().translate(x, y, z);
                    IChunk eastNeighborChunk = chunk.getWorld().getChunkAtPosition(eastNeighborPos);
                    if(eastNeighborChunk != null) {
                        Block eastNeighborBlk = eastNeighborChunk.getBlockAtPosition(eastNeighborPos);
                        if(eastNeighborBlk == null) {
                            eastBlocks[z][y] = voxels[x][y][z];
                            eastLightLevels[z][y] = lightLevels[x][y][z];
                        } else if(eastNeighborBlk.isTransparent()) {
                            eastBlocks[z][y] = voxels[x][y][z];
                            eastLightLevels[z][y] = lightLevels[x][y][z];
                        }
                    } else {
                        eastBlocks[z][y] = voxels[x][y][z];
                        eastLightLevels[z][y] = lightLevels[x][y][z];
                    }
                }
            }

            greedy(faces, Side.EAST, eastBlocks, eastLightLevels, x);
            greedy(faces, Side.WEST, westBlocks, westLightLevels, x);
        }


        return null;
    }

    /**
     *
     * @param outputList List to put faces in
     * @param side Side being meshed
     * @param blks Blocks on the plane
     * @param lls Light levels of the blocks
     * @param z Depth on the plane
     */
    private void greedy(List<Face> outputList, Side side, Block[][] blks, float lls[][], int z) {
        int width = blks.length;
        int height = blks[0].length;
        boolean[][] used = new boolean[blks.length][blks[0].length];

        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                Block blk = blks[x][y];
                float ll = lls[x][y];
                if(!used[x][y] && blk != null) {
                    used[x][y] = true;
                    int startX = x, endX = x+1;
                    int startY = y, endY = y+1;
                    while(true) {
                        int newX = endX;
                        if(newX == blks.length) {
                            break;
                        }
                        Block newBlk = blks[newX][y];
                        float newll = lls[newX][y];
                        if(newBlk == blk && newll == ll && !used[x][y]) {
                            endX++;
                            used[x][y] = true;
                        } else {
                            while(true) {
                                if(endY == height)break;
                                boolean allPassed = true;
                                for(int lx = startX; lx < endX; lx++) {
                                    if(blks[lx][endY] != blk || lls[lx][endY] != ll || used[lx][endY]) {
                                        allPassed = false;
                                    }
                                }
                                if(allPassed) {
                                    for(int lx = startX; lx < endX; lx++) {
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
                    outputList.add(new Face(side, blk, ll, startX, startY, endX, endY, z));
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
            this.x1 = startX; this.y1 = startY;
            this.x2 = endX; this.y2 = endY;
            this.z = z;
            this.side = side;
        }

        public void render(FloatBuffer positions, FloatBuffer colors, FloatBuffer normals, FloatBuffer texCoords) {

        }
    }
}
