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
        int chunkRelativeX = x & (game.getWorld().getChunkSize()-1);
        int chunkRelativeZ = z & (game.getWorld().getChunkSize()-1);
        int block = packet.getRecord().getBlock();
        int id = block >> 4;
        int meta = block & 15;
        IChunk chunk = game.getWorld().getChunk(x, z);
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
