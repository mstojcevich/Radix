package sx.lambda.voxel.world.chunk;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Disposable;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.IWorld;
import sx.lambda.voxel.world.biome.Biome;
import sx.lambda.voxel.world.chunk.BlockStorage.CoordinatesOutOfBoundsException;

import java.io.Serializable;

/**
 * Broken-up piece of the world made up of blocks
 */
public interface IChunk extends Serializable, Disposable {

    /**
     * Redraws all of the blocks in the chunk
     */
    void rerender();

    /**
     * Renders the chunk
     */
    void render(ModelBatch batch);


    /**
     * Gets the starting position of the chunk
     */
    Vec3i getStartPosition();

    /**
     * @return The y-value of the highest block in the chunk
     */
    int getHighestPoint();

    int getBlockId(int x, int y, int z) throws CoordinatesOutOfBoundsException;

    /**
     * Get the block at the specified position
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     */
    Block getBlock(int x, int y, int z) throws CoordinatesOutOfBoundsException;

    /**
     * Remove the block at the specified position
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     */
    void removeBlock(int x, int y, int z) throws CoordinatesOutOfBoundsException;

    /**
     * Set the block at the specified position
     * @param block New block to set to
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     */
    void setBlock(int block, int x, int y, int z) throws CoordinatesOutOfBoundsException;

    /**
     * Set the block at the specified position
     * @param block New block to set to
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     * @param updateSunlight Whether to update sunlight values for the new block
     */
    void setBlock(int block, int x, int y, int z, boolean updateSunlight) throws CoordinatesOutOfBoundsException;

    /**
     * Set the metadata for the block at the specified position
     * @param meta Metadata value to set to
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     */
    void setMeta(short meta, int x, int y, int z) throws CoordinatesOutOfBoundsException;

    /**
     * Get the metadata value at the specified position
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     */
    short getMeta(int x, int y, int z) throws CoordinatesOutOfBoundsException;

    /**
     * Gets the light value at the specified location
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     */
    float getLightLevel(int x, int y, int z) throws CoordinatesOutOfBoundsException;

    /**
     * Get sunlight at the specified position
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     * @return Sunlight value, between 0 and 15
     */
    int getSunlight(int x, int y, int z) throws CoordinatesOutOfBoundsException;

    /**
     * Set the sunlight at the specified position
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     * @param level Value between 0 and 15 for sunlight
     */
    void setSunlight(int x, int y, int z, int level) throws CoordinatesOutOfBoundsException;

    /**
     * Get the blocklight at the specified position
     * @param x
     * @param y
     * @param z
     * @return Blocklight value, between 0 and 15
     */
    int getBlocklight(int x, int y, int z) throws CoordinatesOutOfBoundsException;

    /**
     * Set the blocklight at the specified position
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     * @param level Value between 0 and 15 for blocklight
     */
    void setBlocklight(int x, int y, int z, int level) throws CoordinatesOutOfBoundsException;

    void eachBlock(EachBlockCallee action);

    /**
     * Update the state of the chunk to represent changed sunlight
     */
    void finishChangingSunlight();

    /**
     * Get the parent world of the chunk
     */
    IWorld getWorld();

    /**
     * Get the biome type of the chunk
     */
    Biome getBiome();

    /**
     * Whether the chunk as obtained its initial sunlight from World
     */
    boolean hasInitialSun();

    /**
     * Called after the chunk as finished getting initial sunlight fromm world
     */
    void finishAddingSun();

    /**
     * Returns true if the chunk is waiting for a light update to finish before rerendering
     */
    boolean waitingOnLightFinish();

    int getMaxLightLevel();

    interface EachBlockCallee {
        void call(Block block, int x, int y, int z);
    }

}
