package sx.lambda.voxel.net.mc.client.handlers;

import org.spacehq.mc.protocol.data.game.values.setting.ChatVisibility;
import org.spacehq.mc.protocol.data.game.values.setting.SkinPart;
import org.spacehq.mc.protocol.packet.ingame.client.ClientSettingsPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import sx.lambda.voxel.VoxelGameClient;

public class JoinGameHandler implements PacketHandler<ServerJoinGamePacket> {
    @Override
    public void handle(ServerJoinGamePacket packet) {
        VoxelGameClient.getInstance().getPlayer().setID(packet.getEntityId());
        VoxelGameClient.getInstance().getMinecraftConn().getClient().getSession()
                .send(new ClientSettingsPacket("en_US", 1, ChatVisibility.FULL, false, SkinPart.HAT));
    }
}
