package sx.lambda.voxel.client.render.meshing;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.world.chunk.BlockStorage;

/**
 * Turns an array of voxels into OpenGL vertices
 */
public interface Mesher {

    /**
     * Meshes the specified voxels.
     *
     * @param builder     MeshBuilder to build the mesh onto
     * @param storage     Blocks to mesh
     */
    Mesh meshVoxels(MeshBuilder builder, UseCondition condition);

    interface UseCondition {
        /**
         * @param block Block to check
         * @return True if the block should be used in this mesh
         */
        boolean shouldUse(Block block);
    }

}
