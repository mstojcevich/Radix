package sx.lambda.mstojcevich.voxel.block;

import groovy.transform.CompileStatic;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import sx.lambda.mstojcevich.voxel.VoxelGame;
import sx.lambda.mstojcevich.voxel.util.Vec3i;
import sx.lambda.mstojcevich.voxel.util.gl.SpriteBatcher;
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk;

import java.io.IOException;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

@CompileStatic
public class NormalBlockRenderer implements IBlockRenderer {

    protected static final float TEXTURE_PERCENTAGE = 0.25f;

    private static Texture blockMap;

    protected final float u, v;

    private static boolean initialized;

	public NormalBlockRenderer(int blockID) {
        u = ((blockID%4)*TEXTURE_PERCENTAGE);
        v = ((blockID/4)*TEXTURE_PERCENTAGE);
	}

    @Override
    public void renderVBO(IChunk chunk, float x, float y, float z, float[][][] lightLevels,
                          FloatBuffer vertexBuffer, FloatBuffer textureBuffer, FloatBuffer normalBuffer, FloatBuffer colorBuffer,
                          boolean shouldRenderTop, boolean shouldRenderBottom,
                          boolean shouldRenderLeft, boolean shouldRenderRight,
                          boolean shouldRenderFront, boolean shouldRenderBack) {
        float u2 = u+TEXTURE_PERCENTAGE-.001f;
        float v2 = v+TEXTURE_PERCENTAGE-.001f;

        int worldX = chunk.getStartPosition().x + (int)x;
        int worldZ = chunk.getStartPosition().z + (int)z;

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
            float usedLightLevel = chunk.getWorld().getLightLevel(new Vec3i(worldX, (int)y, worldZ+1));
            for(int i = 0; i < 4*3; i++) {
                colorBuffer.put(usedLightLevel);
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
            float usedLightLevel = chunk.getWorld().getLightLevel(new Vec3i(worldX-1, (int)y, worldZ));
            for(int i = 0; i < 4*3; i++) {
                colorBuffer.put(usedLightLevel);
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
            float usedLightLevel = chunk.getWorld().getLightLevel(new Vec3i(worldX + 1, (int)y, worldZ));
            for(int i = 0; i < 4*3; i++) {
                colorBuffer.put(usedLightLevel);
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
            if(y-1 > 0) {
                usedLightLevel = lightLevels[(int)x][(int)y-1][(int)z];
            }
            for(int i = 0; i < 4*3; i++) {
                colorBuffer.put(usedLightLevel);
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
            if(y+1 < lightLevels[0].length) {
                usedLightLevel = lightLevels[(int)x][(int)y+1][(int)z];
            }
            for(int i = 0; i < 4*3; i++) {
                colorBuffer.put(usedLightLevel);
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
            float usedLightLevel = chunk.getWorld().getLightLevel(new Vec3i(worldX, (int)y, worldZ-1));
            for(int i = 0; i < 4*3; i++) {
                colorBuffer.put(usedLightLevel);
            }
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

    private static void initialize() {
        try {
            blockMap = TextureLoader.getTexture("PNG", NormalBlockRenderer.class.getResourceAsStream("/textures/block/blockSheet.png"));
            blockMap.setTextureFilter(GL_NEAREST);
        }catch(IOException e) {
            e.printStackTrace();
        }
        initialized = true;
    }

    public static Texture getBlockMap() {
        if(blockMap == null) {
            initialize();
        }
        return blockMap;
    }
}
