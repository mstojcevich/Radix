package sx.lambda.mstojcevich.voxel.world

import groovy.transform.CompileStatic;
import io.netty.util.internal.ConcurrentSet
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL15;
import sx.lambda.mstojcevich.voxel.VoxelGame;
import sx.lambda.mstojcevich.voxel.api.VoxelGameAPI;
import sx.lambda.mstojcevich.voxel.api.events.worldgen.EventFinishChunkGen;
import sx.lambda.mstojcevich.voxel.block.Block
import sx.lambda.mstojcevich.voxel.block.IBlockRenderer;
import sx.lambda.mstojcevich.voxel.entity.Entity;
import sx.lambda.mstojcevich.voxel.util.Vec3i;
import sx.lambda.mstojcevich.voxel.entity.EntityPosition;
import sx.lambda.mstojcevich.voxel.world.chunk.Chunk;
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk;
import sx.lambda.mstojcevich.voxel.world.generation.SimplexNoise

import java.nio.FloatBuffer
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingDeque;

import static org.lwjgl.opengl.GL11.*;

@CompileStatic
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

    private transient int liquidVertexVbo = -1;
    private transient int liquidTextureVbo = -1;
    private transient int liquidNormalVbo = -1;
    private transient int liquidColorVbo = -1;
    private transient int liquidVisibleSides = 0

    private boolean toRerenderLiquids = true

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
            if(toRerenderLiquids) {
                actuallyRerenderLiquids()
                toRerenderLiquids = false
            }
            long renderStartNS = System.nanoTime();
            for (IChunk c : this.chunkList) {
                if (VoxelGame.getInstance().getFrustum().cubeInFrustum(c.getStartPosition().x, c.getStartPosition().y, c.getStartPosition().z, CHUNK_SIZE, c.getHighestPoint())) {
                    glPushMatrix();
                    glTranslatef(c.getStartPosition().x, c.getStartPosition().y, c.getStartPosition().z);
                    c.render();
                    glPopMatrix();
                }
            }

            VoxelGame.instance.shaderManager.enableWave()
            glEnable(GL_BLEND)
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, liquidVertexVbo)
            glVertexPointer(3, GL_FLOAT, 0, 0)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, liquidTextureVbo)
            glTexCoordPointer(2, GL_FLOAT, 0, 0)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, liquidNormalVbo)
            glNormalPointer(GL_FLOAT, 0, 0)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, liquidColorVbo)
            glColorPointer(4, GL_FLOAT, 0, 0)

            glDrawArrays(GL_QUADS, 0, liquidVisibleSides*4)
            glDisable(GL_BLEND)
            VoxelGame.instance.shaderManager.disableWave()

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
                        c.rerender()
                        rerenderLiquids()
                    }
                });

                if(Math.abs(position.x+(position.x<0?1:0)) % 16 == 15) {
                    if(position.x < 0) {
                        VoxelGame.getInstance().addToGLQueue(new Runnable() {
                            @Override
                            public void run() {
                                getChunkAtPosition(new Vec3i(position.x-1, position.y, position.z)).rerender();
                                rerenderLiquids()
                            }
                        });
                    } else {
                        VoxelGame.getInstance().addToGLQueue(new Runnable() {
                            @Override
                            public void run() {
                                getChunkAtPosition(new Vec3i(position.x+1, position.y, position.z)).rerender();
                                rerenderLiquids()
                            }
                        });
                    }
                } else if(Math.abs(position.x+(position.x<0?1:0)) % 16 == 0) {
                    if(position.x < 0) {
                        VoxelGame.getInstance().addToGLQueue(new Runnable() {
                            @Override
                            public void run() {
                                getChunkAtPosition(new Vec3i(position.x+1, position.y, position.z)).rerender();
                                rerenderLiquids()
                            }
                        });
                    } else {
                        VoxelGame.getInstance().addToGLQueue(new Runnable() {
                            @Override
                            public void run() {
                                getChunkAtPosition(new Vec3i(position.x-1, position.y, position.z)).rerender();
                                rerenderLiquids()
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
                                rerenderLiquids()
                            }
                        });
                    } else {
                        VoxelGame.getInstance().addToGLQueue(new Runnable() {
                            @Override
                            public void run() {
                                getChunkAtPosition(new Vec3i(position.x, position.y, position.z+1)).rerender();
                                rerenderLiquids()
                            }
                        });
                    }
                } else if(Math.abs(position.z+(position.z<0?1:0)) % 16 == 0) {
                    if(position.z < 0) {
                        VoxelGame.getInstance().addToGLQueue(new Runnable() {
                            @Override
                            public void run() {
                                getChunkAtPosition(new Vec3i(position.x, position.y, position.z+1)).rerender();
                                rerenderLiquids()
                            }
                        });
                    } else {
                        VoxelGame.getInstance().addToGLQueue(new Runnable() {
                            @Override
                            public void run() {
                                getChunkAtPosition(new Vec3i(position.x, position.y, position.z-1)).rerender();
                                rerenderLiquids()
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
                        rerenderLiquids()
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
                    rerenderLiquids()
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
                        rerenderLiquids()
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

    private void rerenderLiquids() {
        this.toRerenderLiquids = true
    }

    private void actuallyRerenderLiquids() {
        if(liquidVertexVbo == -1 || liquidVertexVbo == 0) {
            IntBuffer buffer = BufferUtils.createIntBuffer(4)
            GL15.glGenBuffers(buffer)
            liquidVertexVbo = buffer.get(0)
            liquidTextureVbo = buffer.get(1)
            liquidNormalVbo = buffer.get(2)
            liquidColorVbo = buffer.get(3)

            glEnableClientState(GL_NORMAL_ARRAY)
            glEnableClientState(GL_VERTEX_ARRAY)
            glEnableClientState(GL_TEXTURE_COORD_ARRAY)
            glEnableClientState(GL_COLOR_ARRAY)
        }

        Queue<Vec3i> liquidBlocks = new LinkedBlockingDeque<>();
        boolean[][] shouldRenderSides = new boolean[CHUNK_SIZE*CHUNK_SIZE*WORLD_HEIGHT][6]
        int totalVisibleFaceCount = 0
        for(IChunk c : chunkList) {
            boolean[][][] shouldRenderTopC = new boolean[CHUNK_SIZE][WORLD_HEIGHT][CHUNK_SIZE];
            boolean[][][] shouldRenderBottomC = new boolean[CHUNK_SIZE][WORLD_HEIGHT][CHUNK_SIZE];
            boolean[][][] shouldRenderLeftC = new boolean[CHUNK_SIZE][WORLD_HEIGHT][CHUNK_SIZE];
            boolean[][][] shouldRenderRightC = new boolean[CHUNK_SIZE][WORLD_HEIGHT][CHUNK_SIZE];
            boolean[][][] shouldRenderFrontC = new boolean[CHUNK_SIZE][WORLD_HEIGHT][CHUNK_SIZE];
            boolean[][][] shouldRenderBackC = new boolean[CHUNK_SIZE][WORLD_HEIGHT][CHUNK_SIZE];

            int liquidFaceCount = c.liquidBlockCount*6

            liquidFaceCount = c.calcShouldRender({it == Block.WATER}, liquidFaceCount,
                    shouldRenderTopC, shouldRenderBottomC,
                    shouldRenderLeftC, shouldRenderRightC,
                    shouldRenderFrontC, shouldRenderBackC);

            totalVisibleFaceCount += liquidFaceCount

            for(int x = 0; x < CHUNK_SIZE; x++) {
                for(int y = 0; y < WORLD_HEIGHT; y++) {
                    for(int z = 0; z < CHUNK_SIZE; z++) {
                        Vec3i pos = new Vec3i(x, y, z)
                        Block b = c.getBlockAtPosition(pos)
                        if(b == Block.WATER) {
                            liquidBlocks.push(pos)
                            boolean[] srs = [shouldRenderTopC[x][y][z],
                                shouldRenderBottomC[x][y][z],
                                shouldRenderLeftC[x][y][z],
                                shouldRenderRightC[x][y][z],
                                shouldRenderFrontC[x][y][z],
                                shouldRenderBackC[x][y][z]];
                            shouldRenderSides[liquidBlocks.size()-1] = srs
                        }
                    }
                }
            }

            IBlockRenderer waterRenderer = Block.WATER.renderer
            FloatBuffer vertexPosData = BufferUtils.createFloatBuffer(totalVisibleFaceCount*4*3)
            FloatBuffer textureData = BufferUtils.createFloatBuffer(totalVisibleFaceCount*4*2)
            FloatBuffer normalData = BufferUtils.createFloatBuffer(totalVisibleFaceCount*4*3)
            FloatBuffer colorData = BufferUtils.createFloatBuffer(totalVisibleFaceCount*4*4)

            //TODO get light levels from the respective chunks
            float[][][] lightLevels = new float[CHUNK_SIZE][WORLD_HEIGHT][CHUNK_SIZE]
            for(int r = 0; r < CHUNK_SIZE; r++) {
                for(int g = 0; g < WORLD_HEIGHT; g++) {
                    for(int b = 0; b < CHUNK_SIZE; b++) {
                        lightLevels[r][g][b] = 1.0f;
                    }
                }
            }
            Vec3i pos
            while((pos = liquidBlocks.poll()) != null) {
                boolean[] srs = shouldRenderSides[liquidBlocks.size()]
                waterRenderer.renderVBO(pos.x, pos.y, pos.z, lightLevels,
                        vertexPosData, textureData, normalData, colorData,
                        srs[0], srs[1], srs[2], srs[3], srs[4], srs[5])
            }

            vertexPosData.flip()
            textureData.flip()
            normalData.flip()
            colorData.flip()

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, liquidVertexVbo)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexPosData, GL15.GL_STATIC_DRAW)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, liquidTextureVbo)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureData, GL15.GL_STATIC_DRAW)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, liquidNormalVbo)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalData, GL15.GL_STATIC_DRAW)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, liquidColorVbo)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorData, GL15.GL_STATIC_DRAW)
        }
        this.liquidVisibleSides = totalVisibleFaceCount
    }

}
