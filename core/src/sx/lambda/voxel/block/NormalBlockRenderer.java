package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import groovy.transform.CompileStatic;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.render.NotInitializedException;

@CompileStatic
public class NormalBlockRenderer implements IBlockRenderer {

    protected static final float TEXTURE_PERCENTAGE = 0.03125f;

    private static Texture blockMap;

    protected final float u, v;
    protected final int blockID;

    private static boolean initialized;

    final int BLOCKS_PER_WIDTH = 1024/32;

    public NormalBlockRenderer(int blockID) {
        this.blockID = blockID;
        u = ((blockID%BLOCKS_PER_WIDTH)*TEXTURE_PERCENTAGE);
        v = ((blockID/BLOCKS_PER_WIDTH)*TEXTURE_PERCENTAGE);
    }

    @Override
    public void render2d(SpriteBatch batcher, int x, int y, int width) {
        if(!initialized) {
            initialize();
        }
        float u2 = u+TEXTURE_PERCENTAGE-.001f;
        float v2 = v+TEXTURE_PERCENTAGE-.001f;
        batcher.draw(getBlockMap(), x, y, x+width, y+width, u, v, u2, v2);
    }

    @Override
    public void renderNorth(int x1, int y1, int x2, int y2, int z, float lightLevel, MeshBuilder builder) {
        // POSITIVE Z
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(blockID / 100.0f, blockID / 100.0f, blockID / 100.0f, blockID / 100.0f);
        builder.rect(x1, y1, z,
                x2, y1, z,
                x2, y2, z,
                x1, y2, z,
                0, 0, 1);
    }

    @Override
    public void renderSouth(int x1, int y1, int x2, int y2, int z, float lightLevel, MeshBuilder builder) {
        // NEGATIVE Z
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(blockID / 100.0f, blockID / 100.0f, blockID / 100.0f, blockID / 100.0f);
        builder.rect(x1, y2, z,
                x2, y2, z,
                x2, y1, z,
                x1, y1, z,
                0, 0, -1);
    }

    @Override
    public void renderWest(int z1, int y1, int z2, int y2, int x, float lightLevel, MeshBuilder builder) {
        // NEGATIVE X
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(blockID / 100.0f, blockID / 100.0f, blockID / 100.0f, blockID / 100.0f);
        builder.rect(x, y1, z2,
                x, y2, z2,
                x, y2, z1,
                x, y1, z1,
                -1, 0, 0);
    }

    @Override
    public void renderEast(int z1, int y1, int z2, int y2, int x, float lightLevel, MeshBuilder builder) {
        // POSITIVE X
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(blockID / 100.0f, blockID / 100.0f, blockID / 100.0f, blockID / 100.0f);
        builder.rect(x, y1, z1,
                x, y2, z1,
                x, y2, z2,
                x, y1, z2,
                1, 0, 0);
    }

    @Override
    public void renderTop(int x1, int z1, int x2, int z2, int y, float lightLevel, MeshBuilder builder) {
        // POSITIVE Y
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(blockID / 100.0f, blockID / 100.0f, blockID / 100.0f, blockID / 100.0f);
        builder.rect(x1, y, z2,
                x2, y, z2,
                x2, y, z1,
                x1, y, z1,
                0, 1, 0);
    }

    @Override
    public void renderBottom(int x1, int z1, int x2, int z2, int y, float lightLevel, MeshBuilder builder) {
        // NEGATIVE Y
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(blockID / 100.0f, blockID / 100.0f, blockID / 100.0f, blockID / 100.0f);
        builder.rect(x1, y, z1,
                x2, y, z1,
                x2, y, z2,
                x1, y, z2,
                0, -1, 0);
    }

    private static void initialize() {
        try {
            blockMap = VoxelGameClient.getInstance().getBlockTextureAtlas();
        } catch (NotInitializedException e) {
            System.err.println("Error getting block texture atlas!");
            e.printStackTrace();
        }
        initialized = true;
    }

    public static Texture getBlockMap() {
        if(blockMap == null) {
            initialize();
        }
        return blockMap;
    }
}
