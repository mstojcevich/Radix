package sx.lambda.voxel.world;

import sx.lambda.voxel.world.chunk.BlockStorage.CoordinatesOutOfBoundsException;
import sx.lambda.voxel.world.chunk.IChunk;

import java.util.Queue;

public class SunlightQueueWorker extends LightQueueWorker {

    public SunlightQueueWorker(IWorld world, Queue<int[]> lightUpdateQueue) {
        super(world, lightUpdateQueue, false);
    }

    @Override
    protected int getLight(IChunk c, int cx, int cy, int cz) {
        try {
            return c.getSunlight(cx, cy, cz);
        } catch (CoordinatesOutOfBoundsException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    protected void setLight(IChunk c, int cx, int cy, int cz, int newLight) {
        try {
            c.setSunlight(cx, cy, cz, newLight);
        } catch (CoordinatesOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

}
