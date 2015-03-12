package sx.lambda.mstojcevich.voxel.client.render.meshing;

import sx.lambda.mstojcevich.voxel.block.Block;

/**
 * Turns an array of voxels into OpenGL vertices
 */
public interface Mesher {

    /**
     * Meshes the specified voxels.
     * @param voxels Voxels to mesh
     * @param lightLevels Light levels of voxels
     * @return Result of meshing the voxels.
     */
    MeshResult meshVoxels(Block[][][] voxels, float[][][] lightLevels);

    /**
     * Enables the alpha channel for color
     */
    void enableAlpha();

    /**
     * Disables theslpha channel for color
     */
    void disableAlpha();

}
