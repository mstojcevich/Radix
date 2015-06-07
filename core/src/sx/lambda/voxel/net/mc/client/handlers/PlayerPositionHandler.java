package sx.lambda.voxel.net.mc.client.handlers;

import com.badlogic.gdx.Gdx;
import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import sx.lambda.voxel.VoxelGameClient;

public class PlayerPositionHandler implements PacketHandler<ServerPlayerPositionRotationPacket> {

    private final VoxelGameClient game;

    public PlayerPositionHandler(VoxelGameClient game) {
        this.game = game;
    }

    @Override
    public void handle(ServerPlayerPositionRotationPacket packet) {
        game.getPlayer().getPosition().set((float)packet.getX(), (float)packet.getY(), (float)packet.getZ());
        game.getPlayer().getRotation().setRot(-packet.getPitch(), 180 - packet.getYaw());

        Gdx.graphics.requestRendering();
    }
}
