package sx.lambda.mstojcevich.voxel.world.chunk;

import sx.lambda.mstojcevich.voxel.block.Block;
import sx.lambda.mstojcevich.voxel.util.Vec3i;

import java.io.Serializable;

public interface IChunk extends Serializable {
	
	/**
	 * Redraws all of the blocks in the chunk
	 */
	public void rerender();
	
	/**
	 * Renders the chunk 
	 */
	public void render();
	
	public Block getBlockAtPosition(Vec3i position);

    public void removeBlock(Vec3i Vec3i);

    public void addBlock(Block block, Vec3i position);

    public void unload();

    public Vec3i getStartPosition();

    public float getHighestPoint();

    public float getLightLevel(int x, int y, int z);

}
