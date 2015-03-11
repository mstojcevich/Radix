package sx.lambda.mstojcevich.voxel.world;

import io.netty.util.internal.ConcurrentSet;
import sx.lambda.mstojcevich.voxel.VoxelGame;
import sx.lambda.mstojcevich.voxel.api.VoxelGameAPI;
import sx.lambda.mstojcevich.voxel.api.events.worldgen.EventFinishChunkGen;
import sx.lambda.mstojcevich.voxel.block.Block;
import sx.lambda.mstojcevich.voxel.entity.Entity;
import sx.lambda.mstojcevich.voxel.util.Vec3i;
import sx.lambda.mstojcevich.voxel.entity.EntityPosition;
import sx.lambda.mstojcevich.voxel.world.chunk.Chunk;
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk;
import sx.lambda.mstojcevich.voxel.world.generation.SimplexNoise;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lwjgl.opengl.GL11.*;

public class World implements IWorld {
	
	private static final int CHUNK_SIZE = 16;

    private static final int WORLD_HEIGHT = 128;

    private static final int SEA_LEVEL = 64;

    private final Map<Vec3i, IChunk> chunkMap = new ConcurrentHashMap<>();
    private final Set<IChunk> chunkList = new ConcurrentSet<>();

    private static final float GRAVITY = 4.69f;

    private static final float TERMINAL_VELOCITY = 56;

    private final SimplexNoise noise;

    private final boolean remote, server;

    private List<Entity> loadedEntities = new CopyOnWriteArrayList<>();

    public World(boolean remote, boolean server) {
        this.remote = remote;
        this.server = server;
        if(!remote) {
            this.noise = new SimplexNoise(100, 0.05, new Random().nextInt());
        } else {
            this.noise = null;
        }
    }
	
	public int getChunkSize() {
		return CHUNK_SIZE;
	}

    public int getHeight() { return WORLD_HEIGHT; }

    public IChunk getChunkAtPosition(Vec3i position) {
        Vec3i chunkPosition = new Vec3i(
                getChunkPosition(position.x),
                0,
                getChunkPosition(position.z));

        return this.chunkMap.get(chunkPosition);
    }

    public void render() {
        if(!server) {
            long renderStartNS = System.nanoTime();
            for (IChunk c : this.chunkList) {
                if (VoxelGame.getInstance().getFrustum().cubeInFrustum(c.getStartPosition().x, c.getStartPosition().y, c.getStartPosition().z, CHUNK_SIZE, c.getHighestPoint())) {
                    glPushMatrix();
                    glTranslatef(c.getStartPosition().x, c.getStartPosition().y, c.getStartPosition().z);
                    c.render();
                    glPopMatrix();
                }
            }
            if(VoxelGame.getInstance().numChunkRenders == 100) {  // Reset every 100 renders
                VoxelGame.getInstance().numChunkRenders = 0;
                VoxelGame.getInstance().chunkRenderTimes = 0;
            }
            VoxelGame.getInstance().chunkRenderTimes += (int)(System.nanoTime() - renderStartNS);
            VoxelGame.getInstance().numChunkRenders++;
        } else {
            System.err.println("Why the hell is the server running render?");
        }
    }

    @Override
    public void loadChunks(EntityPosition playerPosition, int viewDistance) {
        if(!remote) { //don't gen chunks if we're not local
            //TODO Make sure all of these values apply to the chunkGC check
            this.getChunksInRange(playerPosition, viewDistance);
        }

        gcChunks(playerPosition, viewDistance);
    }

    @Override
    public int getSeaLevel() { return SEA_LEVEL; }

    @Override
    public int getHeightAboveSeaLevel(int x, int z) {
        return (int)Math.round(100*this.noise.getNoise(x, z));
    }

    private int getChunkPosition(float value) {
        int subtraction = (int)(value%CHUNK_SIZE);
        if(value <= 0 && subtraction != 0) {
            subtraction = CHUNK_SIZE+subtraction;
        }
        return (int)(value-subtraction);
    }

    @Override
    public float getGravity() {
        return GRAVITY;
    }

    @Override
    public float applyGravity(float velocity, long ms) {
        if(ms < 0)ms = 0-ms;
        return Math.max(-TERMINAL_VELOCITY, velocity-(getGravity()/1000)*(ms/10f));
    }

    @Override
    public void removeBlock(final Vec3i position) {
        synchronized (this) {
            final IChunk c = this.getChunkAtPosition(position);
            c.removeBlock(position);
            if(!server) {
                VoxelGame.getInstance().addToGLQueue(new Runnable() {
                    @Override
                    public void run() {
                        c.rerender();
                    }
                });

                if(Math.abs(position.x+(position.x<0?1:0)) % 16 == 15) {
                    if(position.x < 0) {
                        VoxelGame.getInstance().addToGLQueue(new Runnable() {
                            @Override
                            public void run() {
                                getChunkAtPosition(new Vec3i(position.x-1, position.y, position.z)).rerender();
                            }
                        });
                    } else {
                        VoxelGame.getInstance().addToGLQueue(new Runnable() {
                            @Override
                            public void run() {
                                getChunkAtPosition(new Vec3i(position.x+1, position.y, position.z)).rerender();
                            }
                        });
                    }
                } else if(Math.abs(position.x+(position.x<0?1:0)) % 16 == 0) {
                    if(position.x < 0) {
                        VoxelGame.getInstance().addToGLQueue(new Runnable() {
                            @Override
                            public void run() {
                                getChunkAtPosition(new Vec3i(position.x+1, position.y, position.z)).rerender();
                            }
                        });
                    } else {
                        VoxelGame.getInstance().addToGLQueue(new Runnable() {
                            @Override
                            public void run() {
                                getChunkAtPosition(new Vec3i(position.x-1, position.y, position.z)).rerender();
                            }
                        });
                    }
                }

                if(Math.abs(position.z+(position.z<0?1:0)) % 16 == 15) {
                    if(position.z < 0) {
                        VoxelGame.getInstance().addToGLQueue(new Runnable() {
                            @Override
                            public void run() {
                                getChunkAtPosition(new Vec3i(position.x, position.y, position.z - 1)).rerender();
                            }
                        });
                    } else {
                        VoxelGame.getInstance().addToGLQueue(new Runnable() {
                            @Override
                            public void run() {
                                getChunkAtPosition(new Vec3i(position.x, position.y, position.z+1)).rerender();
                            }
                        });
                    }
                } else if(Math.abs(position.z+(position.z<0?1:0)) % 16 == 0) {
                    if(position.z < 0) {
                        VoxelGame.getInstance().addToGLQueue(new Runnable() {
                            @Override
                            public void run() {
                                getChunkAtPosition(new Vec3i(position.x, position.y, position.z+1)).rerender();
                            }
                        });
                    } else {
                        VoxelGame.getInstance().addToGLQueue(new Runnable() {
                            @Override
                            public void run() {
                                getChunkAtPosition(new Vec3i(position.x, position.y, position.z-1)).rerender();
                            }
                        });
                    }
                }
            }
        }
    }

    @Override
    public void addBlock(Block block, final Vec3i position) {
        synchronized(this) {
            final IChunk c = this.getChunkAtPosition(position);
            c.addBlock(block, position);
            if(!server) {
                VoxelGame.getInstance().addToGLQueue(new Runnable() {
                    @Override
                    public void run() {
                        c.rerender();
                    }
                });
            }
        }
    }

    @Override
    public IChunk[] getChunksInRange(EntityPosition epos, int viewDistance) {
        List<IChunk> chunkList = new ArrayList<IChunk>();
        int playerChunkX = getChunkPosition(epos.getX());
        int playerChunkZ = getChunkPosition(epos.getZ());
        int range = viewDistance*CHUNK_SIZE;
        for (int x = playerChunkX - range; x <= playerChunkX + range; x += CHUNK_SIZE) {
            for (int z = playerChunkZ - range; z <= playerChunkZ + range; z += CHUNK_SIZE) {
                chunkList.add(loadChunk(x, z));
            }
        }
        return chunkList.toArray(new IChunk[chunkList.size()]);
    }

    @Override
    public void addChunk(final IChunk chunk) {
        Vec3i pos = chunk.getStartPosition();
        IChunk c = this.chunkMap.get(pos);
        if(c != null) {
            this.chunkMap.remove(pos);
            this.chunkList.remove(c);
        }
        this.chunkMap.put(pos, chunk);
        this.chunkList.add(chunk);
        if(!server) {
            VoxelGame.getInstance().addToGLQueue(new Runnable() {
                @Override
                public void run() {
                    chunk.rerender();
                }
            });
        }
    }

    @Override
    public void gcChunks(EntityPosition playerPosition, int viewDistance) {
        int range = viewDistance*CHUNK_SIZE;

        int playerChunkX = getChunkPosition(playerPosition.getX());
        int playerChunkZ = getChunkPosition(playerPosition.getZ());

        for(Map.Entry<Vec3i, IChunk> e : this.chunkMap.entrySet()) {
            Vec3i b = e.getKey();
            if(Math.abs(b.x - playerChunkX) > range
                    || Math.abs(b.z - playerChunkZ) > range) {
                this.chunkList.remove(e.getValue());
                this.chunkMap.get(b).unload();
                this.chunkMap.remove(b);
            }
        }
    }

    @Override
    public List<Entity> getLoadedEntities() {
        return this.loadedEntities;
    }

    private IChunk loadChunk(int startX, int startZ) {
        Vec3i pos = new Vec3i(startX, 0, startZ);
        IChunk foundChunk = chunkMap.get(pos);
        if (foundChunk == null && !remote) {
            final IChunk c = new Chunk(this, pos);
            VoxelGameAPI.instance.getEventManager().push(new EventFinishChunkGen(c));
            this.chunkMap.put(pos, c);
            this.chunkList.add(c);
            if(!server) {
                VoxelGame.getInstance().addToGLQueue(new Runnable() {
                    @Override
                    public void run() {
                        c.rerender();
                    }
                });
            }
            return c;
        } else {
            return foundChunk;
        }
    }

    public void addEntity(Entity e) {
        loadedEntities.add(e);
    }

}
