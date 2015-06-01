package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.world.biome.Biome;

/**
 * Renderer for blocks like grass with different top, sides, and bottom textures.
 * Also colors based on biome.
 *
 * Expects the block its rendering to have 3 textures.
 */
public class GrassRenderer extends NormalBlockRenderer {

    private static final int
            TOP_OFFSET = 0,
            SIDE_OFFSET = 1,
            BOTTOM_OFFSET = 2;

    @Override
    public void renderNorth(int atlasIndex, int x1, int y1, int x2, int y2, int z, float lightLevel, MeshBuilder builder) {
        super.renderNorth(atlasIndex + SIDE_OFFSET, x1, y1, x2, y2, z, lightLevel, builder);
    }

    @Override
    public void renderSouth(int atlasIndex, int x1, int y1, int x2, int y2, int z, float lightLevel, MeshBuilder builder) {
        super.renderSouth(atlasIndex + SIDE_OFFSET, x1, y1, x2, y2, z, lightLevel, builder);
    }

    @Override
    public void renderEast(int atlasIndex, int z1, int y1, int z2, int y2, int x, float lightLevel, MeshBuilder builder) {
        super.renderEast(atlasIndex + SIDE_OFFSET, z1, y1, z2, y2, x, lightLevel, builder);
    }

    @Override
    public void renderWest(int atlasIndex, int z1, int y1, int z2, int y2, int x, float lightLevel, MeshBuilder builder) {
        super.renderWest(atlasIndex + SIDE_OFFSET, z1, y1, z2, y2, x, lightLevel, builder);
    }

    @Override
    public void renderTop(int atlasIndex, int x1, int z1, int x2, int z2, int y, float lightLevel, MeshBuilder builder) {
        Biome biome = VoxelGameClient.getInstance().getWorld().getChunkAtPosition(x1, z1).getBiome();
        int[] color = biome.getGrassColor(y - 1);
        float r = color[0]/255f;
        float g = color[1]/255f;
        float b = color[2]/255f;
        builder.setColor(r*lightLevel, g*lightLevel, b*lightLevel, 1);
        builder.setUVRange(atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f);
        builder.rect(x1, y, z2,
                x2, y, z2,
                x2, y, z1,
                x1, y, z1,
                0, 1, 0);
    }

    @Override
    public void renderBottom(int atlasIndex, int x1, int z1, int x2, int z2, int y, float lightLevel, MeshBuilder builder) {
        super.renderBottom(atlasIndex + BOTTOM_OFFSET, x1, z1, x2, z2, y, lightLevel, builder);
    }

}
