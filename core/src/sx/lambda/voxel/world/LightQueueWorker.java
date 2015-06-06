package sx.lambda.voxel.world;

import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.block.Side;
import sx.lambda.voxel.world.chunk.BlockStorage.CoordinatesOutOfBoundsException;
import sx.lambda.voxel.world.chunk.IChunk;

import java.util.Queue;

/**
 * Worker that processes a light update queue
 */
abstract class LightQueueWorker extends Thread {

    private final Queue<int[]> lightUpdateQueue;
    private final IWorld world;
    private final Side[] sides;
    private final boolean decayDown;

    /**
     * @param world World that light is updating in
     * @param lightUpdateQueue Light queue to process
     * @param decayDown Whether light should decay when heading down and at full brightness.
     *                  This should be set to false for sunlight since sunlight doesn't decay when heading down at max brightness.
     */
    public LightQueueWorker(IWorld world, Queue<int[]> lightUpdateQueue, boolean decayDown) {
        this.world = world;
        this.lightUpdateQueue = lightUpdateQueue;
        this.sides = Side.values();
        this.decayDown = decayDown;
    }

    @Override
    public void run() {
        while(true) {
            try {
                boolean updatedLight = false;
                while(!lightUpdateQueue.isEmpty()) {
                    int chunkSize = world.getChunkSize();

                    int[] pos = lightUpdateQueue.poll();
                    int x = pos[0];
                    int y = pos[1];
                    int z = pos[2];
                    // cx and cz are relative to the chunk
                    int cx = x & (chunkSize - 1);
                    int cz = z & (chunkSize - 1);

                    IChunk posChunk = world.getChunk(x, z);
                    if (posChunk == null) {
                        continue;
                    }
                    int ll = getLight(posChunk, cx, y, cz);

                    // Spread off to each side
                    for (Side s : sides) {
                        int nextLL = ll - 1; // Decayed light level for the spread
                        int sx = x; // Side x coord
                        int sy = y; // Side y coord
                        int sz = z; // Side z coord
                        int scx = cx; // Chunk-relative side x coord
                        int scz = cz; // Chunk-relative side z coord
                        IChunk sChunk = posChunk;

                        // Offset values based on side
                        switch (s) {
                            case TOP:
                                sy += 1;
                                break;
                            case BOTTOM:
                                sy -= 1;
                                break;
                            case WEST:
                                sx -= 1;
                                scx -= 1;
                                break;
                            case EAST:
                                sx += 1;
                                scx += 1;
                                break;
                            case NORTH:
                                sz += 1;
                                scz += 1;
                                break;
                            case SOUTH:
                                sz -= 1;
                                scz -= 1;
                                break;
                        }
                        if (sy < 0)
                            continue;
                        if (sy > world.getHeight() - 1)
                            continue;

                        // Select the correct chunk
                        if (scz < 0) {
                            scz += chunkSize;
                            sChunk = world.getChunk(sx, sz);
                        } else if (scz > chunkSize - 1) {
                            scz -= chunkSize;
                            sChunk = world.getChunk(sx, sz);
                        }
                        if (scx < 0) {
                            scx += chunkSize;
                            sChunk = world.getChunk(sx, sz);
                        } else if (scx > chunkSize - 1) {
                            scx -= chunkSize;
                            sChunk = world.getChunk(sx, sz);
                        }

                        if (sChunk == null)
                            continue;

                        try {
                            // Spread lighting
                            Block sBlock = sChunk.getBlock(scx, sy, scz);
                            // When spreading down, lighting at max level does not decay
                            if (!decayDown && s == Side.BOTTOM) {
                                Block block = posChunk.getBlock(cx, y, cz); // Block being spread from
                                if (ll == posChunk.getMaxLightLevel() && (block == null || block.decreasesLight()))
                                    nextLL = posChunk.getMaxLightLevel();
                            }
                            if (sBlock == null || sBlock.doesLightPassThrough() || !sBlock.decreasesLight()) {
                                if (getLight(sChunk, scx, sy, scz) < nextLL) {
                                    setLight(sChunk, scx, sy, scz, nextLL);
                                    lightUpdateQueue.add(new int[]{sx, sy, sz});
                                    updatedLight = true;
                                }
                            }
                        } catch (CoordinatesOutOfBoundsException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                if(updatedLight) {
                    synchronized (lightUpdateQueue) {
                        lightUpdateQueue.notifyAll();
                    }
                }

                synchronized (lightUpdateQueue) {
                    lightUpdateQueue.wait();
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    protected abstract int getLight(IChunk c, int cx, int cy, int cz);

    protected abstract void setLight(IChunk c, int cx, int cy, int cz, int newLight);

}
