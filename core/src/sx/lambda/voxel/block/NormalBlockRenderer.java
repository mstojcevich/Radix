package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
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
        float uv = atlasIndex / 100.0f;
        builder.setColor(getColor((int) x1, (int) y1, (int) z - 1, lightLevel));
        builder.setUVRange(uv, uv, uv, uv);

        VertexInfo c00 = new VertexInfo().setPos(x1, y1, z).setNor(0, 0, 1);
        VertexInfo c01 = new VertexInfo().setPos(x1, y2, z).setNor(0, 0, 1);
        VertexInfo c10 = new VertexInfo().setPos(x2, y1, z).setNor(0, 0, 1);
        VertexInfo c11 = new VertexInfo().setPos(x2, y2, z).setNor(0, 0, 1);

        builder.rect(c00, c10, c11, c01);
    }

    @Override
    public void renderSouth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, MeshBuilder builder) {
        // NEGATIVE Z
        float uv = atlasIndex / 100.0f;
        builder.setColor(getColor((int) x1, (int) y1, (int) z, lightLevel));
        builder.setUVRange(uv, uv, uv, uv);

        VertexInfo c00 = new VertexInfo().setPos(x1, y1, z).setNor(0, 0, -1);
        VertexInfo c01 = new VertexInfo().setPos(x1, y2, z).setNor(0, 0, -1);
        VertexInfo c10 = new VertexInfo().setPos(x2, y1, z).setNor(0, 0, -1);
        VertexInfo c11 = new VertexInfo().setPos(x2, y2, z).setNor(0, 0, -1);

        builder.rect(c01, c11, c10, c00);
    }

    @Override
    public void renderWest(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, MeshBuilder builder) {
        // NEGATIVE X
        float uv = atlasIndex / 100.0f;
        builder.setColor(getColor((int) x, (int) y1, (int) z1, lightLevel));
        builder.setUVRange(uv, uv, uv, uv);

        VertexInfo c00 = new VertexInfo().setPos(x, y1, z1).setNor(-1, 0, 0);
        VertexInfo c01 = new VertexInfo().setPos(x, y1, z2).setNor(-1, 0, 0);
        VertexInfo c10 = new VertexInfo().setPos(x, y2, z1).setNor(-1, 0, 0);
        VertexInfo c11 = new VertexInfo().setPos(x, y2, z2).setNor(-1, 0, 0);

        builder.rect(c01, c11, c10, c00);
    }

    @Override
    public void renderEast(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, MeshBuilder builder) {
        // POSITIVE X
        float uv = atlasIndex / 100.0f;
        builder.setColor(getColor((int) x - 1, (int) y1, (int) z1, lightLevel));
        builder.setUVRange(uv, uv, uv, uv);

        VertexInfo c00 = new VertexInfo().setPos(x, y1, z1).setNor(1, 0, 0);
        VertexInfo c01 = new VertexInfo().setPos(x, y1, z2).setNor(1, 0, 0);
        VertexInfo c10 = new VertexInfo().setPos(x, y2, z1).setNor(1, 0, 0);
        VertexInfo c11 = new VertexInfo().setPos(x, y2, z2).setNor(1, 0, 0);

        builder.rect(c00, c10, c11, c01);
    }

    @Override
    public void renderTop(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, MeshBuilder builder) {
        // POSITIVE Y
        float uv = atlasIndex / 100.0f;
        builder.setColor(getColor((int) x1, (int) y - 1, (int) z1, lightLevel));
        builder.setUVRange(uv, uv, uv, uv);

        VertexInfo c00 = new VertexInfo().setPos(x1, y, z1).setNor(0, 1, 0);
        VertexInfo c01 = new VertexInfo().setPos(x1, y, z2).setNor(0, 1, 0);
        VertexInfo c10 = new VertexInfo().setPos(x2, y, z1).setNor(0, 1, 0);
        VertexInfo c11 = new VertexInfo().setPos(x2, y, z2).setNor(0, 1, 0);

        builder.rect(c01, c11, c10, c00);
    }

    @Override
    public void renderBottom(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, MeshBuilder builder) {
        // NEGATIVE Y
        float uv = atlasIndex / 100.0f;
        builder.setColor(getColor((int) x1, (int) y, (int) z1, lightLevel));
        builder.setUVRange(uv, uv, uv, uv);

        VertexInfo c00 = new VertexInfo().setPos(x1, y, z1).setNor(0, -1, 0);
        VertexInfo c01 = new VertexInfo().setPos(x1, y, z2).setNor(0, -1, 0);
        VertexInfo c10 = new VertexInfo().setPos(x2, y, z1).setNor(0, -1, 0);
        VertexInfo c11 = new VertexInfo().setPos(x2, y, z2).setNor(0, -1, 0);

        builder.rect(c00, c10, c11, c01);
    }

    protected Color getColor(int x, int y, int z, float lightLevel) {
        return new Color(lightLevel, lightLevel, lightLevel, 1);
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
