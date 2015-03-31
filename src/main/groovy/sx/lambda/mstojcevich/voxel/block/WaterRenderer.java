package sx.lambda.mstojcevich.voxel.block;

import sx.lambda.mstojcevich.voxel.world.chunk.IChunk;

import java.nio.FloatBuffer;

public class WaterRenderer extends NormalBlockRenderer {

    public WaterRenderer(int blockID) {
        super(blockID);
    }

    @Override
    public void renderVBO(IChunk c, int x, int y, int z, float[][][] lightLevels,
                          FloatBuffer vertexBuffer, FloatBuffer textureBuffer, FloatBuffer normalBuffer, FloatBuffer colorBuffer,
                          boolean shouldRenderTop, boolean shouldRenderBottom,
                          boolean shouldRenderLeft, boolean shouldRenderRight,
                          boolean shouldRenderFront, boolean shouldRenderBack) {
        float u2 = u + TEXTURE_PERCENTAGE - .001f;
        float v2 = v + TEXTURE_PERCENTAGE - .001f;

        if (shouldRenderTop) {
            vertexBuffer.put(new float[]{
                    // Top
                    x, y, z + 1,
                    x + 1, y, z + 1,
                    x + 1, y + 1, z + 1,
                    x, y + 1, z + 1,
            });
            textureBuffer.put(new float[]{
                    u, v,
                    u, v2,
                    u2, v2,
                    u2, v,
            });
            normalBuffer.put(new float[]{
                    0, 0, 1,
                    0, 0, 1,
                    0, 0, 1,
                    0, 0, 1
            });
            float usedLightLevel = 1.0f;
            if (z + 1 < lightLevels.length) { // TODO check the light level in a chunk over and use that one
                usedLightLevel = lightLevels[(int) x][(int) y][(int) z + 1];
            }
            for (int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }

        if (shouldRenderLeft) {
            vertexBuffer.put(new float[]{
                    // Left
                    x, y, z,
                    x, y, z + 1,
                    x, y + 1, z + 1,
                    x, y + 1, z,
            });
            textureBuffer.put(new float[]{
                    u, v,
                    u, v2,
                    u2, v2,
                    u2, v,
            });
            normalBuffer.put(new float[]{
                    -1, 0, 0,
                    -1, 0, 0,
                    -1, 0, 0,
                    -1, 0, 0,
            });
            float usedLightLevel = 1.0f;
            if (x - 1 > 0) { // TODO check the light level in a chunk over and use that one
                usedLightLevel = lightLevels[(int) x - 1][(int) y][(int) z];
            }
            for (int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }

        if (shouldRenderRight) {
            vertexBuffer.put(new float[]{
                    // Right
                    x + 1, y, z,
                    x + 1, y + 1, z,
                    x + 1, y + 1, z + 1,
                    x + 1, y, z + 1,
            });
            textureBuffer.put(new float[]{
                    u, v,
                    u, v2,
                    u2, v2,
                    u2, v,
            });
            normalBuffer.put(new float[]{
                    1, 0, 0,
                    1, 0, 0,
                    1, 0, 0,
                    1, 0, 0,
            });
            float usedLightLevel = 1.0f;
            if (x + 1 < lightLevels.length) { // TODO check the light level in a chunk over and use that one
                usedLightLevel = lightLevels[(int) x + 1][(int) y][(int) z];
            }
            for (int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }

        if (shouldRenderFront) {
            vertexBuffer.put(new float[]{
                    // Front
                    x, y, z,
                    x + 1, y, z,
                    x + 1, y, z + 1,
                    x, y, z + 1,
            });
            textureBuffer.put(new float[]{
                    u, v,
                    u, v2,
                    u2, v2,
                    u2, v,
            });
            normalBuffer.put(new float[]{
                    0, -1, 0,
                    0, -1, 0,
                    0, -1, 0,
                    0, -1, 0,
            });
            float usedLightLevel = 1.0f;
            if (y - 1 > 0) { // TODO check the light level in a chunk over and use that one
                usedLightLevel = lightLevels[(int) x][(int) y - 1][(int) z];
            }
            for (int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }

        if (shouldRenderBack) {
            vertexBuffer.put(new float[]{
                    // Back
                    x + 1, y + 1, z,
                    x, y + 1, z,
                    x, y + 1, z + 1,
                    x + 1, y + 1, z + 1,
            });
            textureBuffer.put(new float[]{
                    u, v,
                    u, v2,
                    u2, v2,
                    u2, v,
            });
            normalBuffer.put(new float[]{
                    0, 1, 0,
                    0, 1, 0,
                    0, 1, 0,
                    0, 1, 0,
            });
            float usedLightLevel = 1.0f;
            if (y + 1 < lightLevels[0].length) { // TODO check the light level in a chunk over and use that one
                usedLightLevel = lightLevels[(int) x][(int) y + 1][(int) z];
            }
            for (int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }

        if (shouldRenderBottom) {
            vertexBuffer.put(new float[]{
                    // Bottom
                    x + 1, y, z,
                    x, y, z,
                    x, y + 1, z,
                    x + 1, y + 1, z
            });
            textureBuffer.put(new float[]{
                    u, v,
                    u, v2,
                    u2, v2,
                    u2, v,
            });
            normalBuffer.put(new float[]{
                    0, 0, -1,
                    0, 0, -1,
                    0, 0, -1,
                    0, 0, -1,
            });
            float usedLightLevel = 1.0f;
            if (z - 1 > 0) { // TODO check the light level in a chunk over and use that one
                usedLightLevel = lightLevels[(int) x][(int) y][(int) z - 1];
            }
            for (int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }
    }

    @Override
    public void renderNorth(int x1, int y1, int x2, int y2, int z, float lightLevel, FloatBuffer posBuffer, FloatBuffer texBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer) {
        // POSITIVE Z

        float u2 = u+TEXTURE_PERCENTAGE-.001f;
        float v2 = v+TEXTURE_PERCENTAGE-.001f;

        posBuffer.put(new float[] {
                x1, y1, z,
                x2, y1, z,
                x2, y2, z,
                x1, y2, z,
        });
        texBuffer.put(new float[] {
                u, v,
                u, v2,
                u2, v2,
                u2, v,
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
    public void renderSouth(int x1, int y1, int x2, int y2, int z, float lightLevel, FloatBuffer posBuffer, FloatBuffer texBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer) {
        // NEGATIVE Z

        float u2 = u+TEXTURE_PERCENTAGE-.001f;
        float v2 = v+TEXTURE_PERCENTAGE-.001f;

        posBuffer.put(new float[]{
                // Bottom
                x2, y1, z,
                x1, y1, z,
                x1, y2, z,
                x2, y2, z
        });
        texBuffer.put(new float[]{
                u, v,
                u, v2,
                u2, v2,
                u2, v,
        });
        normBuffer.put(new float[]{
                0, 0, -1,
                0, 0, -1,
                0, 0, -1,
                0, 0, -1,
        });
        for(int j = 0; j < 4; j++) {
            for (int i = 0; i < 3; i++) {
                colorBuffer.put(lightLevel);
            }
            colorBuffer.put(0.6f);
        }
    }

    @Override
    public void renderWest(int z1, int y1, int z2, int y2, int x, float lightLevel, FloatBuffer posBuffer, FloatBuffer texBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer) {
        // NEGATIVE X

        float u2 = u+TEXTURE_PERCENTAGE - .001f;
        float v2 = v + TEXTURE_PERCENTAGE - .001f;

        posBuffer.put(new float[]{
                x, y1, z1,
                x, y1, z2,
                x, y2, z2,
                x, y2, z1,
        });
        texBuffer.put(new float[]{
                u, v,
                u, v2,
                u2, v2,
                u2, v,
        });
        normBuffer.put(new float[]{
                -1, 0, 0,
                -1, 0, 0,
                -1, 0, 0,
                -1, 0, 0,
        });
        for(int j = 0; j < 4; j++) {
            for (int i = 0; i < 3; i++) {
                colorBuffer.put(lightLevel);
            }
            colorBuffer.put(0.6f);
        }
    }

    @Override
    public void renderEast(int z1, int y1, int z2, int y2, int x, float lightLevel, FloatBuffer posBuffer, FloatBuffer texBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer) {
        // POSITIVE X

        float u2 = u+TEXTURE_PERCENTAGE - .001f;
        float v2 = v + TEXTURE_PERCENTAGE - .001f;

        posBuffer.put(new float[]{
                x, y1, z1,
                x, y2, z1,
                x, y2, z2,
                x, y1, z2,
        });
        texBuffer.put(new float[]{
                u, v,
                u, v2,
                u2, v2,
                u2, v,
        });
        normBuffer.put(new float[]{
                1, 0, 0,
                1, 0, 0,
                1, 0, 0,
                1, 0, 0,
        });
        for(int j = 0; j < 4; j++) {
            for (int i = 0; i < 3; i++) {
                colorBuffer.put(lightLevel);
            }
            colorBuffer.put(0.6f);
        }
    }

    @Override
    public void renderTop(int x1, int z1, int x2, int z2, int y, float lightLevel, FloatBuffer posBuffer, FloatBuffer texBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer) {
        // POSITIVE Y

        float u2 = u+TEXTURE_PERCENTAGE - .001f;
        float v2 = v + TEXTURE_PERCENTAGE - .001f;

        posBuffer.put(new float[]{
                // Back
                x2, y, z1,
                x1, y, z1,
                x1, y, z2,
                x2, y, z2,
        });
        texBuffer.put(new float[]{
                u, v,
                u, v2,
                u2, v2,
                u2, v,
        });
        normBuffer.put(new float[]{
                0, 1, 0,
                0, 1, 0,
                0, 1, 0,
                0, 1, 0,
        });
        for(int j = 0; j < 4; j++) {
            for (int i = 0; i < 3; i++) {
                colorBuffer.put(lightLevel);
            }
            colorBuffer.put(0.6f);
        }
    }

    @Override
    public void renderBottom(int x1, int z1, int x2, int z2, int y, float lightLevel, FloatBuffer posBuffer, FloatBuffer texBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer) {
        // NEGATIVE Y

        float u2 = u+TEXTURE_PERCENTAGE - .001f;
        float v2 = v + TEXTURE_PERCENTAGE - .001f;

        posBuffer.put(new float[]{
                // Front
                x1, y, z1,
                x2, y, z1,
                x2, y, z2,
                x1, y, z2,
        });
        texBuffer.put(new float[]{
                u, v,
                u, v2,
                u2, v2,
                u2, v,
        });
        normBuffer.put(new float[]{
                0, -1, 0,
                0, -1, 0,
                0, -1, 0,
                0, -1, 0,
        });
        for(int j = 0; j < 4; j++) {
            for (int i = 0; i < 3; i++) {
                colorBuffer.put(lightLevel);
            }
            colorBuffer.put(0.6f);
        }
    }

}
