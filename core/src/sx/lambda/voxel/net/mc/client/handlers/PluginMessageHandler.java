package sx.lambda.voxel.net.mc.client.handlers;

import org.spacehq.mc.protocol.packet.ingame.server.ServerPluginMessagePacket;

/**
 * Created by louxiu on 12/17/15.
 */
public class PluginMessageHandler implements PacketHandler<ServerPluginMessagePacket> {
    @Override
    public void handle(ServerPluginMessagePacket packet) {
//        Gdx.app.debug("", "ServerPluginMessagePacket"
//                + ", channel=" + packet.getChannel()
//                + ", priority=" + packet.isPriority());
    }
}
