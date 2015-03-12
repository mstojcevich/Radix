package sx.lambda.mstojcevich.voxel.world.generation;

import sx.lambda.mstojcevich.voxel.block.Block;
import sx.lambda.mstojcevich.voxel.util.Vec3i;
import sx.lambda.mstojcevich.voxel.world.IWorld;

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
    public int generate(Vec3i startPosition, Block[][][] blocks) {
        int highestPoint = world.getSeaLevel();
        for (int x = 0; x < blocks.length; x++) {
            for (int z = 0; z < blocks.length; z++) {
                int distFromSeaLevel = getHeightAboveSeaLevel(startPosition.x + x, startPosition.z + z);
                int yMax = world.getSeaLevel() + distFromSeaLevel;
                if (yMax < world.getSeaLevel()) {
                    for (int y = yMax; y < world.getSeaLevel(); y++) {
                        Block blockType = Block.WATER;
                        if (y == yMax) {
                            blockType = Block.SAND;
                        }
                        blocks[x][y][z] = blockType;
                    }
                }
                for (int y = 0; y < yMax; y++) {
                    Block blockType = Block.STONE;

                    if (y == world.getSeaLevel() - 1 && y == yMax - 1) {
                        blocks[x][y + 1][z] = Block.SAND;
                    }

                    if (y == yMax - 1) {
                        blockType = Block.GRASS;
                    } else if (y > yMax - 5) {
                        blockType = Block.DIRT;
                    }
                    blocks[x][y][z] = blockType;
                    highestPoint = Math.max(highestPoint, y + 1);
                }
            }
        }

        return highestPoint;
    }

    private int getHeightAboveSeaLevel(int x, int z) {
        return (int) Math.round(100 * this.noise.getNoise(x, z));
    }

}
