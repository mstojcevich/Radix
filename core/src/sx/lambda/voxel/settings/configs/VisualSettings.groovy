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
    private boolean fancyTrees = true
    /**
     * Whether to average light values on a per-corner basis
     */
    private boolean perCornerLight = true;
    /**
     * Whether to only update the screen when something has changed
     *
     * Saves battery on laptops and mobile phones
     */
    private boolean nonContinuous = false;
    /**
     * Whether to distribute chunk rerenders by only uploading one to the gpu per frame.
     * This is for the GL stuff, since the non-gl stuff can be and is done on another thread.
     *
     * This should not be enabled on machines that don't see lag spikes when loading chunks,
     *      since it will make chunks take longer to load in at lower fps.
     */
    private boolean smoothChunkLoad = false;

    public int getViewDistance() { viewDistance }

    public boolean isFancyTreesEnabled() { fancyTrees }

    public boolean perCornerLightEnabled() { perCornerLight }

    public boolean nonContinuous() { nonContinuous }

    public boolean smoothChunkload() { smoothChunkLoad }

}
