package sx.lambda.voxel.world.chunk;

import sx.lambda.voxel.block.Block;

/**
 * Dumb block storage
 */
public interface BlockStorage {

    void setId(int x, int y, int z, int id) throws CoordinatesOutOfBoundsException;
    short getId(int x, int y, int z) throws CoordinatesOutOfBoundsException;
    void setMeta(int x, int y, int z, int meta) throws CoordinatesOutOfBoundsException;
    short getMeta(int x, int y, int z) throws CoordinatesOutOfBoundsException;
    void setBlock(int x, int y, int z, Block block) throws CoordinatesOutOfBoundsException;
    Block getBlock(int x, int y,  int z) throws CoordinatesOutOfBoundsException;
    void setSunlight(int x, int y, int z, int sunlight) throws CoordinatesOutOfBoundsException;
    byte getSunlight(int x, int y, int z) throws CoordinatesOutOfBoundsException;
    void setBlocklight(int x, int y, int z, int blocklight) throws CoordinatesOutOfBoundsException;
    byte getBlocklight(int x, int y, int z) throws CoordinatesOutOfBoundsException;

    int getWidth();
    int getHeight();
    int getDepth();

    class CoordinatesOutOfBoundsException extends Exception {}

}
