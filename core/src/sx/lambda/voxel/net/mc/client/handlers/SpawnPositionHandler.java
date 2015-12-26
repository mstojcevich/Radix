package sx.lambda.voxel.net.mc.client.handlers;

import org.spacehq.mc.protocol.packet.ingame.server.world.ServerSpawnPositionPacket;
import sx.lambda.voxel.RadixClient;

/**
 * Created by louxiu on 12/17/15.
 */
public class SpawnPositionHandler implements PacketHandler<ServerSpawnPositionPacket> {
    private final RadixClient game;

    public SpawnPositionHandler(RadixClient game) {
        this.game = game;
    }


    @Override
    public void handle(ServerSpawnPositionPacket packet) {
//        Gdx.app.debug("", "ServerSpawnPositionPacket"
//                + ", positionX=" + packet.getPosition().getX()
//                + ", positionY=" + packet.getPosition().getY()
//                + ", positionZ=" + packet.getPosition().getZ()
//                + ", priority=" + packet.isPriority());
    }
}
