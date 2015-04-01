package sx.lambda.mstojcevich.voxel.world.chunk;

import groovy.lang.Closure;
import sx.lambda.mstojcevich.voxel.block.Block;
import sx.lambda.mstojcevich.voxel.util.Vec3i;
import sx.lambda.mstojcevich.voxel.world.IWorld;

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
	
	Block getBlockAtPosition(Vec3i position);

    void removeBlock(Vec3i Vec3i);

    void addBlock(Block block, Vec3i position);

    Vec3i getStartPosition();

    float getHighestPoint();

    float getLightLevel(int x, int y, int z);

    void renderWater();

    int[] blocksToIdInt();

    void eachBlock(Closure action);

    void setSunlight(int x, int y, int z, int level);
    int getSunlight(int x, int y, int z);

    public void finishChangingSunlight();

    public IWorld getWorld();

    public void cleanup();

}
