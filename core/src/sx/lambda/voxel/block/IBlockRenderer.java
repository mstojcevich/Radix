package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;

public interface IBlockRenderer {

    void render2d(SpriteBatch batcher, int atlasIndex, float x, float y, float width);

    void renderNorth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, MeshBuilder builder);

    void renderSouth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, MeshBuilder builder);

    void renderWest(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, MeshBuilder builder);

    void renderEast(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, MeshBuilder builder);

    void renderTop(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, MeshBuilder builder);

    void renderBottom(int atlasIndex, int x1, int z1, int x2, int z2, int y, float lightLevel, MeshBuilder builder);

}
