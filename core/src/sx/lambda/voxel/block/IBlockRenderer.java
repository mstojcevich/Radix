package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import sx.lambda.voxel.world.chunk.IChunk;

import java.nio.FloatBuffer;

public interface IBlockRenderer {

    void render2d(SpriteBatch batcher, int x, int y, int width);

    Mesh renderNorth(int x1, int y1, int x2, int y2, int z, float lightLevel, MeshBuilder builder);
    Mesh renderSouth(int x1, int y1, int x2, int y2, int z, float lightLevel, MeshBuilder builder);
    Mesh renderWest(int z1, int y1, int z2, int y2, int x, float lightLevel, MeshBuilder builder);
    Mesh renderEast(int z1, int y1, int z2, int y2, int x, float lightLevel, MeshBuilder builder);
    Mesh renderTop(int x1, int z1, int x2, int z2, int y, float lightLevel, MeshBuilder builder);
    Mesh renderBottom(int x1, int z1, int x2, int z2, int y, float lightLevel, MeshBuilder builder);

}
