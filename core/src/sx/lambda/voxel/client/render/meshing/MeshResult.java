//package sx.lambda.voxel.client.render.meshing;
//
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.graphics.GL20;
//
//import java.nio.FloatBuffer;
//
///**
// * Results of a mesh
// */
//public class MeshResult {
//
//    private final FloatBuffer vertices;
//    private final FloatBuffer colors;
//    private final FloatBuffer normals;
//    private final FloatBuffer blockIds;
//
//    private final boolean alpha;
//
//    /**
//     *
//     * @param vertices Flipped FloatBuffer representing mesh vertices
//     * @param colors Flipped FloatBuffer representing mesh vertex colors
//     * @param alpha Whether the colors have an alpha channel
//     * @param normals Flipped FloatBuffer representing mesh vertex normals
//     * @param blockIds Flipped FloatBuffer representing vertex block ids
//     */
//    public MeshResult(FloatBuffer vertices, FloatBuffer colors, boolean alpha, FloatBuffer normals, FloatBuffer blockIds) {
//        this.vertices = vertices;
//        this.colors = colors;
//        this.normals = normals;
//        this.blockIds = blockIds;
//        this.alpha = alpha;
//    }
//
//    public FloatBuffer getVertices() {
//        return this.vertices;
//    }
//
//    public FloatBuffer getColors() {
//        return this.colors;
//    }
//
//    public FloatBuffer getNormals() {
//        return this.normals;
//    }
//
//    public FloatBuffer getBlockIds() {
//        return this.blockIds;
//    }
//
//    public int getColorCoordLength() {
//        return this.alpha ? 4 : 3;
//    }
//
//    public void putInVBO(int vertexVbo, int colorVbo, int normalVbo, int blockIdVbo) {
//        Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, vertexVbo);
//        Gdx.gl.glBufferData(GL20.GL_ARRAY_BUFFER, getVertices().remaining() << 2, getVertices(), GL20.GL_STATIC_DRAW);
//
//        Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, normalVbo);
//        Gdx.gl.glBufferData(GL20.GL_ARRAY_BUFFER, getNormals().remaining() << 2, getNormals(), GL20.GL_STATIC_DRAW);
//
//        Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, colorVbo);
//        Gdx.gl.glBufferData(GL20.GL_ARRAY_BUFFER, getColors().remaining() << 2, getColors(), GL20.GL_STATIC_DRAW);
//
//        Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, blockIdVbo);
//        Gdx.gl.glBufferData(GL20.GL_ARRAY_BUFFER, getBlockIds().remaining() << 2, getBlockIds(), GL20.GL_STATIC_DRAW);
//    }
//}
