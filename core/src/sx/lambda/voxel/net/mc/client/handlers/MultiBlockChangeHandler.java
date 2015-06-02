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
            int chunkRelativeX = x & (game.getWorld().getChunkSize()-1);
            int chunkRelativeZ = z & (game.getWorld().getChunkSize()-1);
            int block = r.getBlock();
            int id = block >> 4;
            int meta = block & 15;
            IChunk chunk = game.getWorld().getChunkAtPosition(x, z);
            if(chunk != null) {
                if (id > 0) {
                    chunk.setBlock(id, chunkRelativeX, y, chunkRelativeZ);
                    chunk.setMeta((short) meta, chunkRelativeX, y, chunkRelativeZ);
                } else {
                    chunk.removeBlock(chunkRelativeX, y, chunkRelativeZ);
                    chunk.setMeta((short) 0, chunkRelativeX, y, chunkRelativeZ);
                }
            }
        }
    }

}
