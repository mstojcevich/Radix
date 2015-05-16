package sx.lambda.voxel.api.events;

import pw.oxcafebabe.marcusant.eventbus.Event;
import sx.lambda.voxel.world.IWorld;

/**
 * Event called 20ish times a second
 */
public class EventGameTick implements Event {

    private final IWorld world;

    public EventGameTick(IWorld world) {
        this.world = world;
    }

    public IWorld getWorld() {
        return this.world;
    }

}
