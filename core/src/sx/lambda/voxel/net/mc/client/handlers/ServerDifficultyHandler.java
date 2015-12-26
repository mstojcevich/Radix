package sx.lambda.voxel.net.mc.client.handlers;

import org.spacehq.mc.protocol.packet.ingame.server.ServerDifficultyPacket;

/**
 * Created by louxiu on 12/17/15.
 */
public class ServerDifficultyHandler implements PacketHandler<ServerDifficultyPacket> {
    @Override
    public void handle(ServerDifficultyPacket packet) {
//        Gdx.app.debug("", "ServerDifficultyPacket"
//                + ", difficulty=" + packet.getDifficulty()
//                + ", priority=" + packet.isPriority());
    }
}
