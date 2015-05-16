package sx.lambda.voxel.world.chunk;

import groovy.lang.Closure;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.IWorld;

import java.io.Serializable;

public interface IChunk extends Serializable {
	
	/**
	 * Redraws all of the blocks in the chunk
	 */
	void rerender();
	
	/**
	 * Renders the chunk 
	 */
	void render();

    int getBlockIdAtPosition(int x, int y, int z);
	Block getBlockAtPosition(Vec3i position);

    void removeBlock(Vec3i Vec3i);

    void addBlock(int block, Vec3i position);

    Vec3i getStartPosition();

    float getHighestPoint();

    float getLightLevel(int x, int y, int z);

    void renderWater();

    int[][][] blocksToIdInt();

    void eachBlock(Closure action);

    void setSunlight(int x, int y, int z, int level);
    int getSunlight(int x, int y, int z);

    public void finishChangingSunlight();

    public IWorld getWorld();

    public void cleanup();

}
