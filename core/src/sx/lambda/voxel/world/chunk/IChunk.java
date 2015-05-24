package sx.lambda.voxel.world.chunk;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
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
    void render(ModelBatch batch);

    int getBlockIdAtPosition(int x, int y, int z);

    Block getBlockAtPosition(int x, int y, int z);
    Block getBlockAtPosition(Vec3i position);

    void removeBlock(int x, int y, int z);

    void addBlock(int block, int x, int y, int z);

    void addBlock(int block, int x, int y, int z, boolean updateSunlight);

    Vec3i getStartPosition();

    float getHighestPoint();

    float getLightLevel(int x, int y, int z);

    void renderWater(ModelBatch batch);

    int[][][] blocksToIdInt();

    void eachBlock(EachBlockCallee action);

    void setSunlight(int x, int y, int z, int level);

    void setSunlight(int x, int y, int z, int level, boolean updateNeighbors);

    int getSunlight(int x, int y, int z);

    void finishChangingSunlight();

    IWorld getWorld();

    void cleanup();

    interface EachBlockCallee {
        void call(Block block, int x, int y, int z);
    }

}
