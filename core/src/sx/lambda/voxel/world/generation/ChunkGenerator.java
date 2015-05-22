package sx.lambda.voxel.world.generation;

import sx.lambda.voxel.util.Vec3i;

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
    int generate(Vec3i startPosition, int[][][] blocks);

}
