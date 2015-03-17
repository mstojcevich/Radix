package sx.lambda.mstojcevich.voxel.client.gui;

import com.sun.istack.internal.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public abstract class VboBufferedGuiScreen implements GuiScreen {

    private int vertexVbo, colorVbo;
    private int backgroundVertexVbo, backgroundColorVbo;
    private int numVertices;

    protected boolean initialized;
    protected boolean renderedBefore, renderedBgBefore;

    @Override
    public void init() {
        IntBuffer buffer = BufferUtils.createIntBuffer(4);
        GL15.glGenBuffers(buffer);
        vertexVbo = buffer.get(0);
        colorVbo = buffer.get(1);
        backgroundVertexVbo = buffer.get(2);
        backgroundColorVbo = buffer.get(3);
    }

    @Override
    public void render(boolean inGame) {
        if(!renderedBefore) {
            if(!initialized) {
                this.init();
            }
            rerenderBackground();
            rerender();
            renderedBefore = true;
        } else {
            renderBackground();

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVbo);
            GL11.glVertexPointer(2, GL11.GL_FLOAT, 0, 0);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVbo);
            GL11.glColorPointer(4, GL11.GL_FLOAT, 0, 0);

            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, numVertices);
        }
    }

    @Override
    public void finish() {
        GL15.glDeleteBuffers(vertexVbo);
        GL15.glDeleteBuffers(colorVbo);
        initialized = false;
        renderedBefore = false;
    }

    protected abstract void rerender();

    protected void renderVbo(FloatBuffer vertices, FloatBuffer colors) {
        numVertices = vertices.capacity() / 2;
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colors, GL15.GL_STATIC_DRAW);
    }

    /**
     * Updates the VBO with new values for vertices or colors
     * @param vertices New vertices (if null, doesn't update vertices)
     * @param colors New colors (if null, doesn't update colors)
     */
    protected void updateVbo(@Nullable FloatBuffer vertices, @Nullable FloatBuffer colors) {
        if(vertices != null) {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVbo);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertices);
        }
        if(colors != null) {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVbo);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, colors);
        }
    }

    private void rerenderBackground() {
        FloatBuffer vertices = BufferUtils.createFloatBuffer(6*2);
        FloatBuffer colors = BufferUtils.createFloatBuffer(6*4);

        int x1 = 0, y1 = 0, x2 = Display.getWidth(), y2 = Display.getHeight();

        float[] bottomRight = new float[]{x2, y2};
        float[] bottomLeft = new float[]{x1, y2};
        float[] topLeft = new float[]{x1, y1};
        float[] topRight = new float[]{x2, y1};

        vertices.put(topLeft).put(bottomLeft).put(topRight);
        vertices.put(bottomRight).put(topRight).put(bottomLeft);
        vertices.flip();

        float [] cls = new float[]{
                0, 0, 0, 1,
                1, 0, 0, 1,
                0, 0, 0, 1,
                1, 0, 0, 1,
                0, 0, 0, 1,
                1, 0, 0, 1,
        };
        colors.put(cls);
        colors.flip();

        if(!renderedBgBefore) {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, backgroundVertexVbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, backgroundColorVbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colors, GL15.GL_STATIC_DRAW);
        } else {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, backgroundVertexVbo);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertices);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, backgroundColorVbo);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, colors);
        }

        renderedBgBefore = true;
    }

    private void renderBackground() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, backgroundVertexVbo);
        GL11.glVertexPointer(2, GL11.GL_FLOAT, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, backgroundColorVbo);
        GL11.glColorPointer(4, GL11.GL_FLOAT, 0, 0);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

}