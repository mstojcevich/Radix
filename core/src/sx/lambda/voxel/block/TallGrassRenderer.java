package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.Color;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.world.biome.Biome;

/**
 * Renderer for tall grass
 */
public class TallGrassRenderer extends FlatFoliageRenderer {

    @Override
    protected Color getColor(int x, int y, int z, float lightLevel) {
        Biome biome = VoxelGameClient.getInstance().getWorld().getChunk(x, z).getBiome();
        int[] color = biome.getGrassColor(y);
        float r = color[0]/255f;
        float g = color[1]/255f;
        float b = color[2]/255f;
        return new Color(r*lightLevel, g*lightLevel, b*lightLevel, 1);
    }

}
