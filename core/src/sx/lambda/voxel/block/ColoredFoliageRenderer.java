package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.Color;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.world.biome.Biome;

/**
 * Renderer for biome-colored foliage (full-block, not x-shaped).
 * Usually used for leaves.
 */
public class ColoredFoliageRenderer extends NormalBlockRenderer {

    @Override
    protected Color getColor(int x, int y, int z) {
        Biome biome = VoxelGameClient.getInstance().getWorld().getChunk(x, z).getBiome();
        int[] color = biome.getFoliageColor(y);
        float r = color[0]/255f;
        float g = color[1]/255f;
        float b = color[2]/255f;
        return new Color(r, g, b, 1);
    }

}
