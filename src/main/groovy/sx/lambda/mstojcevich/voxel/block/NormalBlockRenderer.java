package sx.lambda.mstojcevich.voxel.block;

import groovy.transform.CompileStatic;
import sx.lambda.mstojcevich.voxel.VoxelGame;
import sx.lambda.mstojcevich.voxel.util.Vec3i;
import sx.lambda.mstojcevich.voxel.util.gl.SpriteBatcher;
import sx.lambda.mstojcevich.voxel.util.gl.TextureLoader;
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@CompileStatic
public class NormalBlockRenderer implements IBlockRenderer {

    // TODO figure this out from the file
    private static final int MAP_WIDTH = 128, MAP_HEIGHT = 128;
    private static final int BLOCKS_PER_WIDTH = 4, TEXMAP_BLOCK_WIDTH = MAP_WIDTH/BLOCKS_PER_WIDTH;
    protected static final float TEXTURE_PERCENTAGE = 0.25f;

    private static int blockMap;

    protected final int blockID;
    protected final float u, v;

    private static boolean initialized;

    public NormalBlockRenderer(int blockID) {
        this.blockID = blockID;
        u = ((blockID%4)*TEXTURE_PERCENTAGE);
        v = ((blockID/4)*TEXTURE_PERCENTAGE);
    }

    @Override
    public void renderVBO(IChunk chunk, int x, int y, int z, float[][][] lightLevels,
                          FloatBuffer vertexBuffer, IntBuffer idBuffer, FloatBuffer normalBuffer, FloatBuffer colorBuffer,
                          boolean shouldRenderTop, boolean shouldRenderBottom,
                          boolean shouldRenderLeft, boolean shouldRenderRight,
                          boolean shouldRenderFront, boolean shouldRenderBack) {
        int worldX = chunk.getStartPosition().x + (int)x;
        int worldZ = chunk.getStartPosition().z + (int)z;

        if(shouldRenderTop) {
            float usedLightLevel = chunk.getWorld().getLightLevel(new Vec3i(worldX, y, worldZ+1));
            renderNorth(x, y, x+1, y+1, z+1, usedLightLevel, vertexBuffer, idBuffer, normalBuffer, colorBuffer);
        }

        if(shouldRenderLeft) {
            float usedLightLevel = chunk.getWorld().getLightLevel(new Vec3i(worldX-1, y, worldZ));
            renderWest(z, y, z+1, y+1, x, usedLightLevel, vertexBuffer, idBuffer, normalBuffer, colorBuffer);
        }

        if(shouldRenderRight) {
            float usedLightLevel = chunk.getWorld().getLightLevel(new Vec3i(worldX + 1, y, worldZ));
            renderEast(z, y, z+1, y+1, x+1, usedLightLevel, vertexBuffer, idBuffer, normalBuffer, colorBuffer);
        }

        if(shouldRenderFront) {
            float usedLightLevel = 1.0f;
            if(y-1 > 0) {
                usedLightLevel = lightLevels[x][y-1][z];
            }
            renderBottom(x, z, x+1, z+1, y, usedLightLevel, vertexBuffer, idBuffer, normalBuffer, colorBuffer);
        }

        if(shouldRenderBack) {
            float usedLightLevel = 1.0f;
            if(y+1 < lightLevels[0].length) {
                usedLightLevel = lightLevels[x][y+1][z];
            }
            renderTop(x, z, x+1, z+1, y+1, usedLightLevel, vertexBuffer, idBuffer, normalBuffer, colorBuffer);
        }

        if(shouldRenderBottom) {
            float usedLightLevel = chunk.getWorld().getLightLevel(new Vec3i(worldX, y, worldZ-1));
            renderSouth(x, y, x+1, y+1, z, usedLightLevel, vertexBuffer, idBuffer, normalBuffer, colorBuffer);
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
        for(int i = 0; i < 4*3; i++) {
            colorBuffer.put(lightLevel);
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
        for(int i = 0; i < 4*3; i++) {
            colorBuffer.put(lightLevel);
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
        for(int i = 0; i < 4*3; i++) {
            colorBuffer.put(lightLevel);
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
        for(int i = 0; i < 4*3; i++) {
            colorBuffer.put(lightLevel);
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
        for(int i = 0; i < 4*3; i++) {
            colorBuffer.put(lightLevel);
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
        for(int i = 0; i < 4*3; i++) {
            colorBuffer.put(lightLevel);
        }
    }

    private static void initialize() {
        blockMap = TextureLoader.loadTexture(NormalBlockRenderer.class.getResourceAsStream("/textures/block/blockSheet.png"), VoxelGame.getInstance().getTextureManager());
        VoxelGame.getInstance().enableDefaultShader();
        VoxelGame.getInstance().getShaderManager().setTexmapWidth(MAP_WIDTH);
        VoxelGame.getInstance().getShaderManager().setTexmapHeight(MAP_HEIGHT);
        VoxelGame.getInstance().getShaderManager().setBlocksPerTexmapRow(BLOCKS_PER_WIDTH);
        VoxelGame.getInstance().getShaderManager().setTexmapBlockWidth(TEXMAP_BLOCK_WIDTH);
        initialized = true;
    }

    public static int getBlockMap() {
        if(blockMap == 0) {
            initialize();
        }
        return blockMap;
    }
}
