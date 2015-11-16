package sx.lambda.voxel.net.mc.client.handlers;

import com.badlogic.gdx.Gdx;
import org.spacehq.mc.protocol.data.game.values.entity.player.GameMode;
import org.spacehq.mc.protocol.data.game.values.setting.ChatVisibility;
import org.spacehq.mc.protocol.data.game.values.setting.SkinPart;
import org.spacehq.mc.protocol.packet.ingame.client.ClientSettingsPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import sx.lambda.voxel.RadixClient;

public class JoinGameHandler implements PacketHandler<ServerJoinGamePacket> {
    @Override
    public void handle(ServerJoinGamePacket packet) {
        GameMode mode = packet.getGameMode();
        int entityId = packet.getEntityId();
        Gdx.app.debug("", "ServerJoinGamePacket, mode=" + mode + ", entityId=" + entityId);

        RadixClient.getInstance().getPlayer().setGameMode(mode);
        RadixClient.getInstance().getPlayer().setID(entityId);
        RadixClient.getInstance().getMinecraftConn().getClient().getSession()
                .send(new ClientSettingsPacket("en_US", 1, ChatVisibility.FULL, false, SkinPart.HAT));
    }
}
