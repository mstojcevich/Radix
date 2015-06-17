package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.Color;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.api.RadixAPI;
import sx.lambda.voxel.world.biome.Biome;
import sx.lambda.voxel.world.chunk.IChunk;

/**
 * Renderer for tall grass
 */
public class TallGrassRenderer extends FlatFoliageRenderer {

    @Override
    protected Color getColor(int x, int y, int z) {
        IChunk chunk = RadixClient.getInstance().getWorld().getChunk(x, z);
        Biome biome;
        if(chunk != null) {
            biome = chunk.getBiome();
        } else {
            biome = RadixAPI.instance.getBiomeByID(0);
        }
        int[] color = biome.getGrassColor(y);
        float r = color[0]/255f;
        float g = color[1]/255f;
        float b = color[2]/255f;
        return new Color(r, g, b, 1);
    }

    @Override
    public String getUniqueID() {
        return "BUILTIN.TALL_GRASS";
    }

}
