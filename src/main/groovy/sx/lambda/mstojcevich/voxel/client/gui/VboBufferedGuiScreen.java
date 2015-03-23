package sx.lambda.mstojcevich.voxel.client.gui;

import com.sun.istack.internal.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import sx.lambda.mstojcevich.voxel.VoxelGame;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public abstract class VboBufferedGuiScreen implements GuiScreen {

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int POSITION_FLOAT_COUNT = 2;
    private static final int COLOR_FLOAT_COUNT = 4;
    private static final int PARTS_PER_VERTEX = POSITION_FLOAT_COUNT + COLOR_FLOAT_COUNT;
    private static final int VERTEX_SIZE_BYTES = PARTS_PER_VERTEX*FLOAT_SIZE_BYTES;

    private int vertexVbo;
    private int backgroundVertexVbo, backgroundColorVbo;
    private int numVertices;

    protected boolean initialized;
    protected boolean renderedBefore, renderedBgBefore;

    @Override
    public void init() {
        IntBuffer buffer = BufferUtils.createIntBuffer(3);
        GL15.glGenBuffers(buffer);
        vertexVbo = buffer.get(0);
        backgroundVertexVbo = buffer.get(1);
        backgroundColorVbo = buffer.get(2);
    }

    @Override
    public void render(boolean inGame) {
        VoxelGame.getInstance().getGuiShader().enableColors();
        VoxelGame.getInstance().getGuiShader().disableTexturing();
        if(!renderedBefore) {
            if(!initialized) {
                this.init();
            }
            rerenderBackground();
            rerender();
            renderedBefore = true;
        } else {
            renderBackground();

            //TODO I was here before my laptop died. Move stuff over to use interleaved.

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVbo);
            glEnableVertexAttribArray(VoxelGame.getInstance().getGuiShader().getPositionAttrib());
            glVertexAttribPointer(VoxelGame.getInstance().getGuiShader().getPositionAttrib(), POSITION_FLOAT_COUNT, GL_FLOAT, false, VERTEX_SIZE_BYTES, 0);
            int byteOffset = FLOAT_SIZE_BYTES*POSITION_FLOAT_COUNT;
            glEnableVertexAttribArray(VoxelGame.getInstance().getGuiShader().getColorAttrib());
            glVertexAttribPointer(VoxelGame.getInstance().getGuiShader().getTexCoordAttrib(), COLOR_FLOAT_COUNT, GL_FLOAT, false, VERTEX_SIZE_BYTES, byteOffset);

            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, numVertices);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }
    }

    @Override
    public void finish() {
        GL15.glDeleteBuffers(vertexVbo);
        initialized = false;
        renderedBefore = false;
    }

    protected abstract void rerender();

    protected void renderVbo(FloatBuffer vertices) {
        numVertices = vertices.capacity() / PARTS_PER_VERTEX;
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
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
                0.4f, 0.4f, 0.4f, 1,
                0.25f, 0.25f, 0.25f, 1,
                0.4f, 0.4f, 0.4f, 1,
                0.25f, 0.25f, 0.25f, 1,
                0.4f, 0.4f, 0.4f, 1,
                0.25f, 0.25f, 0.25f, 1,
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