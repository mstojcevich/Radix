package sx.lambda.mstojcevich.voxel.client.render.meshing;

import org.lwjgl.BufferUtils;
import sx.lambda.mstojcevich.voxel.block.Block;
import sx.lambda.mstojcevich.voxel.block.IBlockRenderer;
import sx.lambda.mstojcevich.voxel.block.Side;
import sx.lambda.mstojcevich.voxel.util.Vec3i;
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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
    public MeshResult meshVoxels(Block[][][] voxels, float[][][] lightLevels) {

        List<Face> faces = new ArrayList<>();

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
                            topLightLevels[x][z] = lightLevels[x][y+1][z];
                        } else if(voxels[x][y+1][z].isTransparent()) {
                            topBlocks[x][z] = voxels[x][y][z];
                            topLightLevels[x][z] = lightLevels[x][y+1][z];
                        }
                    } else {
                        topBlocks[x][z] = voxels[x][y][z];
                        topLightLevels[x][z] = 1;
                    }
                    if(y > 0) {
                        if(voxels[x][y-1][z] == null) {
                            btmBlocks[x][z] = voxels[x][y][z];
                            btmLightLevels[x][z] = lightLevels[x][y-1][z];
                        } else if(voxels[x][y-1][z].isTransparent()) {
                            btmBlocks[x][z] = voxels[x][y][z];
                            btmLightLevels[x][z] = lightLevels[x][y-1][z];
                        }
                    } else {
                        btmBlocks[x][z] = voxels[x][y][z];
                        btmLightLevels[x][z] = 1;
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
                    Vec3i westNeighborPos = chunk.getStartPosition().translate(x-1, y, z);
                    IChunk westNeighborChunk = chunk.getWorld().getChunkAtPosition(westNeighborPos);
                    if(westNeighborChunk != null) {
                        Block westNeighborBlk = westNeighborChunk.getBlockAtPosition(westNeighborPos);
                        if(westNeighborBlk == null) {
                            westBlocks[z][y] = voxels[x][y][z];
                            westLightLevels[z][y] = westNeighborChunk.getLightLevel(westNeighborPos.x, westNeighborPos.y, westNeighborPos.z);
                        } else if(westNeighborBlk.isTransparent()) {
                            westBlocks[z][y] = voxels[x][y][z];
                            westLightLevels[z][y] = westNeighborChunk.getLightLevel(westNeighborPos.x, westNeighborPos.y, westNeighborPos.z);
                        }
                    } else {
                        westBlocks[z][y] = voxels[x][y][z];
                        westLightLevels[z][y] = 1;
                    }

                    Vec3i eastNeighborPos = chunk.getStartPosition().translate(x+1, y, z);
                    IChunk eastNeighborChunk = chunk.getWorld().getChunkAtPosition(eastNeighborPos);
                    if(eastNeighborChunk != null) {
                        Block eastNeighborBlk = eastNeighborChunk.getBlockAtPosition(eastNeighborPos);
                        if(eastNeighborBlk == null) {
                            eastBlocks[z][y] = voxels[x][y][z];
                            eastLightLevels[z][y] = eastNeighborChunk.getLightLevel(eastNeighborPos.x, eastNeighborPos.y, eastNeighborPos.z);
                        } else if(eastNeighborBlk.isTransparent()) {
                            eastBlocks[z][y] = voxels[x][y][z];
                            eastLightLevels[z][y] = eastNeighborChunk.getLightLevel(eastNeighborPos.x, eastNeighborPos.y, eastNeighborPos.z);
                        }
                    } else {
                        eastBlocks[z][y] = voxels[x][y][z];
                        eastLightLevels[z][y] = 1;
                    }
                }
            }

            greedy(faces, Side.EAST, eastBlocks, eastLightLevels, x);
            greedy(faces, Side.WEST, westBlocks, westLightLevels, x);
        }

        // North, south
        for(int z = 0; z < voxels[0][0].length; z++) {
            Block[][] northBlocks = new Block[voxels.length][voxels[0].length];
            float[][] northLightLevels = new float[voxels.length][voxels[0].length];
            Block[][] southBlocks = new Block[voxels.length][voxels[0].length];
            float[][] southLightLevels = new float[voxels.length][voxels[0].length];
            for(int x = 0; x < voxels.length; x++) {
                for(int y = 0; y < voxels[0].length; y++) {
                    Vec3i northNeighborPos = chunk.getStartPosition().translate(x, y, z+1);
                    Vec3i southNeighborPos = chunk.getStartPosition().translate(x, y, z-1);
                    IChunk northNeighborChunk = chunk.getWorld().getChunkAtPosition(northNeighborPos);
                    IChunk southNeighborChunk = chunk.getWorld().getChunkAtPosition(southNeighborPos);

                    if(northNeighborChunk != null) {
                        Block northNeighborBlock = northNeighborChunk.getBlockAtPosition(northNeighborPos);
                        if(northNeighborBlock == null) {
                            northBlocks[x][y] = voxels[x][y][z];
                            northLightLevels[x][y] = northNeighborChunk.getLightLevel(northNeighborPos.x, northNeighborPos.y, northNeighborPos.z);
                        } else if(northNeighborBlock.isTransparent()) {
                            northBlocks[x][y] = voxels[x][y][z];
                            northLightLevels[x][y] = northNeighborChunk.getLightLevel(northNeighborPos.x, northNeighborPos.y, northNeighborPos.z);
                        }
                    } else {
                        northBlocks[x][y] = voxels[x][y][z];
                        northLightLevels[x][y] = 1;
                    }

                    if(southNeighborChunk != null) {
                        Block southNeighborBlock = southNeighborChunk.getBlockAtPosition(southNeighborPos);
                        if(southNeighborBlock == null) {
                            southBlocks[x][y] = voxels[x][y][z];
                            southLightLevels[x][y] = southNeighborChunk.getLightLevel(southNeighborPos.x, southNeighborPos.y, southNeighborPos.z);
                        } else if(southNeighborBlock.isTransparent()) {
                            southBlocks[x][y] = voxels[x][y][z];
                            southLightLevels[x][y] = southNeighborChunk.getLightLevel(southNeighborPos.x, southNeighborPos.y, southNeighborPos.z);
                        }
                    } else {
                        southBlocks[x][y] = voxels[x][y][z];
                        southLightLevels[x][y] = 1;
                    }
                }
            }

            greedy(faces, Side.NORTH, northBlocks, northLightLevels, z);
            greedy(faces, Side.SOUTH, southBlocks, southLightLevels, z);
        }

        FloatBuffer posBuffer = BufferUtils.createFloatBuffer(faces.size()*4*3);
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(faces.size()*4*(useAlpha?4:3));
        FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(faces.size()*4*3);
        IntBuffer idBuffer = BufferUtils.createIntBuffer(faces.size()*4);
        for(Face f : faces) {
            f.render(posBuffer, colorBuffer, normalBuffer, idBuffer);
        }
        posBuffer.flip();
        colorBuffer.flip();
        normalBuffer.flip();
        idBuffer.flip();

        return new MeshResult(posBuffer, colorBuffer, useAlpha, normalBuffer, idBuffer);
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
                        if(newBlk == blk && newll == ll && !used[newX][y]) {
                            endX++;
                            used[newX][y] = true;
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

        public void render(FloatBuffer positions, FloatBuffer colors, FloatBuffer normals, IntBuffer idBuffer) {
            IBlockRenderer renderer = block.getRenderer();

            switch(side) {
                case TOP:
                    renderer.renderTop(x1, y1, x2, y2, z+1, lightLevel, positions, idBuffer, normals, colors);
                    break;
                case BOTTOM:
                    renderer.renderBottom(x1, y1, x2, y2, z, lightLevel, positions, idBuffer, normals, colors);
                    break;
                case NORTH:
                    renderer.renderNorth(x1, y1, x2, y2, z+1, lightLevel, positions, idBuffer, normals, colors);
                    break;
                case SOUTH:
                    renderer.renderSouth(x1, y1, x2, y2, z, lightLevel, positions, idBuffer, normals, colors);
                    break;
                case EAST:
                    renderer.renderEast(x1, y1, x2, y2, z+1, lightLevel, positions, idBuffer, normals, colors);
                    break;
                case WEST:
                    renderer.renderWest(x1, y1, x2, y2, z, lightLevel, positions, idBuffer, normals, colors);
                    break;
            }
        }
    }
}
