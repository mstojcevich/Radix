package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;

public interface IBlockRenderer {

    void render2d(SpriteBatch batcher, int x, int y, int width);

    void renderNorth(int x1, int y1, int x2, int y2, int z, float lightLevel, MeshBuilder builder);

    void renderSouth(int x1, int y1, int x2, int y2, int z, float lightLevel, MeshBuilder builder);

    void renderWest(int z1, int y1, int z2, int y2, int x, float lightLevel, MeshBuilder builder);

    void renderEast(int z1, int y1, int z2, int y2, int x, float lightLevel, MeshBuilder builder);

    void renderTop(int x1, int z1, int x2, int z2, int y, float lightLevel, MeshBuilder builder);

    void renderBottom(int x1, int z1, int x2, int z2, int y, float lightLevel, MeshBuilder builder);

}
