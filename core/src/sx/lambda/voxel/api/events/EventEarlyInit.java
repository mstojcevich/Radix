package sx.lambda.voxel.api.events;

import pw.oxcafebabe.marcusant.eventbus.Event;

/**
 * Fired when the game first starts, and after all registration events.
 * This is fired before a GL context.
 *
 * Do not use FIRST priority, as that is used internally to set things up.
 */
public class EventEarlyInit implements Event {
}
