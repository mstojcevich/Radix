package sx.lambda.mstojcevich.voxel.block;

import sx.lambda.mstojcevich.voxel.util.gl.SpriteBatcher;
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk;

import java.nio.FloatBuffer;

public interface IBlockRenderer {

    void renderVBO(IChunk chunk, int x, int y, int z, float[][][] lightLevels,
                   FloatBuffer vertexBuffer, FloatBuffer normalBuffer, FloatBuffer colorBuffer, FloatBuffer idBuffer,
                   boolean shouldRenderTop, boolean shouldRenderBottom,
                   boolean shouldRenderLeft, boolean shouldRenderRight,
                   boolean shouldRenderFront, boolean shouldRenderBack);

    void render2d(SpriteBatcher batcher, int x, int y, int width);

    void renderNorth(int x1, int y1, int x2, int y2, int z, float lightLevel,
                     FloatBuffer posBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer, FloatBuffer idBuffer);
    void renderSouth(int x1, int y1, int x2, int y2, int z, float lightLevel,
                     FloatBuffer posBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer, FloatBuffer idBuffer);
    void renderWest(int z1, int y1, int z2, int y2, int x, float lightLevel,
                     FloatBuffer posBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer, FloatBuffer idBuffer);
    void renderEast(int z1, int y1, int z2, int y2, int x, float lightLevel,
                     FloatBuffer posBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer, FloatBuffer idBuffer);
    void renderTop(int x1, int z1, int x2, int z2, int y, float lightLevel,
                     FloatBuffer posBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer, FloatBuffer idBuffer);
    void renderBottom(int x1, int z1, int x2, int z2, int y, float lightLevel,
                     FloatBuffer posBuffer, FloatBuffer normBuffer, FloatBuffer colorBuffer, FloatBuffer idBuffer);

}
