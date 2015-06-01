package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.world.biome.Biome;

/**
 * Renderer for biome-colored foliage (full-block, not x-shaped).
 * Usually used for leaves.
 */
public class ColoredFoliageRenderer extends NormalBlockRenderer {

    @Override
    public void renderNorth(int atlasIndex, int x1, int y1, int x2, int y2, int z, float lightLevel, MeshBuilder builder) {
        // POSITIVE Z
        Biome biome = VoxelGameClient.getInstance().getWorld().getChunkAtPosition(x1, z).getBiome();
        int[] color = biome.getFoliageColor(y1);
        float r = color[0]/255f;
        float g = color[1]/255f;
        float b = color[2]/255f;
        builder.setColor(r*lightLevel, g*lightLevel, b*lightLevel, 1);
        builder.setUVRange(atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f);
        builder.rect(x1, y1, z,
                x2, y1, z,
                x2, y2, z,
                x1, y2, z,
                0, 0, 1);
    }

    @Override
    public void renderSouth(int atlasIndex, int x1, int y1, int x2, int y2, int z, float lightLevel, MeshBuilder builder) {
        // NEGATIVE Z
        Biome biome = VoxelGameClient.getInstance().getWorld().getChunkAtPosition(x1, z).getBiome();
        int[] color = biome.getFoliageColor(y1);
        float r = color[0]/255f;
        float g = color[1]/255f;
        float b = color[2]/255f;
        builder.setColor(r*lightLevel, g*lightLevel, b*lightLevel, 1);
        builder.setUVRange(atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f);
        builder.rect(x1, y2, z,
                x2, y2, z,
                x2, y1, z,
                x1, y1, z,
                0, 0, -1);
    }

    @Override
    public void renderWest(int atlasIndex, int z1, int y1, int z2, int y2, int x, float lightLevel, MeshBuilder builder) {
        // NEGATIVE X
        Biome biome = VoxelGameClient.getInstance().getWorld().getChunkAtPosition(x, z1).getBiome();
        int[] color = biome.getFoliageColor(y1);
        float r = color[0]/255f;
        float g = color[1]/255f;
        float b = color[2]/255f;
        builder.setColor(r*lightLevel, g*lightLevel, b*lightLevel, 1);
        builder.setUVRange(atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f);
        builder.rect(x, y1, z2,
                x, y2, z2,
                x, y2, z1,
                x, y1, z1,
                -1, 0, 0);
    }

    @Override
    public void renderEast(int atlasIndex, int z1, int y1, int z2, int y2, int x, float lightLevel, MeshBuilder builder) {
        // POSITIVE X
        Biome biome = VoxelGameClient.getInstance().getWorld().getChunkAtPosition(x, z1).getBiome();
        int[] color = biome.getFoliageColor(y1);
        float r = color[0]/255f;
        float g = color[1]/255f;
        float b = color[2]/255f;
        builder.setColor(r*lightLevel, g*lightLevel, b*lightLevel, 1);
        builder.setUVRange(atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f);
        builder.rect(x, y1, z1,
                x, y2, z1,
                x, y2, z2,
                x, y1, z2,
                1, 0, 0);
    }

    @Override
    public void renderTop(int atlasIndex, int x1, int z1, int x2, int z2, int y, float lightLevel, MeshBuilder builder) {
        // POSITIVE Y
        Biome biome = VoxelGameClient.getInstance().getWorld().getChunkAtPosition(x1, z1).getBiome();
        int[] color = biome.getFoliageColor(y - 1);
        float r = color[0]/255f;
        float g = color[1]/255f;
        float b = color[2]/255f;
        builder.setColor(r*lightLevel, g*lightLevel, b*lightLevel, 1);        builder.setUVRange(atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f);
        builder.rect(x1, y, z2,
                x2, y, z2,
                x2, y, z1,
                x1, y, z1,
                0, 1, 0);
    }

    @Override
    public void renderBottom(int atlasIndex, int x1, int z1, int x2, int z2, int y, float lightLevel, MeshBuilder builder) {
        // NEGATIVE Y
        Biome biome = VoxelGameClient.getInstance().getWorld().getChunkAtPosition(x1, z1).getBiome();
        int[] color = biome.getFoliageColor(y);
        float r = color[0]/255f;
        float g = color[1]/255f;
        float b = color[2]/255f;
        builder.setColor(r*lightLevel, g*lightLevel, b*lightLevel, 1);
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f);
        builder.rect(x1, y, z1,
                x2, y, z1,
                x2, y, z2,
                x1, y, z2,
                0, -1, 0);
    }

}
