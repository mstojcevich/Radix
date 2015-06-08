package sx.lambda.voxel.world.chunk;

import org.spacehq.mc.protocol.data.game.NibbleArray3d;
import sx.lambda.voxel.api.RadixAPI;
import sx.lambda.voxel.block.Block;

/**
 * Block storage using flat multidimensional arrays
 */
public class FlatBlockStorage implements BlockStorage {

    private final int width, depth, height, size;
    private short[] blocks; // last nibble is metadata, everything up to that is block id
    private NibbleArray3d sunlight;
    private NibbleArray3d blocklight;

    public FlatBlockStorage(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.size = width*height*depth;

        blocks = new short[size];
        sunlight = new NibbleArray3d(size);
        blocklight = new NibbleArray3d(size);
    }

    /**
     * ONLY USE THIS CONSTRUCTOR WITH ALLOCATE SET TO FALSE IF YOU'RE GOING TO IMMEDIATELY FILL IN BLOCKS, SUNLIGHT, AND BLOCKLIGHT
     * @param allocate Whether to default the arrays. Set to false if you're going to manually set them immediately after construction.
     */
    public FlatBlockStorage(int width, int height, int depth, boolean allocate) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.size = width*height*depth;

        if(allocate) {
            blocks = new short[size];
            sunlight = new NibbleArray3d(size);
            blocklight = new NibbleArray3d(size);
        }
    }

    @Override
    public void setId(int x, int y, int z, int id) throws CoordinatesOutOfBoundsException {
        if(x < 0 || x >= width || z <  0 || z >= depth || y < 0 || y >= height)
            throw new CoordinatesOutOfBoundsException();

        int index = getIndex(x, y, z);
        blocks[index] = (short)(id << 4 | (blocks[index] & 0xF));
    }

    @Override
    public short getId(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        if(x < 0 || x >= width || z <  0 || z >= depth || y < 0 || y >= height)
            throw new CoordinatesOutOfBoundsException();

        return (short)(blocks[getIndex(x, y, z)] >> 4);
    }

    @Override
    public void setMeta(int x, int y, int z, int meta) throws CoordinatesOutOfBoundsException {
        if(x < 0 || x >= width || z <  0 || z >= depth || y < 0 || y >= height)
            throw new CoordinatesOutOfBoundsException();

        int index = getIndex(x, y, z);
        blocks[index] = (short)((blocks[index] >> 4 << 4) | meta);
    }

    @Override
    public short getMeta(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        if(x < 0 || x >= width || z <  0 || z >= depth || y < 0 || y >= height)
            throw new CoordinatesOutOfBoundsException();

        return (short)(blocks[getIndex(x, y, z)] & 0xF);
    }

    @Override
    public void setBlock(int x, int y, int z, Block block) throws CoordinatesOutOfBoundsException {
    }

    @Override
    public Block getBlock(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        return RadixAPI.instance.getBlockByID(getId(x, y, z));
    }

    @Override
    public void setSunlight(int x, int y, int z, int sunlight) throws CoordinatesOutOfBoundsException {
        if(x < 0 || x >= width || z <  0 || z >= depth || y < 0 || y >= height)
            throw new CoordinatesOutOfBoundsException();

        this.sunlight.set(x, y, z, sunlight);
    }

    @Override
    public byte getSunlight(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        if(x < 0 || x >= width || z <  0 || z >= depth || y < 0 || y >= height)
            throw new CoordinatesOutOfBoundsException();

        return (byte)this.sunlight.get(x, y, z);
    }

    @Override
    public void setBlocklight(int x, int y, int z, int blocklight) throws CoordinatesOutOfBoundsException {
        if(x < 0 || x >= width || z <  0 || z >= depth || y < 0 || y >= height)
            throw new CoordinatesOutOfBoundsException();

        this.blocklight.set(x, y, z, blocklight);
    }

    @Override
    public byte getBlocklight(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        if(x < 0 || x >= width || z <  0 || z >= depth || y < 0 || y >= height)
            throw new CoordinatesOutOfBoundsException();

        return (byte)this.blocklight.get(x, y, z);
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

    /**
     * Set underlying sunlight array
     */
    public void setSunlight(NibbleArray3d nibbleArray) {
        this.sunlight = nibbleArray;
    }

    /**
     * Set underlying blocklight array
     */
    public void setBlocklight(NibbleArray3d nibbleArray) {
        this.blocklight = nibbleArray;
    }

    /**
     * Set underlying block array.
     * Should probably only call this if you know what you're doing.
     * @param blocks Array of shorts with the last nibble as the metadata and everything before it as tbe block id
     */
    public void setBlocks(short[] blocks) {
        this.blocks = blocks;
    }

    private int getIndex(int x, int y, int z) throws CoordinatesOutOfBoundsException {
        if(x < 0 || x > width || y < 0 || y > height || z < 0 || z > depth)
            throw new CoordinatesOutOfBoundsException();

        return x + z*width + y*width*depth;
    }

}
