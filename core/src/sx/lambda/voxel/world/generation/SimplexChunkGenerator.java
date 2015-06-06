package sx.lambda.voxel.world.generation;

import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.api.VoxelGameAPI;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.IWorld;
import sx.lambda.voxel.world.chunk.BlockStorage;
import sx.lambda.voxel.world.chunk.BlockStorage.CoordinatesOutOfBoundsException;

/**
 * Generates chunks using simplex noise
 */
public class SimplexChunkGenerator implements ChunkGenerator {

    // Not entirely sure what this does, but 0.05 works fine
    private static final float PERSISTANCE = 0.05f;

    private final SimplexNoise noise;
    private final IWorld world;

    public SimplexChunkGenerator(IWorld world, int largestFeature, int seed) {
        this.world = world;
        this.noise = new SimplexNoise(largestFeature, PERSISTANCE, seed);
    }

    @Override
    public int generate(Vec3i startPosition, BlockStorage storage) {
        int highestPoint = world.getSeaLevel();
        for (int x = 0; x < storage.getWidth(); x++) {
            for (int z = 0; z < storage.getDepth(); z++) {
                int distFromSeaLevel = getHeightAboveSeaLevel(startPosition.x + x, startPosition.z + z);
                int yMax = world.getSeaLevel() + distFromSeaLevel;
                if (yMax < world.getSeaLevel()) {
                    for (int y = yMax; y < world.getSeaLevel(); y++) {
                        int blockType = BuiltInBlockIds.WATER_ID;
                        if (y == yMax) {
                            blockType = BuiltInBlockIds.SAND_ID;
                        }
                        try {
                            storage.setId(x, y, z, blockType);
                            storage.setBlock(x, y, z, VoxelGameAPI.instance.getBlockByID(blockType));
                        } catch(CoordinatesOutOfBoundsException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                for (int y = 0; y < yMax; y++) {
                    int blockType = BuiltInBlockIds.STONE_ID;

                    if (y == world.getSeaLevel() - 1 && y == yMax - 1) {
                        try {
                            storage.setId(x, y + 1, z, BuiltInBlockIds.SAND_ID);
                            storage.setBlock(x, y + 1, z, VoxelGameAPI.instance.getBlockByID(BuiltInBlockIds.SAND_ID));
                            highestPoint = Math.max(highestPoint, y + 1);
                        } catch(CoordinatesOutOfBoundsException ex) {
                            ex.printStackTrace();
                        }
                    }

                    if (y == yMax - 1) {
                        blockType = BuiltInBlockIds.GRASS_ID;
                    } else if (y > yMax - 5) {
                        blockType = BuiltInBlockIds.DIRT_ID;
                    }
                    try {
                        storage.setId(x, y, z, blockType);
                        storage.setBlock(x, y, z, VoxelGameAPI.instance.getBlockByID(blockType));
                    } catch (CoordinatesOutOfBoundsException ex) {
                        ex.printStackTrace();
                    }
                    highestPoint = Math.max(highestPoint, y);
                }
            }
        }

        return highestPoint;
    }

    private int getHeightAboveSeaLevel(int x, int z) {
        return (int) Math.round(100 * this.noise.getNoise(x, z));
    }

}
