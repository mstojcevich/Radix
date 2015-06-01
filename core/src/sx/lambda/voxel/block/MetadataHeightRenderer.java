package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import sx.lambda.voxel.VoxelGameClient;

/**
 * Renderer for blocks with a metadata-based height
 */
public class MetadataHeightRenderer extends NormalBlockRenderer {

    private final int maxData;
    private final boolean invert;

    public MetadataHeightRenderer(int maxData, boolean invert) {
        this.maxData = maxData;
        this.invert = invert;
    }

    public MetadataHeightRenderer(int maxData) {
        this(maxData, false);
    }

    @Override
    public void renderNorth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, MeshBuilder builder) {
        short meta = VoxelGameClient.getInstance().getWorld().getChunkAtPosition((int)x1, (int)z).getMeta((int)x1, (int)y1, (int)z - 1);
        super.renderNorth(atlasIndex, x1, y1, x2, y1 + getHeight(meta), z, lightLevel, builder);
    }

    @Override
    public void renderSouth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, MeshBuilder builder) {
        short meta = VoxelGameClient.getInstance().getWorld().getChunkAtPosition((int)x1, (int)z).getMeta((int)x1, (int)y1, (int)z);
        super.renderSouth(atlasIndex, x1, y1, x2, y1 + getHeight(meta), z, lightLevel, builder);
    }

    @Override
    public void renderWest(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, MeshBuilder builder) {
        short meta = VoxelGameClient.getInstance().getWorld().getChunkAtPosition((int)x, (int)z1).getMeta((int)x, (int)y1, (int)z1);
        super.renderWest(atlasIndex, z1, y1, z2, y1 + getHeight(meta), x, lightLevel, builder);
    }

    @Override
    public void renderEast(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, MeshBuilder builder) {
        short meta = VoxelGameClient.getInstance().getWorld().getChunkAtPosition((int)x, (int)z1).getMeta((int)x - 1, (int)y1, (int)z1);
        super.renderEast(atlasIndex, z1, y1, z2, y1 + getHeight(meta), x, lightLevel, builder);
    }

    @Override
    public void renderTop(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, MeshBuilder builder) {
        short meta = VoxelGameClient.getInstance().getWorld().getChunkAtPosition((int) x1, (int) z1).getMeta((int)x1, (int) y - 1, (int)z1);
        super.renderTop(atlasIndex, x1, z1, x2, z2, y - (1 - getHeight(meta)), lightLevel, builder);
    }

    private float getHeight(short metadata) {
        float height = ((float) (metadata + 1) / (maxData + 1));
        if(invert && height < 1)
            height = 1-height;
        return height;
    }

}
