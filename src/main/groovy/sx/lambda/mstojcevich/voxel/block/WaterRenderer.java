package sx.lambda.mstojcevich.voxel.block;

import sx.lambda.mstojcevich.voxel.util.Vec3i;
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class WaterRenderer extends NormalBlockRenderer {

    public WaterRenderer(int blockID) {
        super(blockID);
    }


    @Override
    public void renderVBO(IChunk chunk, byte x, byte y, byte z, float[][][] lightLevels,
                          ByteBuffer vertexBuffer, FloatBuffer textureBuffer, FloatBuffer normalBuffer, FloatBuffer colorBuffer,
                          boolean shouldRenderTop, boolean shouldRenderBottom,
                          boolean shouldRenderLeft, boolean shouldRenderRight,
                          boolean shouldRenderFront, boolean shouldRenderBack) {
        float u2 = u+TEXTURE_PERCENTAGE-.001f;
        float v2 = v+TEXTURE_PERCENTAGE-.001f;

        int worldX = chunk.getStartPosition().x + (int)x;
        int worldZ = chunk.getStartPosition().z + (int)z;

        if(shouldRenderTop) {
            vertexBuffer.put(new byte[]{
                    // Top
                    x, y, (byte) (z + 1),
                    (byte) (x + 1), y, (byte) (z + 1),
                    (byte) (x + 1), (byte) (y + 1), (byte) (z + 1),
                    x, (byte) (y + 1), (byte) (z + 1),
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
            float usedLightLevel = chunk.getWorld().getLightLevel(new Vec3i(worldX, (int)y, worldZ+1));
            for(int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }

        if(shouldRenderLeft) {
            vertexBuffer.put(new byte[]{
                    // Left
                    x, y, z,
                    x, y, (byte) (z + 1),
                    x, (byte) (y + 1), (byte) (z + 1),
                    x, (byte) (y + 1), z,
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
            float usedLightLevel = chunk.getWorld().getLightLevel(new Vec3i(worldX-1, (int)y, worldZ));
            for(int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }

        if(shouldRenderRight) {
            vertexBuffer.put(new byte[]{
                    // Right
                    (byte) (x + 1), y, z,
                    (byte) (x + 1), (byte) (y + 1), z,
                    (byte) (x + 1), (byte) (y + 1), (byte) (z + 1),
                    (byte) (x + 1), y, (byte) (z + 1),
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
            float usedLightLevel = chunk.getWorld().getLightLevel(new Vec3i(worldX + 1, (int)y, worldZ));
            for(int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }

        if(shouldRenderFront) {
            vertexBuffer.put(new byte[]{
                    // Front
                    x, y, z,
                    (byte) (x + 1), y, z,
                    (byte) (x + 1), y, (byte) (z + 1),
                    x, y, (byte) (z + 1),
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
            if(y-1 > 0) {
                usedLightLevel = lightLevels[(int)x][(int)y-1][(int)z];
            }
            for(int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }

        if(shouldRenderBack) {
            vertexBuffer.put(new byte[]{
                    // Back
                    (byte) (x + 1), (byte) (y + 1), z,
                    x, (byte) (y + 1), z,
                    x, (byte) (y + 1), (byte) (z + 1),
                    (byte) (x + 1), (byte) (y + 1), (byte) (z + 1),
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
            if(y+1 < lightLevels[0].length) {
                usedLightLevel = lightLevels[(int)x][(int)y+1][(int)z];
            }
            for(int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }

        if(shouldRenderBottom) {
            vertexBuffer.put(new byte[]{
                    // Bottom
                    (byte) (x + 1), y, z,
                    x, y, z,
                    x, (byte) (y + 1), z,
                    (byte) (x + 1), (byte) (y + 1), z
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
            float usedLightLevel = chunk.getWorld().getLightLevel(new Vec3i(worldX, (int)y, worldZ-1));
            for(int i = 0; i < 4; i++) {
                colorBuffer.put(new float[]{usedLightLevel, usedLightLevel, usedLightLevel, 0.6f});
            }
        }
    }

}
