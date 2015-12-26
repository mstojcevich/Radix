package sx.lambda.voxel.net.mc.client.handlers.player;

import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerAbilitiesPacket;
import sx.lambda.voxel.net.mc.client.handlers.PacketHandler;

/**
 * Created by louxiu on 12/17/15.
 */
public class PlayerAbilitiesHandler implements PacketHandler<ServerPlayerAbilitiesPacket>{
    @Override
    public void handle(ServerPlayerAbilitiesPacket packet) {
    }
}
