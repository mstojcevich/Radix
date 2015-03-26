package sx.lambda.mstojcevich.voxel.util.gl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import sx.lambda.mstojcevich.voxel.texture.TextureManager;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class FrameBuffer {

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int POSITION_FLOAT_COUNT = 2;
    private static final int TEXCOORD_FLOAT_COUNT = 2;
    private static final int FLOATS_PER_VERTEX = POSITION_FLOAT_COUNT+TEXCOORD_FLOAT_COUNT;
    private static final int VERTEX_SIZE_BYTES = FLOATS_PER_VERTEX*FLOAT_SIZE_BYTES;

    private int width, height;
    private int id = -1;
    private int texture;

    private int renderBuffer, drawVaoId, drawVboId;

    boolean firstTime = true;

    public FrameBuffer() {
        width = Display.getWidth();
        height = Display.getHeight();
        generate();
    }

    public void updateSize() {
        if(width != Display.getWidth() || height != Display.getHeight()) {
            width = Display.getWidth();
            height = Display.getHeight();

            generate();
        }
    }

    private void generate() {
        cleanup();

        id = glGenFramebuffersEXT();
        texture = glGenTextures();
        drawVaoId = glGenVertexArrays();
        drawVboId = glGenBuffers();

        renderBuffer = glGenRenderbuffers();
        glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, renderBuffer);
        glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_DEPTH_COMPONENT24, Display.getWidth(), Display.getHeight());
        glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, 0);

        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, Display.getWidth(), Display.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer)null);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

        bind();
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT,
                GL_TEXTURE_2D, texture, 0);
        glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_RENDERBUFFER_EXT, renderBuffer);
        unbind();
    }

    public void bind() {
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, id);
    }

    public void unbind() {
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
    }

    public void cleanup() {
        if(id > -1) {
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
            glDeleteFramebuffersEXT(id);
            glDeleteTextures(texture);
            glDeleteRenderbuffers(renderBuffer);

            glBindVertexArray(drawVaoId);
            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);
            glDisableVertexAttribArray(2);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glDeleteBuffers(drawVboId);
            glBindVertexArray(0);
            glDeleteVertexArrays(drawVaoId);

            id = -1;
            texture = -1;
        }
    }

    public void drawTexture(TextureManager tm, int x, int y, int w, int h, int positionAttrib, int textureAttrib) {
        tm.bindTexture(texture);

        int indices = 6*4; // 4 x XYUV
        FloatBuffer vertexData = BufferUtils.createFloatBuffer(indices);

        float[] bottomRight = new float[]{x+w, y+h, 1, 0};
        float[] bottomLeft = new float[]{x, y+h, 0, 0};
        float[] topLeft = new float[]{x, y, 0, 1};
        float[] topRight = new float[]{x+w, y, 1, 1};

        vertexData.put(topLeft).put(bottomLeft).put(topRight);
        vertexData.put(bottomRight).put(topRight).put(bottomLeft);
        vertexData.flip();

        glBindVertexArray(drawVaoId);
        glBindBuffer(GL_ARRAY_BUFFER, drawVboId);
        if(firstTime) {
            glBufferData(GL_ARRAY_BUFFER, vertexData, GL_DYNAMIC_DRAW);
        } else {
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexData);
        }
        glEnableVertexAttribArray(positionAttrib);
        glVertexAttribPointer(positionAttrib, POSITION_FLOAT_COUNT, GL_FLOAT, false, VERTEX_SIZE_BYTES, 0);
        int byteOffset = FLOAT_SIZE_BYTES*POSITION_FLOAT_COUNT;
        glEnableVertexAttribArray(textureAttrib);
        glVertexAttribPointer(textureAttrib, TEXCOORD_FLOAT_COUNT, GL_FLOAT, false, VERTEX_SIZE_BYTES, byteOffset);

        glDrawArrays(GL_TRIANGLES, 0, indices);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        firstTime = false;
    }

    public void drawTexture(TextureManager tm, int positionAttrib, int textureAttrib) {
        drawTexture(tm, 0, 0, width, height, positionAttrib, textureAttrib);
    }

}
