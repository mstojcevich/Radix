package sx.lambda.mstojcevich.voxel.client.render.light;

import sx.lambda.mstojcevich.voxel.block.Block;

/**
 * Calculates the light level for voxels
 */
public interface LightLevelCalculator {

    /**
     * @param voxels 3d array of voxels to process
     * @param lightLevels Empty 3d array to be filled with light levels
     */
    void calculateLightLevels(Block[][][] voxels, float[][][] lightLevels);

}
