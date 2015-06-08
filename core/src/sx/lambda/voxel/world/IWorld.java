package sx.lambda.voxel.world;

import sx.lambda.voxel.entity.Entity;
import sx.lambda.voxel.entity.EntityPosition;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.chunk.IChunk;
import sx.lambda.voxel.world.generation.ChunkGenerator;

import java.util.List;

public interface IWorld {

    /**
     * Gets the chunk size used in the world.
     *
     * The number returned should be a 2^x number.
     *
     * @return 2^x number representing chunk size
     */
    int getChunkSize();

    int getHeight();

    IChunk getChunk(Vec3i position);
    IChunk getChunk(int x, int z);

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

    void removeBlock(int x, int y, int z);

    void addBlock(int block, int x, int y, int z);

    IChunk[] getChunksInRange(EntityPosition pos, int viewDistance);

    void addChunk(IChunk chunk);

    List<Entity> getLoadedEntities();

    void addEntity(Entity e);

    void rerenderChunk(IChunk c);

    int getChunkPosition(float value);

    ChunkGenerator getChunkGen();

    /**
     * Add a block to a list of blocks to process sunlight for
     * The block at the position passed should be translucent or null and have a sunlight level greater than 0
     */
    void addToSunlightQueue(int x, int y, int z);

    /**
     * Add a block to the list of blocks to process block light for
     * The block at the position should not be null and should have a blocklight level greater than 0.
     */
    void addToBlocklightQueue(int x, int y, int z);

    /**
     * Add a position to the sunlight removal queue
     */
    void addToSunlightRemovalQueue(int x, int y, int z);

    void processLightQueue();

    void cleanup();

    void rerenderChunks();

    void rmChunk(IChunk chunk);

    /**
     * Add to the queue of chunk meshing.
     * This queue may be processed on another thread, so shouldn't do any GL stuff.
     *
     * This method is for internal use by chunks!
     *
     * @param updateFaces Method that generates the mesh
     */
    void addToMeshQueue(Runnable updateFaces);

    /**
     * Add to the queue of chunks to upload.
     * This queue is used to distribute chunk uploading across frames if the option is enabled.
     * This queue is processed on the main thread, so gl can be used (and the mesh queue should be used for non-gl).
     *
     * This method is for internal use by chunks!
     *
     * @param upload Method that uploads the chunk
     */
    void addToChunkUploadQueue(Runnable upload);

}
