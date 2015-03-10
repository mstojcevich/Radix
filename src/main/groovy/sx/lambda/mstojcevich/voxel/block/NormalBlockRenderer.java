package sx.lambda.mstojcevich.voxel.block;

import groovy.transform.CompileStatic;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import sx.lambda.mstojcevich.voxel.VoxelGame;

import java.io.IOException;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

@CompileStatic
public class NormalBlockRenderer implements IBlockRenderer {

    private static final float TEXTURE_PERCENTAGE = 0.25f;

    private static Texture blockMap;

    private final int blockID;
    private final float u, v;

    private static boolean initialized;

	public NormalBlockRenderer(int blockID) {
		this.blockID = blockID;
        u = ((blockID%9)*TEXTURE_PERCENTAGE);
        v = ((blockID/9)*TEXTURE_PERCENTAGE);
	}

    @Override
    public void render(float x, float y, float z, boolean shouldRenderTop, boolean shouldRenderBottom, boolean shouldRenderLeft, boolean shouldRenderRight, boolean shouldRenderFront, boolean shouldRenderBack) {
        if(!initialized) {
            initialize();
        }
        float u2 = u+TEXTURE_PERCENTAGE-.001f;
        float v2 = v+TEXTURE_PERCENTAGE-.001f;
        if (shouldRenderTop) {
            glBegin(GL_QUADS);
            glNormal3f(0, 0, 1);
            glTexCoord2f(u, v); glVertex3f(x, y, z+1);
            glTexCoord2f(u, v2); glVertex3f(x+1, y, z+1);
            glTexCoord2f(u2, v2); glVertex3f(x+1, y + 1, z+1);
            glTexCoord2f(u2, v); glVertex3f(x, y + 1, z+1);
            glEnd();
        }
        //left
        if (shouldRenderLeft) {
            glBegin(GL_QUADS);
            glNormal3f(-1, 0, 0);
            glTexCoord2f(u, v); glVertex3f(x, y, z);
            glTexCoord2f(u, v2); glVertex3f(x, y, z + 1);
            glTexCoord2f(u2, v2); glVertex3f(x, y + 1, z + 1);
            glTexCoord2f(u2, v); glVertex3f(x, y + 1, z);
            glEnd();
        }
        if (shouldRenderRight) {
            //right
            glBegin(GL_QUADS);
            glNormal3f(1, 0, 0);
            glTexCoord2f(u, v); glVertex3f(x + 1, y, z);
            glTexCoord2f(u, v2); glVertex3f(x + 1, y + 1, z);
            glTexCoord2f(u2, v2); glVertex3f(x + 1, y + 1, z + 1);
            glTexCoord2f(u2, v); glVertex3f(x + 1, y, z + 1);
            glEnd();
        }
        if (shouldRenderFront) {
            //front
            glBegin(GL_QUADS);
            glNormal3f(0, -1, 0);
            glTexCoord2f(u, v); glVertex3f(x, y, z);
            glTexCoord2f(u, v2); glVertex3f(x + 1, y, z);
            glTexCoord2f(u2, v2); glVertex3f(x + 1, y, z + 1);
            glTexCoord2f(u2, v); glVertex3f(x, y, z + 1);
            glEnd();
        }
        //back
        if (shouldRenderBack) {
            glBegin(GL_QUADS);
            glNormal3f(0, 1, 0);
            glTexCoord2f(u, v); glVertex3f(x + 1, y + 1, z);
            glTexCoord2f(u, v2); glVertex3f(x, y + 1, z);
            glTexCoord2f(u2, v2); glVertex3f(x, y + 1, z + 1);
            glTexCoord2f(u2, v); glVertex3f(x + 1, y + 1, z + 1);
            glEnd();
        }
        //bottom
        if(shouldRenderBottom) {
            glBegin(GL_QUADS);
            glNormal3f(0, 0, -1);
            glTexCoord2f(u, v); glVertex3f(x + 1, y, z);
            glTexCoord2f(u, v2); glVertex3f(x, y, z);
            glTexCoord2f(u2, v2); glVertex3f(x, y + 1, z);
            glTexCoord2f(u2, v); glVertex3f(x + 1, y + 1, z);
            glEnd();
        }
    }

    @Override
    public void renderVBO(float x, float y, float z,
                          FloatBuffer vertexBuffer, FloatBuffer textureBuffer, FloatBuffer normalBuffer,
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
        }
    }

    @Override
    public void render2d(float x, float y, float width) {
        if(!initialized) {
            initialize();
        }
        float u2 = u+TEXTURE_PERCENTAGE-.001f;
        float v2 = v+TEXTURE_PERCENTAGE-.001f;
        VoxelGame.getInstance().getTextureManager().bindTexture(blockMap.getTextureID());
        glBegin(GL_QUADS);
        glTexCoord2f(u, v);glVertex2f(x, y);
        glTexCoord2f(u, v2);glVertex2f(x, y+width);
        glTexCoord2f(u2, v2);glVertex2f(x+width, y+width);
        glTexCoord2f(u2, v);glVertex2f(x+width, y);
        glEnd();
    }           

    @Override
    public void prerender() {
        if(!initialized)initialize();
        VoxelGame.getInstance().getTextureManager().bindTexture(blockMap.getTextureID());
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
