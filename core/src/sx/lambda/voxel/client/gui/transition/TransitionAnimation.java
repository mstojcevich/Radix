package sx.lambda.voxel.client.gui.transition;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface TransitionAnimation {

    /**
     * Initialize the animation in an OpenGL context
     */
    void init();

    /**
     * Render the animation on the screen
     */
    void render(SpriteBatch guiBatch);

    /**
     * Clean up transition animation resources
     */
    void finish();

    /**
     * Check if the animation is finished
     *
     * @return Whether the animation has finished
     */
    boolean isFinished();

}
