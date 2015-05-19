//package sx.lambda.voxel.util.gl;
//
//import com.badlogic.gdx.Gdx;
//import static com.badlogic.gdx.graphics.GL20.*;
//
//import com.badlogic.gdx.utils.BufferUtils;
//import sx.lambda.voxel.VoxelGameClient;
//import sx.lambda.voxel.texture.TextureManager;
//
//import javax.security.auth.Destroyable;
//import java.nio.FloatBuffer;
//import java.nio.IntBuffer;
//import java.util.Queue;
//import java.util.concurrent.ConcurrentLinkedQueue;
//
////TODO convert to use GDX SpriteBatch as backend
//
///**
// * "Batches" all 2d sprites drawn in a frame to one VBO
// *
// * This is pretty generic and will be slower than making a more specific implementation to the problem
// */
//public class SpriteBatcher {
//    //TODO use index buffers for rects
//
//    private static final int FLOAT_SIZE_BYTES = 4;
//    private static final int POSITION_FLOAT_COUNT = 2;
//    private static final int TEXCOORD_FLOAT_COUNT = 2;
//    private static final int PARTS_PER_VERTEX = POSITION_FLOAT_COUNT + TEXCOORD_FLOAT_COUNT;
//    private static final int VERTEX_SIZE_BYTES = PARTS_PER_VERTEX*FLOAT_SIZE_BYTES;
//    private static final int VBO_MODE = GL_STREAM_DRAW; // We're going to draw every frame
//    private static final int VERTICES_PER_RECT = 6;
//
//    private int vbo = -1;
//    private boolean initialized;
//
//    private Queue<float[]> vertexDrawQueue = new ConcurrentLinkedQueue<>();
//
//    private final TextureManager textureManager;
//
//    private int numIndices;
//
//    /**
//     * @param textureManager Texture manager to bind textures with
//     */
//    public SpriteBatcher(TextureManager textureManager) {
//        this.textureManager = textureManager;
//    }
//
//    public void init() {
//        if(!initialized) {
//            IntBuffer buffers = BufferUtils.newIntBuffer(1);
//            Gdx.gl20.glGenBuffers(1, buffers);
//            vbo = buffers.get();
//
//            initialized = true;
//        }
//    }
//
//    public void cleanup() {
//        if(initialized) {
//            IntBuffer buffer = BufferUtils.newIntBuffer(1);
//            buffer.put(vbo);
//            buffer.flip();
//            Gdx.gl20.glDeleteBuffers(1, buffer);
//            initialized = false;
//        }
//    }
//
//    /**
//     * Draws a textured rect to the VBO
//     * @param x1 Starting horizontal position
//     * @param y1 Starting vertical position
//     * @param x2 Ending horizontal position
//     * @param y2 Ending vertical position
//     * @param u1 Beginning horizontal texture percentage
//     * @param v1 Beginning vertical texture percentage
//     * @param u2 Ending horizontal texture percentage
//     * @param v2 Ending vertical texture percentage
//     */
//    public void drawTexturedRect(int x1, int y1, int x2, int y2, float u1, float v1, float u2, float v2) {
//        float[] vertices = new float[] {
//                // Top left
//                x1, y1, u1, v2,
//                // Bottom left
//                x1, y2, u1, v1,
//                // Top Right
//                x2, y1, u2, v2,
//                // Bottom right
//                x2, y2, u2, v1,
//                // Top right
//                x2, y1, u2, v2,
//                // Bottom left
//                x1, y2, u1, v1
//        };
//
//        vertexDrawQueue.add(vertices);
//
//        numIndices += VERTICES_PER_RECT;
//    }
//
//    /**
//     * Renders the contents of the sprite batch then clears it
//     *
//     * @param textureID Texture ID to draw with
//     */
//    public void render(int textureID) {
//        FloatBuffer verticesBuffer = BufferUtils.newFloatBuffer(vertexDrawQueue.size()*PARTS_PER_VERTEX*VERTICES_PER_RECT);
//        float[] vertex;
//        while((vertex = vertexDrawQueue.poll()) != null) {
//            verticesBuffer.put(vertex);
//        }
//        verticesBuffer.flip();
//
//        Gdx.gl20.glBindBuffer(GL_ARRAY_BUFFER, vbo);
//        Gdx.gl20.glBufferData(GL_ARRAY_BUFFER, verticesBuffer.remaining() << 2, verticesBuffer, VBO_MODE);
//
//        textureManager.bindTexture(textureID);
//        Gdx.gl20.glEnableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getPositionAttrib());
//        Gdx.gl20.glVertexAttribPointer(VoxelGameClient.getInstance().getGuiShader().getPositionAttrib(), POSITION_FLOAT_COUNT, GL_FLOAT, false, VERTEX_SIZE_BYTES, 0);
//        int byteOffset = FLOAT_SIZE_BYTES*POSITION_FLOAT_COUNT; // we added positions
//        Gdx.gl20.glEnableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getTexCoordAttrib());
//        Gdx.gl20.glVertexAttribPointer(VoxelGameClient.getInstance().getGuiShader().getTexCoordAttrib(), TEXCOORD_FLOAT_COUNT, GL_FLOAT, false, VERTEX_SIZE_BYTES, byteOffset);
//
//        Gdx.gl20.glDrawArrays(GL_TRIANGLES, 0, numIndices);
//
//        numIndices = 0; // The draw is done, the queue is empty, we're starting fresh
//
//        Gdx.gl20.glDisableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getPositionAttrib());
//        Gdx.gl20.glDisableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getTexCoordAttrib());
//    }
//
//    public StaticRender renderStatic(int textureID) {
//        IntBuffer buffer = BufferUtils.newIntBuffer(1);
//        Gdx.gl20.glGenBuffers(1, buffer);
//        int vbo = buffer.get();
//        buffer = BufferUtils.newIntBuffer(1);
//        Gdx.gl30.glGenVertexArrays(1, buffer);
//        int vao = buffer.get();
//
//        FloatBuffer verticesBuffer = BufferUtils.newFloatBuffer(vertexDrawQueue.size() * PARTS_PER_VERTEX * VERTICES_PER_RECT);
//        float[] vertex;
//        while((vertex = vertexDrawQueue.poll()) != null) {
//            verticesBuffer.put(vertex);
//        }
//        verticesBuffer.flip();
//
//        Gdx.gl20.glBindBuffer(GL_ARRAY_BUFFER, vbo);
//        Gdx.gl20.glBufferData(GL_ARRAY_BUFFER, verticesBuffer.remaining() << 2, verticesBuffer, GL_STATIC_DRAW);
//
//        Gdx.gl30.glBindVertexArray(vao);
//        Gdx.gl20.glEnableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getPositionAttrib());
//        Gdx.gl20.glVertexAttribPointer(VoxelGameClient.getInstance().getGuiShader().getPositionAttrib(), POSITION_FLOAT_COUNT, GL_FLOAT, false, VERTEX_SIZE_BYTES, 0);
//        int byteOffset = FLOAT_SIZE_BYTES*POSITION_FLOAT_COUNT; // we added positions
//        Gdx.gl20.glEnableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getTexCoordAttrib());
//        Gdx.gl20.glVertexAttribPointer(VoxelGameClient.getInstance().getGuiShader().getTexCoordAttrib(), TEXCOORD_FLOAT_COUNT, GL_FLOAT, false, VERTEX_SIZE_BYTES, byteOffset);
//
//        int indiciesUsed = numIndices;
//        numIndices = 0; // The draw is done, the queue is empty, we're starting fresh
//
//        Gdx.gl20.glDisableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getPositionAttrib());
//        Gdx.gl20.glDisableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getTexCoordAttrib());
//
//        Gdx.gl30.glBindVertexArray(0);
//        Gdx.gl20.glBindBuffer(GL_ARRAY_BUFFER, 0);
//
//        return new VboStaticRender(vbo, vao, textureID, textureManager, indiciesUsed);
//    }
//
//    public interface StaticRender extends Destroyable {
//        void render();
//
//        void destroy();
//    }
//
//    private static class VboStaticRender implements StaticRender {
//        private final int vboID, vaoID, textureID;
//        private final int numVertices;
//        private final TextureManager manager;
//
//        public VboStaticRender(int vboID, int vaoID, int textureID, TextureManager manager, int numVertices) {
//            this.vboID = vboID;
//            this.vaoID = vaoID;
//            this.textureID = textureID;
//            this.manager = manager;
//            this.numVertices = numVertices;
//        }
//
//        @Override
//        public void render() {
//            manager.bindTexture(textureID);
//
//            Gdx.gl30.glBindVertexArray(vaoID);
//            Gdx.gl.glEnableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getPositionAttrib());
//            Gdx.gl.glEnableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getTexCoordAttrib());
//
//            Gdx.gl30.glDrawArrays(GL_TRIANGLES, 0, numVertices);
//
//            Gdx.gl.glDisableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getPositionAttrib());
//            Gdx.gl.glDisableVertexAttribArray(VoxelGameClient.getInstance().getGuiShader().getTexCoordAttrib());
//
//            Gdx.gl30.glBindVertexArray(0);
//            Gdx.gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
//        }
//
//        @Override
//        public void destroy() {
//            IntBuffer toDestroy = BufferUtils.newIntBuffer(1);
//            toDestroy.put(vboID);
//            toDestroy.flip();
//            Gdx.gl.glDeleteBuffers(1, toDestroy);
//            toDestroy = BufferUtils.newIntBuffer(1);
//            toDestroy.put(vaoID);
//            toDestroy.flip();
//            Gdx.gl30.glDeleteVertexArrays(1, toDestroy);
//        }
//    }
//
//}
