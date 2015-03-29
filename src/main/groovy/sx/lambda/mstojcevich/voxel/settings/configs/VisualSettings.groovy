package sx.lambda.mstojcevich.voxel.settings.configs

import groovy.transform.CompileStatic
import sx.lambda.mstojcevich.voxel.VoxelGame

@CompileStatic
class VisualSettings implements Serializable {

    /**
     * Distance, in chunks, to load the world
     * Defaults to 2
     */
    private int viewDistance = 5
    /**
     * Whether to run the game in fullscreen
     */
    private boolean fullscreen = false
    private int maxFPS = VoxelGame.DEBUG ? 10 : 0 //Save my battery life pls
    private int windowWidth = 640, windowHeight = 480
    private boolean postProcess = false
    private boolean peasantMode = false

    public int getViewDistance() { viewDistance }

    public int getMaxFPS() { maxFPS }

    public boolean isFullscreen() { fullscreen }

    public int getWindowWidth() { windowWidth }
    public int getWindowHeight() { windowHeight }

    public boolean isPostProcessEnabled() { return postProcess }
    public boolean isPeasantModeEnabled() { peasantMode }

}
