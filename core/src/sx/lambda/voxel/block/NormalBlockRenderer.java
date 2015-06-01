package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import groovy.transform.CompileStatic;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.render.NotInitializedException;

@CompileStatic
public class NormalBlockRenderer implements IBlockRenderer {

    private static final float TEXTURE_PERCENTAGE = 0.03125f;
    private static final int BLOCKS_PER_WIDTH = 1024 / 32;

    private static Texture blockMap;

    private static boolean initialized;

    public NormalBlockRenderer() {}

    @Override
    public void render2d(SpriteBatch batcher, int atlasIndex, float x, float y, float width) {
        if (!initialized) {
            initialize();
        }
        float u = getU(atlasIndex);
        float v = getV(atlasIndex);
        float u2 = u + TEXTURE_PERCENTAGE - .001f;
        float v2 = v + TEXTURE_PERCENTAGE - .001f;
        batcher.draw(getBlockMap(), x, y, width, width, u, v, u2, v2);
    }

    @Override
    public void renderNorth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, MeshBuilder builder) {
        // POSITIVE Z
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f);
        builder.rect(x1, y1, z,
                x2, y1, z,
                x2, y2, z,
                x1, y2, z,
                0, 0, 1);
    }

    @Override
    public void renderSouth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, MeshBuilder builder) {
        // NEGATIVE Z
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f);
        builder.rect(x1, y2, z,
                x2, y2, z,
                x2, y1, z,
                x1, y1, z,
                0, 0, -1);
    }

    @Override
    public void renderWest(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, MeshBuilder builder) {
        // NEGATIVE X
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f);
        builder.rect(x, y1, z2,
                x, y2, z2,
                x, y2, z1,
                x, y1, z1,
                -1, 0, 0);
    }

    @Override
    public void renderEast(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, MeshBuilder builder) {
        // POSITIVE X
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f);
        builder.rect(x, y1, z1,
                x, y2, z1,
                x, y2, z2,
                x, y1, z2,
                1, 0, 0);
    }

    @Override
    public void renderTop(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, MeshBuilder builder) {
        // POSITIVE Y
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f);
        builder.rect(x1, y, z2,
                x2, y, z2,
                x2, y, z1,
                x1, y, z1,
                0, 1, 0);
    }

    @Override
    public void renderBottom(int atlasIndex, int x1, int z1, int x2, int z2, int y, float lightLevel, MeshBuilder builder) {
        // NEGATIVE Y
        builder.setColor(lightLevel, lightLevel, lightLevel, 1);
        builder.setUVRange(atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f, atlasIndex / 100.0f);
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
        if (blockMap == null) {
            initialize();
        }
        return blockMap;
    }

    public static float getU(int atlasIndex) {
        return ((atlasIndex % BLOCKS_PER_WIDTH) * TEXTURE_PERCENTAGE);
    }

    public static float getV(int atlasIndex) {
        return ((atlasIndex / BLOCKS_PER_WIDTH) * TEXTURE_PERCENTAGE);
    }
}
