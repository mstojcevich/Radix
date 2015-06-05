package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import sx.lambda.voxel.client.render.meshing.PerCornerLightData;

public class FenceRenderer extends NormalBlockRenderer {

    private final float WIDTH = 0.25f;
    private final float R = WIDTH/2f;

    @Override
    public void renderNorth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        // POSITIVE Z
        super.renderNorth(atlasIndex, x1 + 0.5f - R, y1, x2 - 0.5f + R, y2, z - 0.5f + R, lightLevel, pcld, builder);
    }

    @Override
    public void renderSouth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        // NEGATIVE Z
        super.renderSouth(atlasIndex, x1 + 0.5f - R, y1, x2 - 0.5f + R, y2, z + 0.5f - R, lightLevel, pcld, builder);
    }

    @Override
    public void renderWest(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        // NEGATIVE X
        super.renderWest(atlasIndex, z1 + 0.5f - R, y1, z2 - 0.5f + R, y2, x + 0.5f - R, lightLevel, pcld, builder);
    }

    @Override
    public void renderEast(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        // POSITIVE X
        super.renderEast(atlasIndex, z1 + 0.5f - R, y1, z2 - 0.5f + R, y2, x - 0.5f + R, lightLevel, pcld, builder);
    }

    @Override
    public void renderTop(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        // POSITIVE Y
        super.renderTop(atlasIndex, x1 + 0.5f - R, z1 + 0.5f - R, x2 - 0.5f + R, z2 - 0.5f + R, y, lightLevel, pcld, builder);
    }

    @Override
    public void renderBottom(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        // NEGATIVE Y
        super.renderBottom(atlasIndex, x1 + 0.5f - R, z1 + 0.5f - R, x2 - 0.5f + R, z2 - 0.5f + R, y, lightLevel, pcld, builder);
    }

}
