package sx.lambda.voxel.client.gui.transition;

import com.badlogic.gdx.Gdx;

public abstract class TimedTransitionAnimation implements TransitionAnimation {

    private final int length;
    private long startTimeMS;

    /**
     * @param length Time in milliseconds to run the animation for
     */
    public TimedTransitionAnimation(int length) {
        this.length = length;
    }


    @Override
    public void init() {
        this.startTimeMS = System.currentTimeMillis();
    }

    @Override
    public boolean isFinished() {
        boolean finished = getTimeSinceStart() >= length;
        if(!finished) {
            Gdx.graphics.requestRendering();
        }
        return finished;
    }

    public long getTimeSinceStart() {
        return System.currentTimeMillis() - startTimeMS;
    }

    public int getLength() {
        return length;
    }

}
