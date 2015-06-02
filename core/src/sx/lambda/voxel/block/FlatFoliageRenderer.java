package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;

/**
 * Renders foliage blocks, such as tall grass, as a "+" made out of their textures.
 */
public class FlatFoliageRenderer extends NormalBlockRenderer {

    @Override
    public void renderNorth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, MeshBuilder builder) {
        // POSITIVE Z
        builder.setColor(getColor((int) x1, (int) y1, (int) z, lightLevel));
        builder.setUVRange(atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f);
        builder.rect(x1, y1, z - 0.5f,
                x2, y1, z - 0.5f,
                x2, y2, z - 0.5f,
                x1, y2, z - 0.5f,
                0, 0, 1);
    }

    @Override
    public void renderSouth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, MeshBuilder builder) {
        // NEGATIVE Z
        builder.setColor(getColor((int) x1, (int) y1, (int) z, lightLevel));
        builder.setUVRange(atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f);
        builder.rect(x1, y2, z + 0.5f,
                x2, y2, z + 0.5f,
                x2, y1, z + 0.5f,
                x1, y1, z + 0.5f,
                0, 0, -1);
    }

    @Override
    public void renderWest(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, MeshBuilder builder) {
        // NEGATIVE X
        builder.setColor(getColor((int) x, (int) y1, (int) z1, lightLevel));
        builder.setUVRange(atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f);
        builder.rect(x + 0.5f, y1, z2,
                x + 0.5f, y2, z2,
                x + 0.5f, y2, z1,
                x + 0.5f, y1, z1,
                -1, 0, 0);
    }

    @Override
    public void renderEast(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, MeshBuilder builder) {
        // POSITIVE X
        builder.setColor(getColor((int) x, (int) y1, (int) z1, lightLevel));
        builder.setUVRange(atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f);
        builder.rect(x - 0.5f, y1, z1,
                x - 0.5f, y2, z1,
                x - 0.5f, y2, z2,
                x - 0.5f, y1, z2,
                1, 0, 0);
    }

    @Override
    public void renderTop(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, MeshBuilder builder) {}
    @Override
    public void renderBottom(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, MeshBuilder builder) {}

}
