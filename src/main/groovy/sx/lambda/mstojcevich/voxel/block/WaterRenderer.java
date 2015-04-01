package sx.lambda.mstojcevich.voxel.block;

import sx.lambda.mstojcevich.voxel.world.chunk.IChunk;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class WaterRenderer extends NormalBlockRenderer {

    public WaterRenderer(int blockID) {
        super(blockID);
    }

    @Override
    public void renderNorth(int x1, int y1, int x2, int y2, int z, float lightLevel, FloatBuffer posBuffer, IntBuffer idBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer) {
        // POSITIVE Z

        posBuffer.put(new float[] {
                x1, y1, z,
                x2, y1, z,
                x2, y2, z,
                x1, y2, z,
        });
        idBuffer.put(new int[]{
                blockID, blockID, blockID, blockID
        });
        normBuffer.put(new float[]{
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,
                0, 0, 1
        });
        for(int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                colorBuffer.put(lightLevel);
            }
            colorBuffer.put(0.6f);
        }
    }

    @Override
    public void renderSouth(int x1, int y1, int x2, int y2, int z, float lightLevel, FloatBuffer posBuffer, IntBuffer idBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer) {
        // NEGATIVE Z

        posBuffer.put(new float[]{
                // Bottom
                x2, y1, z,
                x1, y1, z,
                x1, y2, z,
                x2, y2, z
        });
        idBuffer.put(new int[]{
                blockID, blockID, blockID, blockID
        });
        normBuffer.put(new float[]{
                0, 0, -1,
                0, 0, -1,
                0, 0, -1,
                0, 0, -1,
        });
        for(int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                colorBuffer.put(lightLevel);
            }
            colorBuffer.put(0.6f);
        }
    }

    @Override
    public void renderWest(int z1, int y1, int z2, int y2, int x, float lightLevel, FloatBuffer posBuffer, IntBuffer idBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer) {
        // NEGATIVE X

        posBuffer.put(new float[]{
                x, y1, z1,
                x, y1, z2,
                x, y2, z2,
                x, y2, z1,
        });
        idBuffer.put(new int[]{
                blockID, blockID, blockID, blockID
        });
        normBuffer.put(new float[]{
                -1, 0, 0,
                -1, 0, 0,
                -1, 0, 0,
                -1, 0, 0,
        });
        for(int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                colorBuffer.put(lightLevel);
            }
            colorBuffer.put(0.6f);
        }
    }

    @Override
    public void renderEast(int z1, int y1, int z2, int y2, int x, float lightLevel, FloatBuffer posBuffer, IntBuffer idBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer) {
        // POSITIVE X

        posBuffer.put(new float[]{
                x, y1, z1,
                x, y2, z1,
                x, y2, z2,
                x, y1, z2,
        });
        idBuffer.put(new int[]{
                blockID, blockID, blockID, blockID
        });
        normBuffer.put(new float[]{
                1, 0, 0,
                1, 0, 0,
                1, 0, 0,
                1, 0, 0,
        });
        for(int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                colorBuffer.put(lightLevel);
            }
            colorBuffer.put(0.6f);
        }
    }

    @Override
    public void renderTop(int x1, int z1, int x2, int z2, int y, float lightLevel, FloatBuffer posBuffer, IntBuffer idBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer) {
        // POSITIVE Y

        posBuffer.put(new float[]{
                // Back
                x2, y, z1,
                x1, y, z1,
                x1, y, z2,
                x2, y, z2,
        });
        idBuffer.put(new int[]{
                blockID, blockID, blockID, blockID
        });
        normBuffer.put(new float[]{
                0, 1, 0,
                0, 1, 0,
                0, 1, 0,
                0, 1, 0,
        });
        for(int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                colorBuffer.put(lightLevel);
            }
            colorBuffer.put(0.6f);
        }
    }

    @Override
    public void renderBottom(int x1, int z1, int x2, int z2, int y, float lightLevel, FloatBuffer posBuffer, IntBuffer idBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer) {
        // NEGATIVE Y

        posBuffer.put(new float[]{
                // Front
                x1, y, z1,
                x2, y, z1,
                x2, y, z2,
                x1, y, z2,
        });
        idBuffer.put(new int[]{
                blockID, blockID, blockID, blockID
        });
        normBuffer.put(new float[]{
                0, -1, 0,
                0, -1, 0,
                0, -1, 0,
                0, -1, 0,
        });
        for(int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                colorBuffer.put(lightLevel);
            }
            colorBuffer.put(0.6f);
        }
    }

}
