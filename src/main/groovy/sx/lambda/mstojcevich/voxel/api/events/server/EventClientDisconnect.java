package sx.lambda.mstojcevich.voxel.api.events.server;

import pw.oxcafebabe.marcusant.eventbus.Event;
import sx.lambda.mstojcevich.voxel.server.net.ConnectedClient;

/**
 * Event fired whenever a client disconnects from the server
 */
public class EventClientDisconnect implements Event {

    private final ConnectedClient client;
    private final String reason;

    public EventClientDisconnect(ConnectedClient client, String reason) {
        this.client = client;
        this.reason = reason;
    }

    public ConnectedClient getClient() {
        return this.client;
    }

    public String getReason() {
        return this.reason;
    }

}
