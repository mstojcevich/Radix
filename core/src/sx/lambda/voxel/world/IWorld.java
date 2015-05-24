package sx.lambda.voxel.world;

import sx.lambda.voxel.entity.Entity;
import sx.lambda.voxel.entity.EntityPosition;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.chunk.IChunk;
import sx.lambda.voxel.world.generation.ChunkGenerator;

import java.util.List;

public interface IWorld {

    int getChunkSize();

    int getHeight();

    IChunk getChunkAtPosition(Vec3i position);
    IChunk getChunkAtPosition(int x, int z);

    void render();

    void loadChunks(EntityPosition playerPosition, int viewDistance);

    int getSeaLevel();

    /**
     * @return Gravity of the world in m/(s^2)
     */
    float getGravity();

    /**
     * @param velocity Velocity to modify, in m/s
     * @param ms       Time elapsed in MS since last gravity application
     * @return Velocity affected by gravity
     */
    float applyGravity(float velocity, long ms);

    void removeBlock(Vec3i Vec3i);

    void addBlock(int block, Vec3i position);

    IChunk[] getChunksInRange(EntityPosition pos, int viewDistance);

    void addChunk(IChunk chunk);

    void gcChunks(EntityPosition playerPos, int viewDistance);

    List<Entity> getLoadedEntities();

    void addEntity(Entity e);

    void rerenderChunk(IChunk c);

    int getChunkPosition(float value);

    ChunkGenerator getChunkGen();

    /**
     * Add a position to the sunlight queue
     * @param pos int[] {x, y, z}
     */
    void addToSunlightQueue(int[] pos);

    /**
     * Add a position to the sunlight removal queue
     * @param pos int[] {x, y, z}
     */
    void addToSunlightRemovalQueue(int[] pos);

    void processLightQueue();

    float getLightLevel(Vec3i pos);

    void cleanup();

    int getNumChunksMeshing();
    void incrChunksMeshing();
    void decrChunksMeshing();

}
