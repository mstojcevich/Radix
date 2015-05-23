package sx.lambda.voxel.client.gui.transition;

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
        return getTimeSinceStart() >= length;
    }

    public long getTimeSinceStart() {
        return System.currentTimeMillis() - startTimeMS;
    }

    public int getLength() {
        return length;
    }

}
