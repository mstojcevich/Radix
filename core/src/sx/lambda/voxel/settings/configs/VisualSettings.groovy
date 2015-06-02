package sx.lambda.voxel.settings.configs

import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient

@CompileStatic
class VisualSettings implements Serializable {

    /**
     * Distance, in chunks, to load the world
     * Defaults to 4
     */
    private int viewDistance = 4
    /**
     * Whether to draw internal leaves of trees
     */
    private boolean fancyTrees = false

    public int getViewDistance() { viewDistance }

    public boolean isFancyTreesEnabled() { fancyTrees }

}
