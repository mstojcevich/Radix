package sx.lambda.mstojcevich.voxel.util.gl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import sx.lambda.mstojcevich.voxel.texture.TextureManager;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class FrameBuffer {

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int POSITION_FLOAT_COUNT = 3;
    private static final int COLOR_FLOAT_COUNT = 4;
    private static final int TEXCOORD_FLOAT_COUNT = 2;
    private static final int FLOATS_PER_VERTEX = POSITION_FLOAT_COUNT+COLOR_FLOAT_COUNT+TEXCOORD_FLOAT_COUNT;
    private static final int VERTEX_SIZE_BYTES = FLOATS_PER_VERTEX*FLOAT_SIZE_BYTES;

    private int width, height;
    private int id = -1;
    private int texture;
    private int drawVboId;
    private int drawVaoId;

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

        glBindTexture(GL_TEXTURE_2D, texture);

        bind();
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT,
                GL_TEXTURE_2D, texture, 0);
        unbind();
    }

    public void bind() {
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, id);
    }

    public void unbind() {
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
    }

    private void cleanup() {
        if(id > -1) {
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
            glDeleteFramebuffersEXT(id);
            glDeleteTextures(texture);

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

    public void drawTexture(TextureManager tm) {
        tm.bindTexture(texture);

        glColor4f(1, 1, 1, 1);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);glVertex2f(0, 0);
        glTexCoord2f(0, 1);glVertex2f(0, Display.getHeight());
        glTexCoord2f(1, 1);glVertex2f(Display.getWidth(), Display.getHeight());
        glTexCoord2f(1, 0);glVertex2f(Display.getWidth(), 0);
        glEnd();

//        int indices = 6*8; // 6 x XYRGBAUV
//        FloatBuffer vertexData = BufferUtils.createFloatBuffer(indices);
//
//        float[] bottomRight = new float[]{width, height, 1, 1, 1, 1, 1, 1};
//        float[] bottomLeft = new float[]{0, height, 1, 1, 1, 1, 0, 1};
//        float[] topLeft = new float[]{0, 0, 1, 1, 1, 1, 0, 0};
//        float[] topRight = new float[]{width, 0, 1, 1, 1, 1, 1, 0};
//
//        vertexData.put(topLeft).put(topRight).put(bottomLeft);
//        vertexData.put(topRight).put(bottomRight).put(bottomLeft);
//        vertexData.flip();
//
//        glBindVertexArray(drawVaoId);
//        glBindBuffer(GL_ARRAY_BUFFER, drawVboId);
//        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_DYNAMIC_DRAW);
//        glEnableVertexAttribArray(0);
//        glVertexAttribPointer(0, POSITION_FLOAT_COUNT, GL_FLOAT, false, VERTEX_SIZE_BYTES, 0);
//        int byteOffset = FLOAT_SIZE_BYTES*POSITION_FLOAT_COUNT;
//        glEnableVertexAttribArray(1);
//        glVertexAttribPointer(1, COLOR_FLOAT_COUNT, GL_FLOAT, false, VERTEX_SIZE_BYTES, byteOffset);
//        byteOffset += FLOAT_SIZE_BYTES*COLOR_FLOAT_COUNT;
//        glEnableVertexAttribArray(2);
//        glVertexAttribPointer(2, TEXCOORD_FLOAT_COUNT, GL_FLOAT, false, VERTEX_SIZE_BYTES, byteOffset);
//
//        glDrawArrays(GL_TRIANGLES, 0, indices);
//        glBindBuffer(GL_ARRAY_BUFFER, 0);
//        glBindVertexArray(0);
    }

}
