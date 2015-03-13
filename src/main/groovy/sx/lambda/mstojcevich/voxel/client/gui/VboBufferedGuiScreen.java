package sx.lambda.mstojcevich.voxel.client.gui;

import com.sun.istack.internal.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public abstract class VboBufferedGuiScreen implements GuiScreen {

    private int vertexVbo;
    private int colorVbo;
    private int numVertices;

    protected boolean initialized;
    protected boolean renderedBefore;

    @Override
    public void init() {
        IntBuffer buffer = BufferUtils.createIntBuffer(2);
        GL15.glGenBuffers(buffer);
        vertexVbo = buffer.get(0);
        colorVbo = buffer.get(1);
    }

    @Override
    public void render(boolean inGame) {
        if(!renderedBefore) {
            if(!initialized) {
                this.init();
            }
            rerender();
            renderedBefore = true;
        } else {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVbo);
            GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVbo);
            GL11.glColorPointer(4, GL11.GL_FLOAT, 0, 0);

            GL11.glDrawArrays(GL11.GL_QUADS, 0, numVertices);
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
        numVertices = vertices.capacity() / 3;
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

}