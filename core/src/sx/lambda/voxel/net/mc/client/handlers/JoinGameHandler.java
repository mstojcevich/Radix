package sx.lambda.voxel.net.mc.client.handlers;

import org.spacehq.mc.protocol.data.game.values.entity.player.GameMode;
import org.spacehq.mc.protocol.data.game.values.setting.ChatVisibility;
import org.spacehq.mc.protocol.data.game.values.setting.SkinPart;
import org.spacehq.mc.protocol.packet.ingame.client.ClientSettingsPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import sx.lambda.voxel.RadixClient;

public class JoinGameHandler implements PacketHandler<ServerJoinGamePacket> {
    private final RadixClient game;

    public JoinGameHandler(RadixClient game) {
        this.game = game;
    }

    @Override
    public void handle(ServerJoinGamePacket packet) {
        GameMode mode = packet.getGameMode();
        int entityId = packet.getEntityId();
        game.getPlayer().setGameMode(mode);
        game.getPlayer().setID(entityId);
        game.getWorld().addEntity(entityId, game.getPlayer());
        game.getMinecraftConn().getClient().getSession()
                .send(new ClientSettingsPacket("en_US", 1, ChatVisibility.FULL, false, SkinPart.HAT));
    }
}
