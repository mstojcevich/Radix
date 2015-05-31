package sx.lambda.voxel.net.mc.client.handlers;

import org.spacehq.mc.protocol.packet.ingame.server.world.ServerBlockChangePacket;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.world.chunk.IChunk;

public class BlockChangeHandler implements PacketHandler<ServerBlockChangePacket> {

    private final VoxelGameClient game;

    public BlockChangeHandler(VoxelGameClient game) {
        this.game = game;
    }

    @Override
    public void handle(ServerBlockChangePacket packet) {
        int x = packet.getRecord().getPosition().getX();
        int y = packet.getRecord().getPosition().getY();
        int z = packet.getRecord().getPosition().getZ();
        if(packet.getRecord().getBlock() > 0) {
            game.getWorld().addBlock(packet.getRecord().getBlock(), x, y, z);
        } else {
            game.getWorld().removeBlock(x, y, z);
        }
    }
}
