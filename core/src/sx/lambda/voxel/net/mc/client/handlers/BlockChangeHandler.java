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
        IChunk ck = game.getWorld().getChunkAtPosition(x, z);
        if(ck == null)return;
        if(packet.getRecord().getBlock() > 0) {
            ck.addBlock(packet.getRecord().getBlock(), x, y, z);
        } else {
            ck.removeBlock(x, y, z);
        }
    }
}
