package sx.lambda.mstojcevich.voxel.block;

import java.nio.FloatBuffer;

public class WaterRenderer extends NormalBlockRenderer {

    public WaterRenderer(int blockID) {
        super(blockID);
    }

    @Override
    public void renderNorth(int x1, int y1, int x2, int y2, int z, float lightLevel, FloatBuffer posBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer, FloatBuffer idBuffer) {
        // POSITIVE Z

        posBuffer.put(new float[] {
                x1, y1, z,
                x2, y1, z,
                x2, y2, z,
                x1, y2, z,
        });
        normBuffer.put(new float[]{
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,
                0, 0, 1
        });
        for(int j = 0; j < 4; j++) {
            colorBuffer.put(lightLevel);
            colorBuffer.put(0.6f);
        }
        for(int i = 0; i < 4; i++) {
            idBuffer.put(blockID);
        }
    }

    @Override
    public void renderSouth(int x1, int y1, int x2, int y2, int z, float lightLevel, FloatBuffer posBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer, FloatBuffer idBuffer) {
        // NEGATIVE Z

        posBuffer.put(new float[]{
                // Bottom
                x2, y1, z,
                x1, y1, z,
                x1, y2, z,
                x2, y2, z
        });
        normBuffer.put(new float[]{
                0, 0, -1,
                0, 0, -1,
                0, 0, -1,
                0, 0, -1,
        });
        for(int j = 0; j < 4; j++) {
            colorBuffer.put(lightLevel);
            colorBuffer.put(0.6f);
        }
        for(int i = 0; i < 4; i++) {
            idBuffer.put(blockID);
        }
    }

    @Override
    public void renderWest(int z1, int y1, int z2, int y2, int x, float lightLevel, FloatBuffer posBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer, FloatBuffer idBuffer) {
        // NEGATIVE X

        posBuffer.put(new float[]{
                x, y1, z1,
                x, y1, z2,
                x, y2, z2,
                x, y2, z1,
        });
        normBuffer.put(new float[]{
                -1, 0, 0,
                -1, 0, 0,
                -1, 0, 0,
                -1, 0, 0,
        });
        for(int j = 0; j < 4; j++) {
            colorBuffer.put(lightLevel);
            colorBuffer.put(0.6f);
        }
        for(int i = 0; i < 4; i++) {
            idBuffer.put(blockID);
        }
    }

    @Override
    public void renderEast(int z1, int y1, int z2, int y2, int x, float lightLevel, FloatBuffer posBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer, FloatBuffer idBuffer) {
        // POSITIVE X

        posBuffer.put(new float[]{
                x, y1, z1,
                x, y2, z1,
                x, y2, z2,
                x, y1, z2,
        });
        normBuffer.put(new float[]{
                1, 0, 0,
                1, 0, 0,
                1, 0, 0,
                1, 0, 0,
        });
        for(int j = 0; j < 4; j++) {
            colorBuffer.put(lightLevel);
            colorBuffer.put(0.6f);
        }
        for(int i = 0; i < 4; i++) {
            idBuffer.put(blockID);
        }
    }

    @Override
    public void renderTop(int x1, int z1, int x2, int z2, int y, float lightLevel, FloatBuffer posBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer, FloatBuffer idBuffer) {
        // POSITIVE Y

        posBuffer.put(new float[]{
                // Back
                x2, y, z1,
                x1, y, z1,
                x1, y, z2,
                x2, y, z2,
        });
        normBuffer.put(new float[]{
                0, 1, 0,
                0, 1, 0,
                0, 1, 0,
                0, 1, 0,
        });
        for(int j = 0; j < 4; j++) {
            colorBuffer.put(lightLevel);
            colorBuffer.put(0.6f);
        }
        for(int i = 0; i < 4; i++) {
            idBuffer.put(blockID);
        }
    }

    @Override
    public void renderBottom(int x1, int z1, int x2, int z2, int y, float lightLevel, FloatBuffer posBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer, FloatBuffer idBuffer) {
        // NEGATIVE Y

        posBuffer.put(new float[]{
                // Front
                x1, y, z1,
                x2, y, z1,
                x2, y, z2,
                x1, y, z2,
        });
        normBuffer.put(new float[]{
                0, -1, 0,
                0, -1, 0,
                0, -1, 0,
                0, -1, 0,
        });
        for(int j = 0; j < 4; j++) {
            colorBuffer.put(lightLevel);
            colorBuffer.put(0.6f);
        }
        for(int i = 0; i < 4; i++) {
            idBuffer.put(blockID);
        }
    }

}
