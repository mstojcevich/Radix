package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.api.RadixAPI;
import sx.lambda.voxel.client.render.meshing.PerCornerLightData;
import sx.lambda.voxel.world.biome.Biome;
import sx.lambda.voxel.world.chunk.IChunk;

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
    public void renderNorth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        super.renderNorth(atlasIndex + SIDE_OFFSET, x1, y1, x2, y2, z, lightLevel, pcld, builder);
    }

    @Override
    public void renderSouth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        super.renderSouth(atlasIndex + SIDE_OFFSET, x1, y1, x2, y2, z, lightLevel, pcld, builder);
    }

    @Override
    public void renderEast(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        super.renderEast(atlasIndex + SIDE_OFFSET, z1, y1, z2, y2, x, lightLevel, pcld, builder);
    }

    @Override
    public void renderWest(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        super.renderWest(atlasIndex + SIDE_OFFSET, z1, y1, z2, y2, x, lightLevel, pcld, builder);
    }

    @Override
    public void renderTop(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        float u = getU(atlasIndex);
        float v = getV(atlasIndex);
        builder.setUVRange(u, v, u, v);

        MeshPartBuilder.VertexInfo c00 = new MeshPartBuilder.VertexInfo().setPos(x1, y, z1).setNor(0, 1, 0);
        MeshPartBuilder.VertexInfo c01 = new MeshPartBuilder.VertexInfo().setPos(x1, y, z2).setNor(0, 1, 0);
        MeshPartBuilder.VertexInfo c10 = new MeshPartBuilder.VertexInfo().setPos(x2, y, z1).setNor(0, 1, 0);
        MeshPartBuilder.VertexInfo c11 = new MeshPartBuilder.VertexInfo().setPos(x2, y, z2).setNor(0, 1, 0);

        IChunk chunk = RadixClient.getInstance().getWorld().getChunk((int) x1, (int) z1);
        Biome biome;
        if(chunk != null) {
            biome = chunk.getBiome();
        } else {
            biome = RadixAPI.instance.getBiomeByID(0);
        }
        int[] color = biome.getGrassColor((int) y - 1);
        float r = color[0]/255f;
        float g = color[1]/255f;
        float b = color[2]/255f;
        if(pcld == null) {
            builder.setColor(r*lightLevel, g*lightLevel, b*lightLevel, 1);
        } else {
            c00.setCol(r*pcld.l00, g*pcld.l00, b*pcld.l00, 1);
            c01.setCol(r*pcld.l01, g*pcld.l01, b*pcld.l01, 1);
            c10.setCol(r*pcld.l10, g*pcld.l10, b*pcld.l10, 1);
            c11.setCol(r*pcld.l11, g*pcld.l11, b*pcld.l11, 1);
        }

        builder.rect(c01, c11, c10, c00);
    }

    @Override
    public void renderBottom(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        super.renderBottom(atlasIndex + BOTTOM_OFFSET, x1, z1, x2, z2, y, lightLevel, pcld, builder);
    }

    @Override
    public String getUniqueID() {
        return "Builtin.GRASS";
    }

}
