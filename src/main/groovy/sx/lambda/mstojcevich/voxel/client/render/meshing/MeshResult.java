package sx.lambda.mstojcevich.voxel.client.render.meshing;

import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Results of a mesh
 */
public class MeshResult {

    private final ByteBuffer vertices;
    private final FloatBuffer colors;
    private final ByteBuffer normals;
    private final FloatBuffer textureCoords;

    private final boolean alpha;

    /**
     *
     * @param vertices Flipped FloatBuffer representing mesh vertices
     * @param colors Flipped FloatBuffer representing mesh vertex colors
     * @param alpha Whether the colors have an alpha channel
     * @param normals Flipped FloatBuffer representing mesh vertex normals
     * @param textureCoords Flipped FloatBuffer representing vertex texture coordinates
     */
    public MeshResult(ByteBuffer vertices, FloatBuffer colors, boolean alpha, ByteBuffer normals, FloatBuffer textureCoords) {
        this.vertices = vertices;
        this.colors = colors;
        this.normals = normals;
        this.textureCoords = textureCoords;
        this.alpha = alpha;
    }

    public ByteBuffer getVertices() {
        return this.vertices;
    }

    public FloatBuffer getColors() {
        return this.colors;
    }

    public FloatBuffer getTextureCoords() {
        return this.textureCoords;
    }

    public ByteBuffer getNormals() {
        return this.normals;
    }

    public int getColorCoordLength() {
        return this.alpha ? 4 : 3;
    }

    public void putInVBO(int vertexVbo, int colorVbo, int textureCoordVbo, int normalVbo) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, getVertices(), GL15.GL_STATIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, textureCoordVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, getTextureCoords(), GL15.GL_STATIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, getNormals(), GL15.GL_STATIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, getColors(), GL15.GL_STATIC_DRAW);
    }
}
