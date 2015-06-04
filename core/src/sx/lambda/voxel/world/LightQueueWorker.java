package sx.lambda.voxel.world;

import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.block.Side;
import sx.lambda.voxel.world.chunk.IChunk;

import java.util.Queue;

/**
 * Worker that processes a light update queue
 */
class LightQueueWorker extends Thread {

    private final Queue<int[]> lightUpdateQueue;
    private final IWorld world;
    private final Side[] sides;

    public LightQueueWorker(IWorld world, Queue<int[]> lightUpdateQueue) {
        this.world = world;
        this.lightUpdateQueue = lightUpdateQueue;
        this.sides = Side.values();
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
                    int ll = posChunk.getSunlight(cx, y, cz);

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

                        // Spread lighting
                        Block sBlock = sChunk.getBlock(scx, sy, scz);
                        // When spreading down, lighting at max level does not decay
                        if (s == Side.BOTTOM) {
                            Block block = posChunk.getBlock(cx, y, cz); // Block being spread from
                            if (ll == 16 && (block == null || block.decreasesLight()))
                                nextLL = 16;
                        }
                        if (sBlock == null || sBlock.doesLightPassThrough() || !sBlock.decreasesLight()) {
                            if (sChunk.getSunlight(scx, sy, scz) < nextLL) {
                                sChunk.setSunlight(scx, sy, scz, nextLL);
                                lightUpdateQueue.add(new int[]{sx, sy, sz});
                                updatedLight = true;
                            }
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

}
