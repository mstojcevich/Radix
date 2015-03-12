package sx.lambda.mstojcevich.voxel.client.render.meshing;

import java.nio.FloatBuffer;

/**
 * Results of a mesh
 */
public class MeshResult {

    private final FloatBuffer vertices;
    private final FloatBuffer colors;
    private final FloatBuffer normals;
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
    public MeshResult(FloatBuffer vertices, FloatBuffer colors, boolean alpha, FloatBuffer normals, FloatBuffer textureCoords) {
        this.vertices = vertices;
        this.colors = colors;
        this.normals = normals;
        this.textureCoords = textureCoords;
        this.alpha = alpha;
    }

    public FloatBuffer getVertices() {
        return this.vertices;
    }

    public FloatBuffer getColors() {
        return this.colors;
    }

    public FloatBuffer getTextureCoords() {
        return this.textureCoords;
    }

    public FloatBuffer getNormals() {
        return this.normals;
    }

    public int getColorCoordLength() {
        return this.alpha ? 4 : 3;
    }
}
