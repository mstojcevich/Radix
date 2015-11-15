package sx.lambda.voxel.net.mc.client.handlers;

import com.badlogic.gdx.Gdx;
import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import sx.lambda.voxel.RadixClient;

public class PlayerPositionHandler implements PacketHandler<ServerPlayerPositionRotationPacket> {

    private final RadixClient game;

    public PlayerPositionHandler(RadixClient game) {
        this.game = game;
    }

    @Override
    public void handle(ServerPlayerPositionRotationPacket packet) {
        Gdx.app.debug("", "ServerPlayerPositionRotationPacket"
                        + ", positionX=" + packet.getX()
                        + ", positionY=" + packet.getY()
                        + ", positionZ=" + packet.getZ());

        game.getPlayer().getPosition().set((float) packet.getX(), (float)packet.getY(), (float)packet.getZ());
        game.getPlayer().getRotation().setRot(-packet.getPitch(), 180 - packet.getYaw());

        game.getGameRenderer().calculateFrustum();
        Gdx.graphics.requestRendering();
    }
}
