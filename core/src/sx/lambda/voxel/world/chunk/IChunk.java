package sx.lambda.voxel.world.chunk;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Disposable;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.IWorld;
import sx.lambda.voxel.world.biome.Biome;

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

    int getBlockId(int x, int y, int z);

    /**
     * Get the block at the specified position
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     */
    Block getBlock(int x, int y, int z);

    /**
     * Remove the block at the specified position
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     */
    void removeBlock(int x, int y, int z);

    /**
     * Set the block at the specified position
     * @param block New block to set to
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     */
    void setBlock(int block, int x, int y, int z);

    /**
     * Set the block at the specified position
     * @param block New block to set to
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     * @param updateSunlight Whether to update sunlight values for the new block
     */
    void setBlock(int block, int x, int y, int z, boolean updateSunlight);

    /**
     * Set the metadata for the block at the specified position
     * @param meta Metadata value to set to
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     */
    void setMeta(short meta, int x, int y, int z);

    /**
     * Get the metadata value at the specified position
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     */
    short getMeta(int x, int y, int z);

    /**
     * Gets the starting position of the chunk
     */
    Vec3i getStartPosition();

    /**
     * @return The y-value of the highest block in the chunk
     */
    int getHighestPoint();

    /**
     * Gets the light value at the specified location
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     */
    float getLightLevel(int x, int y, int z);

    /**
     * Get sunlight at the specified position
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     * @return Sunlight value, between 0 and 16
     */
    int getSunlight(int x, int y, int z);

    /**
     * Set the sunlight at the specified position
     * @param x X value, relative to the chunk. 0->(chunk size - 1)
     * @param y Y value, relative to the chunk. 0->(chunk height - 1)
     * @param z Z value, relative to the chunk. 0->(chunk size - 1)
     * @param level Value between 0 and 16 for sunlight
     */
    void setSunlight(int x, int y, int z, int level);

    /**
     * Render the translucent parts of the chunk
     * @param batch Batch to render onto
     */
    void renderTranslucent(ModelBatch batch);

    int[][][] getBlockIds();

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

    boolean isLighted();

    void setLighted(boolean b);

    /**
     * Returns true if the chunk is waiting for light to finish updating to rerender
     */
    boolean waitingOnLightFinish();

    interface EachBlockCallee {
        void call(Block block, int x, int y, int z);
    }

}
