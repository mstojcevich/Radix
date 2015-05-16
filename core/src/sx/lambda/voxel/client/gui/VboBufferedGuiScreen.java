package sx.lambda.voxel.client.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;
import sx.lambda.voxel.VoxelGameClient;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public abstract class VboBufferedGuiScreen implements GuiScreen {

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int POSITION_FLOAT_COUNT = 2;
    private static final int COLOR_FLOAT_COUNT = 4;
    private static final int PARTS_PER_VERTEX = POSITION_FLOAT_COUNT + COLOR_FLOAT_COUNT;
    private static final int VERTEX_SIZE_BYTES = PARTS_PER_VERTEX*FLOAT_SIZE_BYTES;

    private int vertexVbo;
    private int backgroundVertexVbo;
    private int numVertices;

    protected boolean initialized;
    protected boolean renderedBefore, renderedBgBefore;

    @Override
    public void init() {
        IntBuffer buffer = BufferUtils.newIntBuffer(2);
        Gdx.gl.glGenBuffers(2, buffer);
        vertexVbo = buffer.get(0);
        backgroundVertexVbo = buffer.get(1);
    }

    @Override
    public void render(boolean inGame) {
        VoxelGameClient.getInstance().getGuiShader().disableTexturing();
        if(!renderedBefore) {
            if(!initialized) {
                this.init();
            }
            rerenderBackground();
            rerender();
            renderedBefore = true;
        } else {
            renderBackground();

            Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, vertexVbo);
            Gdx.gl.glEnableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getPositionAttrib());
            Gdx.gl.glVertexAttribPointer(VoxelGameClient.getInstance().getGuiShader().getPositionAttrib(), POSITION_FLOAT_COUNT, GL20.GL_FLOAT, false, VERTEX_SIZE_BYTES, 0);
            int byteOffset = FLOAT_SIZE_BYTES*POSITION_FLOAT_COUNT;
            Gdx.gl.glEnableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getColorAttrib());
            Gdx.gl.glVertexAttribPointer(VoxelGameClient.getInstance().getGuiShader().getColorAttrib(), COLOR_FLOAT_COUNT, GL20.GL_FLOAT, false, VERTEX_SIZE_BYTES, byteOffset);

            Gdx.gl.glDrawArrays(GL20.GL_TRIANGLES, 0, numVertices);

            Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
            Gdx.gl30.glBindVertexArray(0);

            Gdx.gl.glDisableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getPositionAttrib());
            Gdx.gl.glDisableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getColorAttrib());
        }
    }

    @Override
    public void finish() {
        IntBuffer toDelete = BufferUtils.newIntBuffer(2);
        toDelete.put(vertexVbo).put(backgroundVertexVbo);
        Gdx.gl.glDeleteBuffers(2, toDelete);
        initialized = false;
        renderedBefore = false;
        renderedBgBefore = false;
    }

    protected abstract void rerender();

    protected void renderVbo(FloatBuffer vertices) {
        numVertices = vertices.capacity() / PARTS_PER_VERTEX;
        Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, vertexVbo);
        Gdx.gl.glBufferData(GL20.GL_ARRAY_BUFFER, vertices.remaining() << 2, vertices, GL20.GL_STATIC_DRAW);
    }

    /**
     * Updates the VBO with new values for vertices or colors
     * @param vertices New vertices (if null, doesn't update vertices)
     * @param colors New colors (if null, doesn't update colors)
     */
    protected void updateVbo(FloatBuffer vertices, FloatBuffer colors) {
        if(vertices != null) {
            Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, vertexVbo);
            Gdx.gl.glBufferSubData(GL20.GL_ARRAY_BUFFER, 0, vertices.remaining() << 2, vertices);
        }
    }

    private void rerenderBackground() {
        FloatBuffer vertices = BufferUtils.newFloatBuffer(6*6);

        int x1 = 0, y1 = 0, x2 = Gdx.graphics.getWidth(), y2 = Gdx.graphics.getHeight();

        float[] bottomRight = new float[]{x2, y2, 0.2f, 0.2f, 0.2f, 1};
        float[] bottomLeft = new float[]{x1, y2, 0.2f, 0.2f, 0.2f, 1};
        float[] topLeft = new float[]{x1, y1, 0.4f, 0.4f, 0.4f, 1};
        float[] topRight = new float[]{x2, y1, 0.4f, 0.4f, 0.4f, 1};

        vertices.put(topLeft).put(bottomLeft).put(topRight);
        vertices.put(bottomRight).put(topRight).put(bottomLeft);
        vertices.flip();

        if(!renderedBgBefore) {
            Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, backgroundVertexVbo);
            Gdx.gl.glBufferData(GL20.GL_ARRAY_BUFFER, vertices.remaining() << 2, vertices, GL20.GL_STATIC_DRAW);
        } else {
            Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, backgroundVertexVbo);
            Gdx.gl.glBufferSubData(GL20.GL_ARRAY_BUFFER, 0, vertices.remaining() << 2, vertices);
        }

        renderedBgBefore = true;
    }

    private void renderBackground() {
        Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, backgroundVertexVbo);
        Gdx.gl.glEnableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getPositionAttrib());
        Gdx.gl.glVertexAttribPointer(VoxelGameClient.getInstance().getGuiShader().getPositionAttrib(), POSITION_FLOAT_COUNT, GL20.GL_FLOAT, false, VERTEX_SIZE_BYTES, 0);
        int byteOffset = FLOAT_SIZE_BYTES*POSITION_FLOAT_COUNT;
        Gdx.gl.glEnableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getColorAttrib());
        Gdx.gl.glVertexAttribPointer(VoxelGameClient.getInstance().getGuiShader().getColorAttrib(), COLOR_FLOAT_COUNT, GL20.GL_FLOAT, false, VERTEX_SIZE_BYTES, byteOffset);

        Gdx.gl.glDrawArrays(GL20.GL_TRIANGLES, 0, 6);
    }

}