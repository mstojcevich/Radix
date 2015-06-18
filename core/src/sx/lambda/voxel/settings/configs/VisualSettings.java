package sx.lambda.voxel.settings.configs;

import java.io.Serializable;

public class VisualSettings implements Serializable {

    /**
     * Distance, in chunks, to load the world
     * Defaults to 4
     */
    private int viewDistance = 4;
    /**
     * Whether to draw internal leaves of trees
     */
    private boolean fancyTrees = false;
    /**
     * Whether to average light values on a per-corner basis
     */
    private boolean perCornerLight = false;
    /**
     * Whether to only update the screen when something has changed
     * <p>
     * Saves battery on laptops and mobile phones
     */
    private boolean nonContinuous = true;
    /**
     * Whether to distribute chunk rerenders by only uploading one to the gpu per frame.
     * This is for the GL stuff, since the non-gl stuff can be and is done on another thread.
     * <p>
     * This should not be enabled on machines that don't see lag spikes when loading chunks,
     * since it will make chunks take longer to load in at lower fps.
     */
    private boolean smoothChunkLoad = true;
    /**
     * Whether to call glFinish() at the end of each frame
     * <p>
     * Some GPUs will wait up to 3 frames before actually rendering. The delay allows the CPU to get ahead while reducing gpu overhead.
     * By finishing each frame, this caching mechanism is avoided. This will take load off of the cpu while putting more on the GPU.
     * Because this game is not GPU intensive to begin with, this may improve performance or smooth out fps.
     * <p>
     * TL;DR this makes sure everything is actually drawn before going to the next frame
     */
    private boolean finishEachFrame = true;

    public int getViewDistance() {
        return viewDistance;
    }

    public boolean isFancyTreesEnabled() {
        return fancyTrees;
    }

    public boolean perCornerLightEnabled() {
        return perCornerLight;
    }

    public boolean nonContinuous() {
        return nonContinuous;
    }

    public boolean smoothChunkload() {
        return smoothChunkLoad;
    }

    public boolean finishEachFrame() {
        return finishEachFrame;
    }

}
