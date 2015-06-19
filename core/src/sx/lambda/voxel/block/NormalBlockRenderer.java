package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.client.render.meshing.PerCornerLightData;
import sx.lambda.voxel.render.NotInitializedException;

/**
 * Render a standard block. Ex. stone, dirt.
 *
 * If you extend, make sure to implement getUniqueID() yourself!
 */
public class NormalBlockRenderer implements BlockRenderer {

    private static final float TEXTURE_PERCENTAGE = 32f / 2048f;
    private static final int BLOCKS_PER_WIDTH = 2048 / 32;

    private static Texture blockMap;

    private static boolean initialized;

    private VertexInfo c00 = new VertexInfo();
    private VertexInfo c01 = new VertexInfo();
    private VertexInfo c10 = new VertexInfo();
    private VertexInfo c11 = new VertexInfo();

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
    public void renderNorth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        // POSITIVE Z
        float uv = atlasIndex / 100.0f;
        builder.setUVRange(uv, uv, uv, uv);

        c00.setPos(x1, y1, z).setNor(0, 0, 1);
        c01.setPos(x1, y2, z).setNor(0, 0, 1);
        c10.setPos(x2, y1, z).setNor(0, 0, 1);
        c11.setPos(x2, y2, z).setNor(0, 0, 1);

        Color c = getColor((int) x1, (int) y1, (int) z - 1);
        if(pcld == null) {
            builder.setColor(c.r*lightLevel, c.g*lightLevel, c.b*lightLevel, c.a);
        } else {
            c00.setCol(c.r*pcld.l00, c.g*pcld.l00, c.b*pcld.l00, c.a);
            c01.setCol(c.r*pcld.l01, c.g*pcld.l01, c.b*pcld.l01, c.a);
            c10.setCol(c.r*pcld.l10, c.g*pcld.l10, c.b*pcld.l10, c.a);
            c11.setCol(c.r*pcld.l11, c.g*pcld.l11, c.b*pcld.l11, c.a);
        }

        builder.rect(c00, c10, c11, c01);
    }

    @Override
    public void renderSouth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        // NEGATIVE Z
        float uv = atlasIndex / 100.0f;
        builder.setUVRange(uv, uv, uv, uv);

        c00.setPos(x1, y1, z).setNor(0, 0, -1);
        c01.setPos(x1, y2, z).setNor(0, 0, -1);
        c10.setPos(x2, y1, z).setNor(0, 0, -1);
        c11.setPos(x2, y2, z).setNor(0, 0, -1);

        Color c = getColor((int) x1, (int) y1, (int) z);
        if(pcld == null) {
            builder.setColor(c.r*lightLevel, c.g*lightLevel, c.b*lightLevel, c.a);
        } else {
            c00.setCol(c.r*pcld.l00, c.g*pcld.l00, c.b*pcld.l00, c.a);
            c01.setCol(c.r*pcld.l01, c.g*pcld.l01, c.b*pcld.l01, c.a);
            c10.setCol(c.r*pcld.l10, c.g*pcld.l10, c.b*pcld.l10, c.a);
            c11.setCol(c.r*pcld.l11, c.g*pcld.l11, c.b*pcld.l11, c.a);
        }

        builder.rect(c01, c11, c10, c00);
    }

    @Override
    public void renderWest(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        // NEGATIVE X
        float uv = atlasIndex / 100.0f;
        builder.setUVRange(uv, uv, uv, uv);

        c00.setPos(x, y1, z1).setNor(-1, 0, 0);
        c01.setPos(x, y1, z2).setNor(-1, 0, 0);
        c10.setPos(x, y2, z1).setNor(-1, 0, 0);
        c11.setPos(x, y2, z2).setNor(-1, 0, 0);

        Color c = getColor((int) x, (int) y1, (int) z1);
        if(pcld == null) {
            builder.setColor(c.r*lightLevel, c.g*lightLevel, c.b*lightLevel, c.a);
        } else {
            c00.setCol(c.r*pcld.l00, c.g*pcld.l00, c.b*pcld.l00, c.a);
            c01.setCol(c.r*pcld.l01, c.g*pcld.l01, c.b*pcld.l01, c.a);
            c10.setCol(c.r*pcld.l10, c.g*pcld.l10, c.b*pcld.l10, c.a);
            c11.setCol(c.r*pcld.l11, c.g*pcld.l11, c.b*pcld.l11, c.a);
        }

        builder.rect(c01, c11, c10, c00);
    }

    @Override
    public void renderEast(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        // POSITIVE X
        float uv = atlasIndex / 100.0f;
        builder.setUVRange(uv, uv, uv, uv);

        c00.setPos(x, y1, z1).setNor(1, 0, 0);
        c01.setPos(x, y1, z2).setNor(1, 0, 0);
        c10.setPos(x, y2, z1).setNor(1, 0, 0);
        c11.setPos(x, y2, z2).setNor(1, 0, 0);

        Color c = getColor((int) x - 1, (int) y1, (int) z1);
        if(pcld == null) {
            builder.setColor(c.r*lightLevel, c.g*lightLevel, c.b*lightLevel, c.a);
        } else {
            c00.setCol(c.r*pcld.l00, c.g*pcld.l00, c.b*pcld.l00, c.a);
            c01.setCol(c.r*pcld.l01, c.g*pcld.l01, c.b*pcld.l01, c.a);
            c10.setCol(c.r*pcld.l10, c.g*pcld.l10, c.b*pcld.l10, c.a);
            c11.setCol(c.r*pcld.l11, c.g*pcld.l11, c.b*pcld.l11, c.a);
        }

        builder.rect(c00, c10, c11, c01);
    }

    @Override
    public void renderTop(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        // POSITIVE Y
        float uv = atlasIndex / 100.0f;
        builder.setUVRange(uv, uv, uv, uv);

        c00.setPos(x1, y, z1).setNor(0, 1, 0);
        c01.setPos(x1, y, z2).setNor(0, 1, 0);
        c10.setPos(x2, y, z1).setNor(0, 1, 0);
        c11.setPos(x2, y, z2).setNor(0, 1, 0);

        Color c = getColor((int) x1, (int) y - 1, (int) z1);
        if(pcld == null) {
            builder.setColor(c.r*lightLevel, c.g*lightLevel, c.b*lightLevel, c.a);
        } else {
            c00.setCol(c.r*pcld.l00, c.g*pcld.l00, c.b*pcld.l00, c.a);
            c01.setCol(c.r*pcld.l01, c.g*pcld.l01, c.b*pcld.l01, c.a);
            c10.setCol(c.r*pcld.l10, c.g*pcld.l10, c.b*pcld.l10, c.a);
            c11.setCol(c.r*pcld.l11, c.g*pcld.l11, c.b*pcld.l11, c.a);
        }

        builder.rect(c01, c11, c10, c00);
    }

    @Override
    public void renderBottom(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        // NEGATIVE Y
        float uv = atlasIndex / 100.0f;
        builder.setUVRange(uv, uv, uv, uv);

        c00.setPos(x1, y, z1).setNor(0, -1, 0);
        c01.setPos(x1, y, z2).setNor(0, -1, 0);
        c10.setPos(x2, y, z1).setNor(0, -1, 0);
        c11.setPos(x2, y, z2).setNor(0, -1, 0);

        Color c = getColor((int) x1, (int) y, (int) z1);
        if(pcld == null) {
            builder.setColor(c.r*lightLevel, c.g*lightLevel, c.b*lightLevel, c.a);
        } else {
            c00.setCol(c.r*pcld.l00, c.g*pcld.l00, c.b*pcld.l00, c.a);
            c01.setCol(c.r*pcld.l01, c.g*pcld.l01, c.b*pcld.l01, c.a);
            c10.setCol(c.r*pcld.l10, c.g*pcld.l10, c.b*pcld.l10, c.a);
            c11.setCol(c.r*pcld.l11, c.g*pcld.l11, c.b*pcld.l11, c.a);
        }

        builder.rect(c00, c10, c11, c01);
    }

    protected Color getColor(int x, int y, int z) {
        return Color.WHITE;
    }

    private static void initialize() {
        try {
            blockMap = RadixClient.getInstance().getBlockTextureAtlas();
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

    @Override
    public String getUniqueID() {
        return "Builtin.NORMAL";
    }

}
