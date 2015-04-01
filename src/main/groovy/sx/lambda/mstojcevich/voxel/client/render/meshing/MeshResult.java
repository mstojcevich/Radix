package sx.lambda.mstojcevich.voxel.client.render.meshing;

import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;

/**
 * Results of a mesh
 */
public class MeshResult {

    private final FloatBuffer vertices;
    private final FloatBuffer colors;
    private final FloatBuffer normals;
    private final FloatBuffer blockIds;

    private final boolean alpha;

    /**
     *
     * @param vertices Flipped FloatBuffer representing mesh vertices
     * @param colors Flipped FloatBuffer representing mesh vertex colors
     * @param alpha Whether the colors have an alpha channel
     * @param normals Flipped FloatBuffer representing mesh vertex normals
     * @param blockIds Flipped FloatBuffer representing vertex block ids
     */
    public MeshResult(FloatBuffer vertices, FloatBuffer colors, boolean alpha, FloatBuffer normals, FloatBuffer blockIds) {
        this.vertices = vertices;
        this.colors = colors;
        this.normals = normals;
        this.blockIds = blockIds;
        this.alpha = alpha;
    }

    public FloatBuffer getVertices() {
        return this.vertices;
    }

    public FloatBuffer getColors() {
        return this.colors;
    }

    public FloatBuffer getNormals() {
        return this.normals;
    }

    public FloatBuffer getBlockIds() {
        return this.blockIds;
    }

    public int getColorCoordLength() {
        return this.alpha ? 4 : 3;
    }

    public void putInVBO(int vertexVbo, int colorVbo, int normalVbo, int blockIdVbo) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, getVertices(), GL15.GL_STATIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, getNormals(), GL15.GL_STATIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, getColors(), GL15.GL_STATIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, blockIdVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, getBlockIds(), GL15.GL_STATIC_DRAW);
    }
}
