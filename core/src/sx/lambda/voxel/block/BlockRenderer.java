package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import sx.lambda.voxel.client.render.meshing.PerCornerLightData;

/**
 *
 */
public interface BlockRenderer {

    void render2d(SpriteBatch batcher, int atlasIndex, float x, float y, float width);

    void renderNorth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, PerCornerLightData pcld, MeshBuilder builder);

    void renderSouth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, PerCornerLightData pcld, MeshBuilder builder);

    void renderWest(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, PerCornerLightData pcld, MeshBuilder builder);

    void renderEast(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, PerCornerLightData pcld, MeshBuilder builder);

    void renderTop(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, PerCornerLightData pcld, MeshBuilder builder);

    void renderBottom(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, PerCornerLightData pcld, MeshBuilder builder);

    /**
     * Get the unique ID of the block renderer.
     * Used in block json file loading.
     *
     * Conditions:
     * Any renderer instance with the same unique ID should render exactly the same way as another.
     * Should be prepended with something unique that won't be taken by another mod. Usually a package name or mod name.
     * Prefix "Builtin." is reserved and should not be used by anything not built into the base client.
     *
     * @return Unique ID of the renderer instance. Ex. "Builtin.METADATA_HEIGHT MAX=7 REVERSED=true"
     */
    String getUniqueID();

}
