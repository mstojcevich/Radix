package sx.lambda.voxel.util.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.BufferUtils;
import sx.lambda.voxel.texture.TextureManager;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static com.badlogic.gdx.graphics.GL20.*;

public class FrameBuffer {

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int POSITION_FLOAT_COUNT = 2;
    private static final int TEXCOORD_FLOAT_COUNT = 2;
    private static final int FLOATS_PER_VERTEX = POSITION_FLOAT_COUNT+TEXCOORD_FLOAT_COUNT;
    private static final int VERTEX_SIZE_BYTES = FLOATS_PER_VERTEX*FLOAT_SIZE_BYTES;

    private int width, height;
    private int id = -1;
    private int texture;

    private int renderBuffer, drawVboId;

    boolean firstTime = true;

    public FrameBuffer() {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        generate();
    }

    public void updateSize() {
        if(width != Gdx.graphics.getWidth() || height != Gdx.graphics.getHeight()) {
            width = Gdx.graphics.getWidth();
            height = Gdx.graphics.getHeight();

            generate();
        }
    }

    private void generate() {
        cleanup();

        id = Gdx.gl.glGenFramebuffer();
        texture = Gdx.gl.glGenTexture();
        drawVboId = Gdx.gl.glGenBuffer();

        IntBuffer buffer = BufferUtils.newIntBuffer(1);
        Gdx.gl.glGenRenderbuffers(1, buffer);
        renderBuffer = buffer.get();
        Gdx.gl.glBindRenderbuffer(GL_RENDERBUFFER, renderBuffer);
        Gdx.gl.glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glBindRenderbuffer(GL_RENDERBUFFER, 0);

        Gdx.gl.glBindTexture(GL_TEXTURE_2D, texture);
        Gdx.gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA /* TODO was GL_RGBA8 */, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer)null);
        Gdx.gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        Gdx.gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        Gdx.gl.glBindTexture(GL_TEXTURE_2D, 0);

        bind();
        Gdx.gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                GL_TEXTURE_2D, texture, 0);
        Gdx.gl.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderBuffer);
        unbind();
    }

    public void bind() {
        Gdx.gl.glBindFramebuffer(GL_FRAMEBUFFER, id);
    }

    public void unbind() {
        Gdx.gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void cleanup() {
        if(id > -1) {
            Gdx.gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
            Gdx.gl.glDeleteFramebuffer(id);
            Gdx.gl.glDeleteTexture(texture);
            Gdx.gl.glDeleteRenderbuffer(renderBuffer);

            Gdx.gl.glDisableVertexAttribArray(0);
            Gdx.gl.glDisableVertexAttribArray(1);
            Gdx.gl.glDisableVertexAttribArray(2);
            Gdx.gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
            Gdx.gl.glDeleteBuffer(drawVboId);

            id = -1;
            texture = -1;
        }
    }

    public void drawTexture(TextureManager tm, int x, int y, int w, int h, int positionAttrib, int textureAttrib) {
        tm.bindTexture(texture);

        int indices = 6*4; // 4 x XYUV
        FloatBuffer vertexData = BufferUtils.newFloatBuffer(indices);

        float[] bottomRight = new float[]{x+w, y+h, 1, 0};
        float[] bottomLeft = new float[]{x, y+h, 0, 0};
        float[] topLeft = new float[]{x, y, 0, 1};
        float[] topRight = new float[]{x+w, y, 1, 1};

        vertexData.put(topLeft).put(bottomLeft).put(topRight);
        vertexData.put(bottomRight).put(topRight).put(bottomLeft);
        vertexData.flip();

        Gdx.gl.glBindBuffer(GL_ARRAY_BUFFER, drawVboId);
        if(firstTime) {
            Gdx.gl.glBufferData(GL_ARRAY_BUFFER, vertexData.remaining()<<2, vertexData, GL_DYNAMIC_DRAW);
        } else {
            Gdx.gl.glBufferSubData(GL_ARRAY_BUFFER, 0, vertexData.remaining()<<2, vertexData);
        }
        Gdx.gl.glEnableVertexAttribArray(positionAttrib);
        Gdx.gl.glVertexAttribPointer(positionAttrib, POSITION_FLOAT_COUNT, GL_FLOAT, false, VERTEX_SIZE_BYTES, 0);
        int byteOffset = FLOAT_SIZE_BYTES*POSITION_FLOAT_COUNT;
        Gdx.gl.glEnableVertexAttribArray(textureAttrib);
        Gdx.gl.glVertexAttribPointer(textureAttrib, TEXCOORD_FLOAT_COUNT, GL_FLOAT, false, VERTEX_SIZE_BYTES, byteOffset);

        Gdx.gl.glDrawArrays(GL_TRIANGLES, 0, indices);
        Gdx.gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

        firstTime = false;
    }

    public void drawTexture(TextureManager tm, int positionAttrib, int textureAttrib) {
        drawTexture(tm, 0, 0, width, height, positionAttrib, textureAttrib);
    }

}
