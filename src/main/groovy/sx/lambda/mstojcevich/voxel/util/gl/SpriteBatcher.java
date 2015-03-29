package sx.lambda.mstojcevich.voxel.util.gl;

import org.lwjgl.BufferUtils;
import sx.lambda.mstojcevich.voxel.VoxelGame;
import sx.lambda.mstojcevich.voxel.texture.TextureManager;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * "Batches" all 2d sprites drawn in a frame to one VBO
 *
 * This is pretty generic and will be slower than making a more specific implementation to the problem
 */
public class SpriteBatcher {
    //TODO use index buffers for rects

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int POSITION_FLOAT_COUNT = 2;
    private static final int TEXCOORD_FLOAT_COUNT = 2;
    private static final int PARTS_PER_VERTEX = POSITION_FLOAT_COUNT + TEXCOORD_FLOAT_COUNT;
    private static final int VERTEX_SIZE_BYTES = PARTS_PER_VERTEX*FLOAT_SIZE_BYTES;
    private static final int VBO_MODE = GL_STREAM_DRAW; // We're going to draw every frame
    private static final int VERTICES_PER_RECT = 6;

    private int vbo = -1, vao = -1;
    private boolean initialized;

    private Queue<float[]> vertexDrawQueue = new ConcurrentLinkedQueue<>();

    private final TextureManager textureManager;

    private int numIndices;

    /**
     * @param textureManager Texture manager to bind textures with
     */
    public SpriteBatcher(TextureManager textureManager) {
        this.textureManager = textureManager;
    }

    public void init() {
        if(!initialized) {
            vbo = glGenBuffers();
            vao = glGenVertexArrays();

            initialized = true;
        }
    }

    public void cleanup() {
        if(initialized) {
            glDeleteBuffers(vbo);
            glDeleteVertexArrays(vao);
            initialized = false;
        }
    }

    /**
     * Draws a textured rect to the VBO
     * @param x1 Starting horizontal position
     * @param y1 Starting vertical position
     * @param x2 Ending horizontal position
     * @param y2 Ending vertical position
     * @param u1 Beginning horizontal texture percentage
     * @param v1 Beginning vertical texture percentage
     * @param u2 Ending horizontal texture percentage
     * @param v2 Ending vertical texture percentage
     */
    public void drawTexturedRect(int x1, int y1, int x2, int y2, float u1, float v1, float u2, float v2) {
        float[] vertices = new float[] {
                // Top left
                x1, y1, u1, v2,
                // Bottom left
                x1, y2, u1, v1,
                // Top Right
                x2, y1, u2, v2,
                // Bottom right
                x2, y2, u2, v1,
                // Top right
                x2, y1, u2, v2,
                // Bottom left
                x1, y2, u1, v1
        };

        vertexDrawQueue.add(vertices);

        numIndices += VERTICES_PER_RECT;
    }

    /**
     * Renders the contents of the sprite batch then clears it
     *
     * @param textureID Texture ID to draw with
     */
    public void render(int textureID) {
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertexDrawQueue.size()*PARTS_PER_VERTEX*VERTICES_PER_RECT);
        float[] vertex;
        while((vertex = vertexDrawQueue.poll()) != null) {
            verticesBuffer.put(vertex);
        }
        verticesBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, VBO_MODE);

        textureManager.bindTexture(textureID);
        glBindVertexArray(vao);
        glEnableVertexAttribArray(VoxelGame.getInstance().getGuiShader().getPositionAttrib());
        glVertexAttribPointer(VoxelGame.getInstance().getGuiShader().getPositionAttrib(), POSITION_FLOAT_COUNT, GL_FLOAT, false, VERTEX_SIZE_BYTES, 0);
        int byteOffset = FLOAT_SIZE_BYTES*POSITION_FLOAT_COUNT; // we added positions
        glEnableVertexAttribArray(VoxelGame.getInstance().getGuiShader().getTexCoordAttrib());
        glVertexAttribPointer(VoxelGame.getInstance().getGuiShader().getTexCoordAttrib(), TEXCOORD_FLOAT_COUNT, GL_FLOAT, false, VERTEX_SIZE_BYTES, byteOffset);

        glDrawArrays(GL_TRIANGLES, 0, numIndices);

        numIndices = 0; // The draw is done, the queue is empty, we're starting fresh

        glDisableVertexAttribArray(VoxelGame.getInstance().getGuiShader().getPositionAttrib());
        glDisableVertexAttribArray(VoxelGame.getInstance().getGuiShader().getTexCoordAttrib());

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public StaticRender renderStatic(int textureID) {
        int vbo = glGenBuffers();
        int vao = glGenVertexArrays();

        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertexDrawQueue.size()*PARTS_PER_VERTEX*VERTICES_PER_RECT);
        float[] vertex;
        while((vertex = vertexDrawQueue.poll()) != null) {
            verticesBuffer.put(vertex);
        }
        verticesBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, VBO_MODE);

        glBindVertexArray(vao);
        glEnableVertexAttribArray(VoxelGame.getInstance().getGuiShader().getPositionAttrib());
        glVertexAttribPointer(VoxelGame.getInstance().getGuiShader().getPositionAttrib(), POSITION_FLOAT_COUNT, GL_FLOAT, false, VERTEX_SIZE_BYTES, 0);
        int byteOffset = FLOAT_SIZE_BYTES*POSITION_FLOAT_COUNT; // we added positions
        glEnableVertexAttribArray(VoxelGame.getInstance().getGuiShader().getTexCoordAttrib());
        glVertexAttribPointer(VoxelGame.getInstance().getGuiShader().getTexCoordAttrib(), TEXCOORD_FLOAT_COUNT, GL_FLOAT, false, VERTEX_SIZE_BYTES, byteOffset);

        int indiciesUsed = numIndices;
        numIndices = 0; // The draw is done, the queue is empty, we're starting fresh

        glDisableVertexAttribArray(VoxelGame.getInstance().getGuiShader().getPositionAttrib());
        glDisableVertexAttribArray(VoxelGame.getInstance().getGuiShader().getTexCoordAttrib());

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        return new VboStaticRender(vbo, vao, textureID, textureManager, indiciesUsed);
    }

    public static interface StaticRender {
        void render();

        void destroy();
    }

    private static class VboStaticRender implements StaticRender {
        private final int vboID, vaoID, textureID;
        private final int numVertices;
        private final TextureManager manager;

        public VboStaticRender(int vboID, int vaoID, int textureID, TextureManager manager, int numVertices) {
            this.vboID = vboID;
            this.vaoID = vaoID;
            this.textureID = textureID;
            this.manager = manager;
            this.numVertices = numVertices;
        }

        @Override
        public void render() {
            manager.bindTexture(textureID);

            glBindVertexArray(vaoID);
            glEnableVertexAttribArray(VoxelGame.getInstance().getGuiShader().getPositionAttrib());
            glEnableVertexAttribArray(VoxelGame.getInstance().getGuiShader().getTexCoordAttrib());

            glDrawArrays(GL_TRIANGLES, 0, numVertices);

            glDisableVertexAttribArray(VoxelGame.getInstance().getGuiShader().getPositionAttrib());
            glDisableVertexAttribArray(VoxelGame.getInstance().getGuiShader().getTexCoordAttrib());

            glBindVertexArray(0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }

        @Override
        public void destroy() {
            glDeleteBuffers(vboID);
            glDeleteVertexArrays(vaoID);
        }
    }

}
