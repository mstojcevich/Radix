package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;

/**
 * Renders foliage blocks, such as tall grass, as a "+" made out of their textures.
 */
public class FoliageBlockRenderer extends NormalBlockRenderer {

    public FoliageBlockRenderer(int blockID) {
        super(blockID);
    }

    @Override
    public void renderNorth(int x1, int y1, int x2, int y2, int z, float lightLevel, MeshBuilder builder) {
        // POSITIVE Z
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(blockID / 100.0f, blockID / 100.0f, blockID / 100.0f, blockID / 100.0f);
        builder.rect(x1, y1, z - 0.5f,
                x2, y1, z - 0.5f,
                x2, y2, z - 0.5f,
                x1, y2, z - 0.5f,
                0, 0, 1);
    }

    @Override
    public void renderSouth(int x1, int y1, int x2, int y2, int z, float lightLevel, MeshBuilder builder) {
        // NEGATIVE Z
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(blockID / 100.0f, blockID / 100.0f, blockID / 100.0f, blockID / 100.0f);
        builder.rect(x1, y2, z + 0.5f,
                x2, y2, z + 0.5f,
                x2, y1, z + 0.5f,
                x1, y1, z + 0.5f,
                0, 0, -1);
    }

    @Override
    public void renderWest(int z1, int y1, int z2, int y2, int x, float lightLevel, MeshBuilder builder) {
        // NEGATIVE X
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(blockID / 100.0f, blockID / 100.0f, blockID / 100.0f, blockID / 100.0f);
        builder.rect(x + 0.5f, y1, z2,
                x + 0.5f, y2, z2,
                x + 0.5f, y2, z1,
                x + 0.5f, y1, z1,
                -1, 0, 0);
    }

    @Override
    public void renderEast(int z1, int y1, int z2, int y2, int x, float lightLevel, MeshBuilder builder) {
        // POSITIVE X
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(blockID / 100.0f, blockID / 100.0f, blockID / 100.0f, blockID / 100.0f);
        builder.rect(x - 0.5f, y1, z1,
                x - 0.5f, y2, z1,
                x - 0.5f, y2, z2,
                x - 0.5f, y1, z2,
                1, 0, 0);
    }

    @Override
    public void renderTop(int x1, int z1, int x2, int z2, int y, float lightLevel, MeshBuilder builder) {}
    @Override
    public void renderBottom(int x1, int z1, int x2, int z2, int y, float lightLevel, MeshBuilder builder) {}

}
