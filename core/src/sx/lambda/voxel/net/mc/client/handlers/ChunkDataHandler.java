package sx.lambda.voxel.net.mc.client.handlers;

import org.spacehq.mc.protocol.data.game.Chunk;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.api.VoxelGameAPI;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.chunk.IChunk;

public class ChunkDataHandler implements PacketHandler<ServerChunkDataPacket> {

    private final VoxelGameClient game;

    public ChunkDataHandler(VoxelGameClient game) {
        this.game = game;
    }

    @Override
    public void handle(ServerChunkDataPacket scdp) {
        if(scdp.getChunks().length == 0) {
            VoxelGameClient.getInstance().getWorld().rmChunk(VoxelGameClient.getInstance().getWorld().getChunkAtPosition(scdp.getX(), scdp.getZ()));
        }

        int cx = scdp.getX()*16;
        int cz = scdp.getZ()*16;
        IChunk ck = game.getWorld().getChunkAtPosition(cx, cz);
        if(ck == null) {
            ck = new sx.lambda.voxel.world.chunk.Chunk(game.getWorld(), new Vec3i(cx, 0, cz), new int[16][256][16]);
            game.getWorld().addChunk(ck);
        }
        int cy = 0;
        for (Chunk c : scdp.getChunks()) {
            if (c == null) {
                cy += 16;
                continue;
            }
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 16; y++) {
                        int id = c.getBlocks().getBlock(x, y, z);
                        boolean blockExists = false;
                        if(id != 0) {
                            for (Block b : VoxelGameAPI.instance.getBlocks()) {
                                if (b.getID() == id) blockExists = true;
                            }
                        } else {
                            blockExists = true;
                        }
                        if(!blockExists)id = BuiltInBlockIds.UNKNOWN_ID;
                        ck.addBlock(id, x, cy+y, z, false);
                    }
                }
            }
            cy += 16;
        }
    }

}
