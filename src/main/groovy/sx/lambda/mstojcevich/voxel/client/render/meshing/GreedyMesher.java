package sx.lambda.mstojcevich.voxel.client.render.meshing;

import org.lwjgl.BufferUtils;
import sx.lambda.mstojcevich.voxel.VoxelGame;
import sx.lambda.mstojcevich.voxel.block.Block;
import sx.lambda.mstojcevich.voxel.util.Vec3i;
import sx.lambda.mstojcevich.voxel.world.IWorld;
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk;

import java.nio.FloatBuffer;
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
        List<Face> faces = new ArrayList<Face>();

        for(int y = 0; y < voxels[0].length; y++) {
            for(int z = 0; z < voxels[0][0].length; z++) {
                boolean[][] adMask = new boolean[voxels.length][voxels[0][0].length];
                Block currentBlock = null;
                float currentLL = -1;
                int curFaceStartX = -1;
                for (int x = 0; x < voxels.length; x++) {
                    Block blk = voxels[x][y][z];
                    float lightLevel = lightLevels[x][y][z];
                    if ((currentBlock == null || currentBlock == blk)
                            && (lightLevel == currentLL || currentLL == -1)
                            && blk != null && !adMask[x][z]) {
                        currentBlock = blk;
                        currentLL = lightLevel;
                        adMask[x][z] = true;
                        if(curFaceStartX == -1) {
                            curFaceStartX = x;
                        }
                    } else { // The current face is over. Add it and start a new one
                        faces.add(new Face(curFaceStartX, y, z, x-1, y, z+1, currentBlock, currentLL));
                        currentBlock = null;
                        currentLL = -1;
                        curFaceStartX = -1;
                        x = x-1; // Go back and try this position again, because this is a new face
                    }
                }
            }
        }

        for(Face f : faces) {

        }

        return null;
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
        private final int x1, y1, z1, x2, y2, z2;
        private final Block block;
        private final float lightLevel;

        public Face(int x1, int y1, int z1, int x2, int y2, int z2, Block block, float lightLevel) {
            this.x1 = x1; this.y1 = y1; this.z1 = z1;
            this.x2 = x2; this.y2 = y2; this.z2 = z2;
            this.block = block;
            this.lightLevel = lightLevel;
        }

        public int getX1() { return x1; }
        public int getX2() { return x2; }
        public int getY1() { return y1; }
        public int getY2() { return y2; }
        public int getZ1() { return z1; }
        public int getZ2() { return z2; }
        public Block getBlock() { return block; }
        public float getLightLevel() { return lightLevel; }

        public void render(FloatBuffer positions, FloatBuffer colors, FloatBuffer normals, FloatBuffer texCoords) {

        }
    }
}
