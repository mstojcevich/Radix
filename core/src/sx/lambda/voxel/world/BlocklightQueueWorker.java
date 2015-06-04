package sx.lambda.voxel.world;

import sx.lambda.voxel.world.chunk.IChunk;

import java.util.Queue;

public class BlocklightQueueWorker extends LightQueueWorker {

    public BlocklightQueueWorker(IWorld world, Queue<int[]> lightUpdateQueue) {
        super(world, lightUpdateQueue, true);
    }

    @Override
    protected int getLight(IChunk c, int cx, int cy, int cz) {
        return c.getBlocklight(cx, cy, cz);
    }

    @Override
    protected void setLight(IChunk c, int cx, int cy, int cz, int newLight) {
        c.setBlocklight(cx, cy, cz, newLight);
    }

}
