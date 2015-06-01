package sx.lambda.voxel.net.mc.client.handlers;

import org.spacehq.mc.protocol.packet.ingame.server.world.ServerBlockChangePacket;
import sx.lambda.voxel.VoxelGameClient;

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
        int block = packet.getRecord().getBlock();
        int id = block >> 4;
        int meta = block & 15;
        if(id > 0) {
            game.getWorld().addBlock(id, x, y, z);
            game.getWorld().getChunkAtPosition(x, z).setMeta((short) meta, x, y, z);
        } else {
            game.getWorld().removeBlock(x, y, z);
            game.getWorld().getChunkAtPosition(x, z).setMeta((short) 0, x, y, z);
        }
    }
}
