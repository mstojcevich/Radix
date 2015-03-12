package sx.lambda.mstojcevich.voxel.world;

import io.netty.util.internal.ConcurrentSet;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBOcclusionQuery;
import org.lwjgl.opengl.ARBOcclusionQuery2;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL15;
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

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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

    private Set<IChunk> chunksToRerender = Collections.newSetFromMap(new ConcurrentHashMap<IChunk, Boolean>());

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
            for(IChunk c : chunksToRerender) {
                c.rerender();
                chunksToRerender.remove(c);
            }

            long renderStartNS = System.nanoTime();
            /*IntBuffer ids = BufferUtils.createIntBuffer(this.chunkList.size());
            ARBOcclusionQuery.glGenQueriesARB(ids);


            glDepthMask(false);
            glColorMask(false, false, false, false);
            glDisable(GL_BLEND);
            glDisable(GL_TEXTURE_2D);
            // TODO have an object that keeps track of what the GPU supports, check if it supports ARB_occlusion_query2 or has OpenGL 3.3

            List<IChunk> distSortedChunks = new ArrayList<>(chunkList);
            Collections.sort(distSortedChunks, new Comparator<IChunk>() {
                @Override
                public int compare(IChunk o1, IChunk o2) {
                    return (int)VoxelGame.getInstance().getPlayer().getPosition().planeDistance(o1.getStartPosition().x,
                            o1.getStartPosition().z) -
                            (int)VoxelGame.getInstance().getPlayer().getPosition().planeDistance(o2.getStartPosition().x,
                            o2.getStartPosition().z);
                }
            });

            // https://www.opengl.org/registry/specs/ARB/occlusion_query.txt
            int index = 0;
            for (IChunk c : distSortedChunks) {
                if(index == distSortedChunks.size()-1) { // Closest chunk to the player, so the player's chunk
                    continue;
                }

                int queryId = ids.get();
                ARBOcclusionQuery.glBeginQueryARB(ARBOcclusionQuery2.GL_ANY_SAMPLES_PASSED, queryId);

                if (VoxelGame.getInstance().getFrustum().cubeInFrustum(c.getStartPosition().x, c.getStartPosition().y, c.getStartPosition().z, CHUNK_SIZE, c.getHighestPoint())) {
                    int x = c.getStartPosition().x;
                    int y = c.getStartPosition().y;
                    int z = c.getStartPosition().z;
                    int x2 = x + CHUNK_SIZE;
                    float y2 = c.getHighestPoint();
                    int z2 = z + CHUNK_SIZE;
                    // TODO render this into a VBO on chunk rerender
                    glBegin(GL_QUADS);
                    glVertex3f(x, y, z);
                    glVertex3f(x, y, z2);
                    glVertex3f(x, y2, z2);
                    glVertex3f(x, y2, z);
                    glEnd();

                    glBegin(GL_QUADS);
                    glVertex3f(x, y, z);
                    glVertex3f(x, y, z2);
                    glVertex3f(x, y2, z2);
                    glVertex3f(x, y2, z);
                    glEnd();

                    glBegin(GL_QUADS);
                    glVertex3f(x2, y, z);
                    glVertex3f(x2, y2, z);
                    glVertex3f(x2, y2, z2);
                    glVertex3f(x2, y, z2);
                    glEnd();

                    glBegin(GL_QUADS);
                    glVertex3f(x, y, z);
                    glVertex3f(x2, y, z);
                    glVertex3f(x2, y, z2);
                    glVertex3f(x, y, z2);
                    glEnd();

                    glBegin(GL_QUADS);
                    glVertex3f(x2, y2, z);
                    glVertex3f(x, y2, z);
                    glVertex3f(x, y2, z2);
                    glVertex3f(x2, y2, z2);
                    glEnd();

                    glBegin(GL_QUADS);
                    glVertex3f(x2, y, z);
                    glVertex3f(x, y, z);
                    glVertex3f(x, y2, z);
                    glVertex3f(x2, y2, z);
                    glEnd();
                }

                ARBOcclusionQuery.glEndQueryARB(ARBOcclusionQuery2.GL_ANY_SAMPLES_PASSED);
                System.out.println(ARBOcclusionQuery.glGetQueryObjectuiARB(ids.get(index), ARBOcclusionQuery2.GL_ANY_SAMPLES_PASSED) > 0);

                index++;
            }
            glDepthMask(true);
            glColorMask(true, true, true, true);

            glFlush();

            index = 0;
            for(IChunk c : distSortedChunks) {
                if(index == distSortedChunks.size()-1 || ARBOcclusionQuery.glGetQueryObjectuiARB(ids.get(index), ARBOcclusionQuery2.GL_ANY_SAMPLES_PASSED) > 0) {
                    glPushMatrix();
                    glTranslatef(c.getStartPosition().x, c.getStartPosition().y, c.getStartPosition().z);
                    c.render();
                    glPopMatrix();

                }
                index++;
            }
            */

            for (IChunk c : this.chunkList) {
                if (VoxelGame.getInstance().getFrustum().cubeInFrustum(c.getStartPosition().x, c.getStartPosition().y, c.getStartPosition().z, CHUNK_SIZE, c.getHighestPoint())) {
                    glPushMatrix();
                    glTranslatef(c.getStartPosition().x, c.getStartPosition().y, c.getStartPosition().z);
                    c.render();
                    glPopMatrix();
                }
            }
            for (IChunk c : this.chunkList) {
                if (VoxelGame.getInstance().getFrustum().cubeInFrustum(c.getStartPosition().x, c.getStartPosition().y, c.getStartPosition().z, CHUNK_SIZE, c.getHighestPoint())) {
                    glPushMatrix();
                    glTranslatef(c.getStartPosition().x, c.getStartPosition().y, c.getStartPosition().z);
                    c.renderWater();
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
                rerenderChunk(c);

                if(Math.abs(position.x+(position.x<0?1:0)) % 16 == 15) {
                    if(position.x < 0) {
                        rerenderChunk(getChunkAtPosition(new Vec3i(position.x-1, position.y, position.z)));
                    } else {
                        rerenderChunk(getChunkAtPosition(new Vec3i(position.x+1, position.y, position.z)));
                    }
                } else if(Math.abs(position.x+(position.x<0?1:0)) % 16 == 0) {
                    if(position.x < 0) {
                        rerenderChunk(getChunkAtPosition(new Vec3i(position.x+1, position.y, position.z)));
                    } else {
                        rerenderChunk(getChunkAtPosition(new Vec3i(position.x-1, position.y, position.z)));
                    }
                }

                if(Math.abs(position.z+(position.z<0?1:0)) % 16 == 15) {
                    if(position.z < 0) {
                        rerenderChunk(getChunkAtPosition(new Vec3i(position.x, position.y, position.z - 1)));
                    } else {
                        rerenderChunk(getChunkAtPosition(new Vec3i(position.x, position.y, position.z+1)));
                    }
                } else if(Math.abs(position.z+(position.z<0?1:0)) % 16 == 0) {
                    if(position.z < 0) {
                        rerenderChunk(getChunkAtPosition(new Vec3i(position.x, position.y, position.z+1)));
                    } else {
                        rerenderChunk(getChunkAtPosition(new Vec3i(position.x, position.y, position.z-1)));
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
                rerenderChunk(c);
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
            rerenderChunk(chunk);
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
                rerenderChunk(c);
            }
            return c;
        } else {
            return foundChunk;
        }
    }

    public void addEntity(Entity e) {
        loadedEntities.add(e);
    }

    @Override
    public void rerenderChunk(IChunk c) {
        chunksToRerender.add(c);
    }

}
