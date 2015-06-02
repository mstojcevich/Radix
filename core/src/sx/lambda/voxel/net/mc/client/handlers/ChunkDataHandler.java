package sx.lambda.voxel.net.mc.client.handlers;

import org.spacehq.mc.protocol.data.game.Chunk;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.api.VoxelGameAPI;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.biome.Biome;
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

        int biomeID = 0;
        if(scdp.getBiomeData() != null) {
            biomeID = scdp.getBiomeData()[0];
        }
        Biome biome = VoxelGameAPI.instance.getBiomeByID(biomeID);
        if(biome == null)
            biome = VoxelGameAPI.instance.getBiomeByID(biomeID-128);
        if(biome == null)
            biome = VoxelGameAPI.instance.getBiomeByID(0);

        int cx = scdp.getX()*16;
        int cz = scdp.getZ()*16;
        IChunk ck = game.getWorld().getChunkAtPosition(cx, cz);
        if(ck != null) {
            game.getWorld().rmChunk(ck);
        }
        ck = new sx.lambda.voxel.world.chunk.Chunk(game.getWorld(), new Vec3i(cx, 0, cz), new int[16][256][16], new short[16][256][16], biome);
        game.getWorld().addChunk(ck);
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
                        short meta = (short)c.getBlocks().getData(x, y, z);
                        boolean blockExists = false;
                        if(id <= 0 || VoxelGameAPI.instance.getBlockByID(id) != null) {
                            blockExists = true;
                        }
                        if(!blockExists)id = BuiltInBlockIds.UNKNOWN_ID;
                        if(id > 0)
                            ck.setBlock(id, x, cy + y, z, false);
                        if(meta > 0)
                            ck.setMeta(meta, x, cy+y, z);
                    }
                }
            }
            cy += 16;
        }
    }

}
