package sx.lambda.voxel.net.mc.client.handlers;

import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import sx.lambda.voxel.VoxelGameClient;

public class PlayerSpawnHandler implements PacketHandler<ServerSpawnPlayerPacket> {

    private final VoxelGameClient game;

    public PlayerSpawnHandler(VoxelGameClient game) {
        this.game = game;
    }

    @Override
    public void handle(ServerSpawnPlayerPacket packet) {
        game.getPlayer().getPosition().set((float)packet.getX(), (float)packet.getY(), (float)packet.getZ());
        game.getPlayer().getRotation().setRot(-packet.getPitch(), 180-packet.getYaw());

        // TODO set current item & entity ID
    }

}
