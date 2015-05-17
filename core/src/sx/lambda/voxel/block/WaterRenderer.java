package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;

import java.nio.FloatBuffer;

public class WaterRenderer extends NormalBlockRenderer {

    public WaterRenderer(int blockID) {
        super(blockID);
    }

    @Override
    public Mesh renderNorth(int x1, int y1, int x2, int y2, int z, float lightLevel, MeshBuilder builder) {
        // POSITIVE Z
        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal, GL20.GL_TRIANGLES);
        builder.setColor(lightLevel, lightLevel, lightLevel, 0.6f);
        builder.setUVRange(blockID / 100.0f, blockID / 100.0f, blockID / 100.0f, blockID / 100.0f);
        builder.rect(x1, y1, z,
                x2, y1, z,
                x2, y2, z,
                x1, y2, z,
                0, 0, 1);
        return builder.end();
    }

    @Override
    public Mesh renderSouth(int x1, int y1, int x2, int y2, int z, float lightLevel, MeshBuilder builder) {
        // NEGATIVE Z
        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal, GL20.GL_TRIANGLES);
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(blockID / 100.0f, blockID / 100.0f, blockID / 100.0f, blockID / 100.0f);
        builder.rect(x1, y2, z,
                x2, y2, z,
                x2, y1, z,
                x1, y1, z,
                0, 0, -1);
        return builder.end();
    }

    @Override
    public Mesh renderWest(int z1, int y1, int z2, int y2, int x, float lightLevel, MeshBuilder builder) {
        // NEGATIVE X
        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal, GL20.GL_TRIANGLES);
        builder.setColor(lightLevel, lightLevel, lightLevel, 0.6f);
        builder.setUVRange(blockID / 100.0f, blockID / 100.0f, blockID / 100.0f, blockID / 100.0f);
        builder.rect(x, y1, z2,
                x, y2, z2,
                x, y2, z1,
                x, y1, z1,
                -1, 0, 0);
        return builder.end();
    }

    @Override
    public Mesh renderEast(int z1, int y1, int z2, int y2, int x, float lightLevel, MeshBuilder builder) {
        // POSITIVE X
        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal, GL20.GL_TRIANGLES);
        builder.setColor(lightLevel, lightLevel, lightLevel, 0.6f);
        builder.setUVRange(blockID / 100.0f, blockID / 100.0f, blockID / 100.0f, blockID / 100.0f);
        builder.rect(x, y1, z1,
                x, y2, z1,
                x, y2, z2,
                x, y1, z2,
                1, 0, 0);
        return builder.end();
    }

    @Override
    public Mesh renderTop(int x1, int z1, int x2, int z2, int y, float lightLevel, MeshBuilder builder) {
        // POSITIVE Y
        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal, GL20.GL_TRIANGLES);
        builder.setColor(lightLevel, lightLevel, lightLevel, 0.6f);
        builder.setUVRange(blockID / 100.0f, blockID / 100.0f, blockID / 100.0f, blockID / 100.0f);
        builder.rect(x1, y, z2,
                x2, y, z2,
                x2, y, z1,
                x1, y, z1,
                0, 1, 0);
        return builder.end();
    }

    @Override
    public Mesh renderBottom(int x1, int z1, int x2, int z2, int y, float lightLevel, MeshBuilder builder) {
        // NEGATIVE Y
        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal, GL20.GL_TRIANGLES);
        builder.setColor(lightLevel, lightLevel, lightLevel, 0.6f);
        builder.setUVRange(blockID / 100.0f, blockID / 100.0f, blockID / 100.0f, blockID / 100.0f);
        builder.rect(x1, y, z1,
                x2, y, z1,
                x2, y, z2,
                x1, y, z2,
                0, -1, 0);
        return builder.end();
    }

}
