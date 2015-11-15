package sx.lambda.voxel.net.mc.client.handlers;

import com.badlogic.gdx.Gdx;
import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import sx.lambda.voxel.RadixClient;

public class PlayerSpawnHandler implements PacketHandler<ServerSpawnPlayerPacket> {

    private final RadixClient game;

    public PlayerSpawnHandler(RadixClient game) {
        this.game = game;
    }

    @Override
    public void handle(ServerSpawnPlayerPacket packet) {
        int playerId = RadixClient.getInstance().getPlayer().getID();
        int entityId = packet.getEntityId();
        Gdx.app.debug("", "ServerSpawnPlayerPacket"
                        + ", positionX=" + packet.getX()
                        + ", positionY=" + packet.getY()
                        + ", positionZ=" + packet.getZ()
                        + ", entityId=" + entityId
                        + ", playerId=" + playerId);

        // TODO if it's not for us, set position of the entity it is for
        if(entityId != playerId) {
            return;
        }

        game.getPlayer().getPosition().set((float) packet.getX(), (float) packet.getY(), (float) packet.getZ());
        game.getPlayer().getRotation().setRot(-packet.getPitch(), 180 - packet.getYaw());

        // TODO set current item

        game.getGameRenderer().calculateFrustum();
        Gdx.graphics.requestRendering();
    }

}
