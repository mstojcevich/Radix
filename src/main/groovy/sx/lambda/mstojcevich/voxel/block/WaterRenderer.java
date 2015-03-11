package sx.lambda.mstojcevich.voxel.block;

import java.nio.FloatBuffer;

public class WaterRenderer extends NormalBlockRenderer {

    public WaterRenderer(int blockID) {
        super(blockID);
    }

    @Override
    public void renderVBO(float x, float y, float z, float[][][] lightLevels,
                          FloatBuffer vertexBuffer, FloatBuffer textureBuffer, FloatBuffer normalBuffer, FloatBuffer colorBuffer,
                          boolean shouldRenderTop, boolean shouldRenderBottom,
                          boolean shouldRenderLeft, boolean shouldRenderRight,
                          boolean shouldRenderFront, boolean shouldRenderBack) {
        float u2 = u+TEXTURE_PERCENTAGE-.001f;
        float v2 = v+TEXTURE_PERCENTAGE-.001f;

        if(shouldRenderTop) {
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
            if(z+1 < lightLevels.length) { // TODO check the light level in a chunk over and use that one
                usedLightLevel = lightLevels[(int)x][(int)y][(int)z+1];
            }
            for(int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }

        if(shouldRenderLeft) {
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
            if(x-1 > 0) { // TODO check the light level in a chunk over and use that one
                usedLightLevel = lightLevels[(int)x-1][(int)y][(int)z];
            }
            for(int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }

        if(shouldRenderRight) {
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
            if(x+1 < lightLevels.length) { // TODO check the light level in a chunk over and use that one
                usedLightLevel = lightLevels[(int)x+1][(int)y][(int)z];
            }
            for(int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }

        if(shouldRenderFront) {
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
            if(y-1 > 0) { // TODO check the light level in a chunk over and use that one
                usedLightLevel = lightLevels[(int)x][(int)y-1][(int)z];
            }
            for(int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }

        if(shouldRenderBack) {
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
            if(y+1 < lightLevels[0].length) { // TODO check the light level in a chunk over and use that one
                usedLightLevel = lightLevels[(int)x][(int)y+1][(int)z];
            }
            for(int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }

        if(shouldRenderBottom) {
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
            if(z-1 > 0) { // TODO check the light level in a chunk over and use that one
                usedLightLevel = lightLevels[(int)x][(int)y][(int)z-1];
            }
            for(int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }
    }

}
