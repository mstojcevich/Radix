package sx.lambda.voxel.world;

import sx.lambda.voxel.world.chunk.IChunk;

import java.util.Queue;

public class SunlightQueueWorker extends LightQueueWorker {

    public SunlightQueueWorker(IWorld world, Queue<int[]> lightUpdateQueue) {
        super(world, lightUpdateQueue, false);
    }

    @Override
    protected int getLight(IChunk c, int cx, int cy, int cz) {
        return c.getSunlight(cx, cy, cz);
    }

    @Override
    protected void setLight(IChunk c, int cx, int cy, int cz, int newLight) {
        c.setSunlight(cx, cy, cz, newLight);
    }

}
