package sx.lambda.mstojcevich.voxel.client.gui.transition;

public interface TransitionAnimation {

    /**
     * Initialize the animation in an OpenGL context
     */
    void init();

    /**
     * Render the animation on the screen
     */
    void render();

    /**
     * Clean up transition animation resources
     */
    void finish();

    /**
     * Check if the animation is finished
     * @return Whether the animation has finished
     */
    boolean isFinished();

}
