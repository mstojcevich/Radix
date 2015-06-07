package sx.lambda.voxel.world.generation;

import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.chunk.BlockStorage;
import sx.lambda.voxel.world.chunk.IChunk;

/**
 * Method of creating voxels for a chunk
 */
public interface ChunkGenerator {

    /**
     * Fills a block array with generated blocks
     *
     * @param startPosition Start position of the chunk to generate
     * @param blocks        Empty 3d array of blocks the size of the chunk
     * @return The maximum height of the generated blocks
     */
    int generate(Vec3i startPosition, IChunk chunk);

}
