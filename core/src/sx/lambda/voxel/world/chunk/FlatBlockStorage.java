package sx.lambda.voxel.world.chunk;

import sx.lambda.voxel.block.Block;

/**
 * Block storage using flat multidimensional arrays
 */
public class FlatBlockStorage implements BlockStorage {

    private final int width, depth, height, size;
    private final Block[] types;
    private final short[] ids;
    private final short[] metadata;
    private final byte[] sunlight;
    private final byte[] blocklight;

    public FlatBlockStorage(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.size = width*height*depth;

        types = new Block[size];
        ids = new short[size];
        metadata = new short[size];
        sunlight = new byte[size];
        blocklight = new byte[size];
    }

    @Override
    public void setId(int x, int y, int z, int id) throws CoordinatesOutOfBoundsException {
        ids[getIndex(x, y, z)] = (short)id;
    }

    @Override
    public short getId(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        return ids[getIndex(x, y, z)];
    }

    @Override
    public void setMeta(int x, int y, int z, int meta) throws CoordinatesOutOfBoundsException {
        metadata[getIndex(x, y, z)] = (short)meta;
    }

    @Override
    public short getMeta(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        return metadata[getIndex(x, y, z)];
    }

    @Override
    public void setBlock(int x, int y, int z, Block block) throws CoordinatesOutOfBoundsException {
        types[getIndex(x, y, z)] = block;
    }

    @Override
    public Block getBlock(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        return types[getIndex(x, y, z)];
    }

    @Override
    public void setSunlight(int x, int y, int z, int sunlight) throws CoordinatesOutOfBoundsException {
        this.sunlight[getIndex(x, y, z)] = (byte)sunlight;
    }

    @Override
    public byte getSunlight(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        return sunlight[getIndex(x, y, z)];
    }

    @Override
    public void setBlocklight(int x, int y, int z, int blocklight) throws CoordinatesOutOfBoundsException {
        this.blocklight[getIndex(x, y, z)] = (byte)blocklight;
    }

    @Override
    public byte getBlocklight(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        return blocklight[getIndex(x, y, z)];
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    private int getIndex(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        if(x < 0 || x > width || y < 0 || y > height || z < 0 || z > depth)
            throw new CoordinatesOutOfBoundsException();
        return x + y*width + z*width*height;
    }

}
