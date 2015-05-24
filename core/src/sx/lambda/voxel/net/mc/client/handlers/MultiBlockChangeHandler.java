package sx.lambda.voxel.net.mc.client.handlers;

import org.spacehq.mc.protocol.data.game.values.world.block.BlockChangeRecord;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerMultiBlockChangePacket;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.world.chunk.IChunk;

public class MultiBlockChangeHandler implements PacketHandler<ServerMultiBlockChangePacket> {

    private final VoxelGameClient game;

    public MultiBlockChangeHandler(VoxelGameClient game) {
        this.game = game;
    }

    @Override
    public void handle(ServerMultiBlockChangePacket packet) {
        for(BlockChangeRecord r : packet.getRecords()) {
            int x = r.getPosition().getX();
            int y = r.getPosition().getY();
            int z = r.getPosition().getZ();
            IChunk ck = game.getWorld().getChunkAtPosition(x, z);
            if(ck == null)return;
            if(r.getBlock() > 0) {
                ck.addBlock(r.getBlock(), x, y, z);
            } else {
                ck.removeBlock(x, y, z);
            }
        }
    }

}
