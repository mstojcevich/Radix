package sx.lambda.voxel.block;

import groovy.transform.CompileStatic;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.render.NotInitializedException;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.util.gl.SpriteBatcher;
import sx.lambda.voxel.world.chunk.IChunk;

import java.nio.FloatBuffer;

@CompileStatic
public class NormalBlockRenderer implements IBlockRenderer {

    protected static final float TEXTURE_PERCENTAGE = 0.03125f;

    private static int blockMap;

    protected final float u, v;
    protected final int blockID;

    private static boolean initialized;

    final int BLOCKS_PER_WIDTH = 1024/32;

    public NormalBlockRenderer(int blockID) {
        this.blockID = blockID;
        u = ((blockID%BLOCKS_PER_WIDTH)*TEXTURE_PERCENTAGE);
        v = ((blockID/BLOCKS_PER_WIDTH)*TEXTURE_PERCENTAGE);
    }

    @Override
    public void renderVBO(IChunk chunk, int x, int y, int z, float[][][] lightLevels,
                          FloatBuffer vertexBuffer, FloatBuffer normalBuffer, FloatBuffer colorBuffer, FloatBuffer idBuffer,
                          boolean shouldRenderTop, boolean shouldRenderBottom,
                          boolean shouldRenderLeft, boolean shouldRenderRight,
                          boolean shouldRenderFront, boolean shouldRenderBack) {
        int worldX = chunk.getStartPosition().x + (int)x;
        int worldZ = chunk.getStartPosition().z + (int)z;

        if(shouldRenderTop) {
            float usedLightLevel = chunk.getWorld().getLightLevel(new Vec3i(worldX, y, worldZ+1));
            renderNorth(x, y, x+1, y+1, z+1, usedLightLevel, vertexBuffer, normalBuffer, colorBuffer, idBuffer);
        }

        if(shouldRenderLeft) {
            float usedLightLevel = chunk.getWorld().getLightLevel(new Vec3i(worldX-1, y, worldZ));
            renderWest(z, y, z+1, y+1, x, usedLightLevel, vertexBuffer, normalBuffer, colorBuffer, idBuffer);
        }

        if(shouldRenderRight) {
            float usedLightLevel = chunk.getWorld().getLightLevel(new Vec3i(worldX + 1, y, worldZ));
            renderEast(z, y, z+1, y+1, x+1, usedLightLevel, vertexBuffer, normalBuffer, colorBuffer, idBuffer);
        }

        if(shouldRenderFront) {
            float usedLightLevel = 1.0f;
            if(y-1 > 0) {
                usedLightLevel = lightLevels[x][y-1][z];
            }
            renderBottom(x, z, x+1, z+1, y, usedLightLevel, vertexBuffer, normalBuffer, colorBuffer, idBuffer);
        }

        if(shouldRenderBack) {
            float usedLightLevel = 1.0f;
            if(y+1 < lightLevels[0].length) {
                usedLightLevel = lightLevels[x][y+1][z];
            }
            renderTop(x, z, x+1, z+1, y+1, usedLightLevel, vertexBuffer, normalBuffer, colorBuffer, idBuffer);
        }

        if(shouldRenderBottom) {
            float usedLightLevel = chunk.getWorld().getLightLevel(new Vec3i(worldX, y, worldZ-1));
            renderSouth(x, y, x+1, y+1, z, usedLightLevel, vertexBuffer, normalBuffer, colorBuffer, idBuffer);
        }
    }

    @Override
    public void render2d(SpriteBatcher batcher, int x, int y, int width) {
        if(!initialized) {
            initialize();
        }
        float u2 = u+TEXTURE_PERCENTAGE-.001f;
        float v2 = v+TEXTURE_PERCENTAGE-.001f;
        batcher.drawTexturedRect(x, y, x+width, y+width, u, v, u2, v2);
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
        for(int i = 0; i < 4; i++) {
            colorBuffer.put(lightLevel);
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
        for(int i = 0; i < 4; i++) {
            colorBuffer.put(lightLevel);
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
        for(int i = 0; i < 4; i++) {
            colorBuffer.put(lightLevel);
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
        for(int i = 0; i < 4; i++) {
            colorBuffer.put(lightLevel);
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
        for(int i = 0; i < 4; i++) {
            colorBuffer.put(lightLevel);
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
        for(int i = 0; i < 4; i++) {
            colorBuffer.put(lightLevel);
        }
        for(int i = 0; i < 4; i++) {
            idBuffer.put(blockID);
        }
    }

    private static void initialize() {
        try {
            blockMap = VoxelGameClient.getInstance().getBlockTextureAtlas();
        } catch (NotInitializedException e) {
            System.err.println("Error getting block texture atlas!");
            e.printStackTrace();
        }
        initialized = true;
    }

    public static int getBlockMap() {
        if(blockMap == 0) {
            initialize();
        }
        return blockMap;
    }
}
