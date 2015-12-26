package sx.lambda.voxel.net.mc.client.handlers;

import org.spacehq.mc.protocol.packet.ingame.server.world.ServerUpdateTimePacket;

/**
 * Created by louxiu on 12/19/15.
 */
public class UpdateTimeHandler implements PacketHandler<ServerUpdateTimePacket> {
    @Override
    public void handle(ServerUpdateTimePacket packet) {
//        TODO: support day night cycle
//        Gdx.app.debug("", "ServerUpdateTimePacket"
//                + ", time=" + packet.getTime()
//                + ", worldAge=" + packet.getWorldAge()
//                + ", priority=" + packet.isPriority());
    }
}
