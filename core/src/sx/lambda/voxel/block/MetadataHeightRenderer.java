package sx.lambda.voxel.block;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.MathUtils;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.client.render.meshing.PerCornerLightData;
import sx.lambda.voxel.world.IWorld;
import sx.lambda.voxel.world.chunk.BlockStorage.CoordinatesOutOfBoundsException;
import sx.lambda.voxel.world.chunk.IChunk;

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
    public void renderNorth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        int zi = MathUtils.floor(z - 1);
        int xi = MathUtils.floor(x1);
        int yi = MathUtils.floor(y1);
        IWorld world = RadixClient.getInstance().getWorld();
        IChunk chunk = world.getChunk(xi, zi);
        short meta = 0;
        try {
            meta = chunk.getMeta(xi & (world.getChunkSize() - 1), yi, zi & (world.getChunkSize() - 1));
        } catch (CoordinatesOutOfBoundsException e) {
            e.printStackTrace();
        }
        super.renderNorth(atlasIndex, x1, y1, x2, y1 + getHeight(meta), z, lightLevel, pcld, builder);
    }

    @Override
    public void renderSouth(int atlasIndex, float x1, float y1, float x2, float y2, float z, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        int zi = MathUtils.floor(z);
        int xi = MathUtils.floor(x1);
        int yi = MathUtils.floor(y1);
        IWorld world = RadixClient.getInstance().getWorld();
        IChunk chunk = world.getChunk(xi, zi);
        short meta = 0;
        try {
            meta = chunk.getMeta(xi & (world.getChunkSize() - 1), yi, zi & (world.getChunkSize() - 1));
        } catch (CoordinatesOutOfBoundsException e) {
            e.printStackTrace();
        }
        super.renderSouth(atlasIndex, x1, y1, x2, y1 + getHeight(meta), z, lightLevel, pcld, builder);
    }

    @Override
    public void renderWest(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        int zi = MathUtils.floor(z1);
        int xi = MathUtils.floor(x);
        int yi = MathUtils.floor(y1);
        IWorld world = RadixClient.getInstance().getWorld();
        IChunk chunk = world.getChunk(xi, zi);
        short meta = 0;
        try {
            meta = chunk.getMeta(xi & (world.getChunkSize() - 1), yi, zi & (world.getChunkSize() - 1));
        } catch (CoordinatesOutOfBoundsException e) {
            e.printStackTrace();
        }
        super.renderWest(atlasIndex, z1, y1, z2, y1 + getHeight(meta), x, lightLevel, pcld, builder);
    }

    @Override
    public void renderEast(int atlasIndex, float z1, float y1, float z2, float y2, float x, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        int zi = MathUtils.floor(z1);
        int xi = MathUtils.floor(x - 1);
        int yi = MathUtils.floor(y1);
        IWorld world = RadixClient.getInstance().getWorld();
        IChunk chunk = world.getChunk(xi, zi);
        short meta = 0;
        try {
            meta = chunk.getMeta(xi & (world.getChunkSize() - 1), yi, zi & (world.getChunkSize() - 1));
        } catch (CoordinatesOutOfBoundsException e) {
            e.printStackTrace();
        }
        super.renderEast(atlasIndex, z1, y1, z2, y1 + getHeight(meta), x, lightLevel, pcld, builder);
    }

    @Override
    public void renderTop(int atlasIndex, float x1, float z1, float x2, float z2, float y, float lightLevel, PerCornerLightData pcld, MeshBuilder builder) {
        int zi = MathUtils.floor(z1);
        int xi = MathUtils.floor(x1);
        int yi = MathUtils.floor(y - 1);
        IWorld world = RadixClient.getInstance().getWorld();
        IChunk chunk = world.getChunk(xi, zi);
        short meta = 0;
        try {
            meta = chunk.getMeta(xi & (world.getChunkSize()-1), yi, zi & (world.getChunkSize()-1));
        } catch (CoordinatesOutOfBoundsException e) {
            e.printStackTrace();
        }
        super.renderTop(atlasIndex, x1, z1, x2, z2, y - (1 - getHeight(meta)), lightLevel, pcld, builder);
    }

    private float getHeight(short metadata) {
        float height = ((float) (metadata + 1) / (maxData + 1));
        if(invert && height < 1)
            height = 1-height;
        return height;
    }

    @Override
    public String getUniqueID() {
        return String.format("Builtin.METADATA_HEIGHT MAX=%d REVERSED=%b", maxData, invert);
    }

}
