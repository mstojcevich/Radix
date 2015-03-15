package sx.lambda.mstojcevich.voxel.block;

import java.nio.FloatBuffer;

public interface IBlockRenderer {

    public void renderVBO(float x, float y, float z, float[][][] lightLevels,
                          FloatBuffer vertexBuffer, FloatBuffer textureBuffer, FloatBuffer normalBuffer, FloatBuffer colorBuffer,
                          boolean shouldRenderTop, boolean shouldRenderBottom,
                          boolean shouldRenderLeft, boolean shouldRenderRight,
                          boolean shouldRenderFront, boolean shouldRenderBack);

    public void render2d(float x, float y, float width);

}
