package sx.lambda.voxel.net.mc.client.handlers;

import com.badlogic.gdx.Gdx;
import org.spacehq.mc.protocol.data.game.entity.player.PositionElement;
import org.spacehq.mc.protocol.packet.ingame.client.world.ClientTeleportConfirmPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import sx.lambda.voxel.RadixClient;

public class PlayerPositionHandler implements PacketHandler<ServerPlayerPositionRotationPacket> {

    private final RadixClient game;

    public PlayerPositionHandler(RadixClient game) {
        this.game = game;
    }

    @Override
    public void handle(ServerPlayerPositionRotationPacket packet) {
        boolean xRelative = false, yRelative = false, zRelative = false, pitchRelative = false, yawRelative = false;
        for(PositionElement pe : packet.getRelativeElements()) {
            if(pe == PositionElement.X) {
                xRelative = true;
            } else if(pe == PositionElement.Y) {
                yRelative = true;
            } else if(pe == PositionElement.Z) {
                zRelative = true;
            } else if(pe == PositionElement.PITCH) {
                pitchRelative = true;
            } else if(pe == PositionElement.YAW) {
                yawRelative = true;
            }
        }

        float x = game.getPlayer().getPosition().getX();
        float y = game.getPlayer().getPosition().getY();
        float z = game.getPlayer().getPosition().getZ();

        if(xRelative) {
            x += packet.getX();
        } else {
            x = (float)packet.getX();
        }

        if(yRelative) {
            y += packet.getY();
        } else {
            y = (float) packet.getY() - 2;
        }

        if(zRelative) {
            z += packet.getZ();
        } else {
            z = (float) packet.getZ();
        }

        float pitch = game.getPlayer().getRotation().getPitch();
        float yaw = game.getPlayer().getRotation().getYaw();

        if(pitchRelative) {
            pitch += packet.getPitch();
        } else {
            pitch = packet.getPitch();
        }

        if(yawRelative) {
            yaw += packet.getYaw();
        } else {
            yaw = packet.getYaw();
        }

        game.getPlayer().getPosition().set(x, y, z);
        game.getPlayer().getRotation().setRot(-pitch, 180 - yaw);

        game.getGameRenderer().calculateFrustum();
        Gdx.graphics.requestRendering();

        game.getMinecraftConn().getClient().getSession()
                .send(new ClientTeleportConfirmPacket(packet.getTeleportId()));
    }
}
