package sx.lambda.mstojcevich.voxel.api.events.server;

import pw.oxcafebabe.marcusant.eventbus.Event;
import sx.lambda.mstojcevich.voxel.server.net.ConnectedClient;

/**
 * Event fired whenever a client joins the server
 */
public class EventClientJoin implements Event {

    private final ConnectedClient client;

    public EventClientJoin(ConnectedClient client) {
        this.client = client;
    }

    public ConnectedClient getClient() {
        return this.client;
    }

}
