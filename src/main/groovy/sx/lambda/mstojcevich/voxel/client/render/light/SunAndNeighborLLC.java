package sx.lambda.mstojcevich.voxel.client.render.light;

import sx.lambda.mstojcevich.voxel.VoxelGame;
import sx.lambda.mstojcevich.voxel.block.Block;
import sx.lambda.mstojcevich.voxel.util.Vec3i;
import sx.lambda.mstojcevich.voxel.world.IWorld;
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk;

/**
 * A light level calculator that uses the sun and neighbors to calculate light.
 */
public class SunAndNeighborLLC implements LightLevelCalculator {

    private final IChunk chunk;

    public SunAndNeighborLLC(IChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public void calculateLightLevels(Block[][][] voxels, float[][][] lightLevels) {
        IWorld world = VoxelGame.getInstance().getWorld();

        int size = voxels.length;
        int height = voxels[0].length;

        // First pass, sunlight
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                float lightLevel = 1.0f;
                boolean firstBlock = false;
                for (int y = height - 1; y >= 0; y--) {
                    Block block = voxels[x][y][z];
                    if (block != null) {
                        if (!firstBlock) {
                            firstBlock = true;
                        }
                        lightLevels[x][y][z] = lightLevel;
                    } else if (!firstBlock) {
                        lightLevels[x][y][z] = 1.0f;
                        lightLevel = 1.0f;
                    } else {
                        lightLevels[x][y][z] = lightLevel;
                    }
                    lightLevel *= 0.8f;
                }
            }
        }

        // Second pass, neighbors
        for(int i = 0; i < 2; i++) {
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    for (int y = height - 1; y >= 0; y--) {
                        if (voxels[x][y][z] == null) {
                            float current = lightLevels[x][y][z];

                            float brightestNeighbor = current;

                            if (x > 0) {
                                if (voxels[x - 1][y][z] == null) {
                                    brightestNeighbor = Math.max(brightestNeighbor, lightLevels[x - 1][y][z]);
                                }
                            } else {
                                int askx = chunk.getStartPosition().x - 1 - x;
                                int asky = chunk.getStartPosition().y;
                                int askz = chunk.getStartPosition().z;
                                Vec3i askWorld = new Vec3i(askx, asky, askz);
                                IChunk chunk = world.getChunkAtPosition(askWorld);
                                if (chunk != null) {
                                    brightestNeighbor = Math.max(brightestNeighbor, chunk.getLightLevel(size - 1 - x, y, z));
                                }
                            }

                            if (x < size - 1) {
                                if (voxels[x + 1][y][z] == null) {
                                    brightestNeighbor = Math.max(brightestNeighbor, lightLevels[x + 1][y][z]);
                                }
                            } else {
                                int askx = chunk.getStartPosition().x + size;
                                int asky = chunk.getStartPosition().y;
                                int askz = chunk.getStartPosition().z;
                                Vec3i askWorld = new Vec3i(askx, asky, askz);
                                IChunk chunk = world.getChunkAtPosition(askWorld);
                                if (chunk != null) {
                                    brightestNeighbor = Math.max(brightestNeighbor, chunk.getLightLevel(x - (size - 1), y, z));
                                }
                            }

                            if (z > 0) {
                                if (voxels[x][y][z - 1] == null) {
                                    brightestNeighbor = Math.max(brightestNeighbor, lightLevels[x][y][z - 1]);
                                }
                            } else {
                                int askx = chunk.getStartPosition().x;
                                int asky = chunk.getStartPosition().y;
                                int askz = chunk.getStartPosition().z - 1 - z;
                                Vec3i askWorld = new Vec3i(askx, asky, askz);
                                IChunk chunk = world.getChunkAtPosition(askWorld);
                                if (chunk != null) {
                                    brightestNeighbor = Math.max(brightestNeighbor, chunk.getLightLevel(x, y, size - 1 - z));
                                }
                            }

                            if (z < size - 1) {
                                if (voxels[x][y][z + 1] == null) {
                                    brightestNeighbor = Math.max(brightestNeighbor, lightLevels[x][y][z + 1]);
                                }
                            } else {
                                int askx = chunk.getStartPosition().x;
                                int asky = chunk.getStartPosition().y;
                                int askz = chunk.getStartPosition().z + size;
                                Vec3i askWorld = new Vec3i(askx, asky, askz);
                                IChunk chunk = world.getChunkAtPosition(askWorld);
                                if (chunk != null) {
                                    brightestNeighbor = Math.max(brightestNeighbor, chunk.getLightLevel(x, y, z - (size - 1)));
                                }
                            }

                            float targetBrightness = brightestNeighbor * 0.8f;
                            if (targetBrightness > current) {
                                lightLevels[x][y][z] = targetBrightness;
                            }
                        }
                    }
                }
            }
        }
    }

}
