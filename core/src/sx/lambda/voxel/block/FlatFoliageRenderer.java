package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import sx.lambda.voxel.client.render.meshing.PerCornerLightData;

/**
 * Renders foliage blocks, such as tall grass, as a "+" made out of their textures.
 */
public class FlatFoliageRenderer extends NormalBlockRenderer {

    @Override
    public void renderNorth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        super.renderNorth(atlasIndex, x1, y1, x2, y2, z - 0.5f, lightLevel, pcld, builder);
    }

    @Override
    public void renderSouth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        super.renderSouth(atlasIndex, x1, y1, x2, y2, z + 0.5f, lightLevel, pcld, builder);
    }

    @Override
    public void renderWest(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        super.renderWest(atlasIndex, z1, y1, z2, y2, x + 0.5f, lightLevel, pcld, builder);
    }

    @Override
    public void renderEast(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        super.renderEast(atlasIndex, z1, y1, z2, y2, x - 0.5f, lightLevel, pcld, builder);
    }

    @Override
    public void renderTop(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {}
    @Override
    public void renderBottom(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {}

}
