package sx.lambda.voxel.api.events.render;

import pw.oxcafebabe.marcusant.eventbus.Event;
import sx.lambda.voxel.entity.Entity;

/**
 * Event fired after an entity is rendered
 */
public class EventEntityRender implements Event {

    private final Entity entity;


    public EventEntityRender(Entity e) {
        this.entity = e;
    }

    public Entity getEntity() {
        return this.entity;
    }

}
