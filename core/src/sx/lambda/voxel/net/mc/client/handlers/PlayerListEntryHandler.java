package sx.lambda.voxel.net.mc.client.handlers;

import org.spacehq.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket;

/**
 * Created by louxiu on 12/19/15.
 */
public class PlayerListEntryHandler implements PacketHandler<ServerPlayerListEntryPacket> {
    @Override
    public void handle(ServerPlayerListEntryPacket packet) {
//        PlayerListEntry[] entrys = packet.getEntries();
//        for (PlayerListEntry entry : entrys) {
//            Gdx.app.debug("", "entry"
//                    + ", name=" + entry.getDisplayName()
//                    + ", mode=" + entry.getGameMode()
//                    + ", ping=" + entry.getPing()
//                    + ", profile=" + entry.getProfile());
//        }
    }
}
