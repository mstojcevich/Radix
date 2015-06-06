package sx.lambda.voxel.net.mc.client.handlers;

import org.spacehq.mc.protocol.data.game.Chunk;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerMultiChunkDataPacket;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.api.VoxelGameAPI;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.biome.Biome;
import sx.lambda.voxel.world.chunk.BlockStorage.CoordinatesOutOfBoundsException;
import sx.lambda.voxel.world.chunk.IChunk;

public class MultiChunkDataHandler implements PacketHandler<ServerMultiChunkDataPacket> {

    private final VoxelGameClient game;

    public MultiChunkDataHandler(VoxelGameClient game) {
        this.game = game;
    }

    @Override
    public void handle(final ServerMultiChunkDataPacket packet) {
        new Thread() {
            @Override
            public void run() {
                for(int column = 0; column < packet.getColumns(); column++) {
                    int cx = packet.getX(column)*16;
                    int cz = packet.getZ(column)*16;
                    int biomeID = packet.getBiomeData(column)[0];
                    Biome biome = VoxelGameAPI.instance.getBiomeByID(biomeID);
                    if(biome == null)
                        biome = VoxelGameAPI.instance.getBiomeByID(biomeID-128);
                    if(biome == null)
                        biome = VoxelGameAPI.instance.getBiomeByID(0);
                    IChunk ck = game.getWorld().getChunk(cx, cz);
                    if(ck != null) {
                        game.getWorld().rmChunk(ck);
                    }
                    ck = new sx.lambda.voxel.world.chunk.Chunk(game.getWorld(), new Vec3i(cx, 0, cz), biome, false);
                    int cy = 0;
                    for(Chunk c : packet.getChunks(column)) {
                        if(c == null) {
                            cy += 16;
                            continue;
                        }
                        for(int x = 0; x < 16; x++) {
                            for(int z = 0; z < 16; z++) {
                                for(int y = 0; y < 16; y++) {
                                    try {
                                        int id = c.getBlocks().getBlock(x, y, z);
                                        short meta = (short) c.getBlocks().getData(x, y, z);
                                        byte blocklight = (byte) c.getBlockLight().get(x, y, z);
                                        byte skylight = (byte) c.getSkyLight().get(x, y, z);
                                        boolean blockExists = false;
                                        if (id <= 0 || VoxelGameAPI.instance.getBlockByID(id) != null) {
                                            blockExists = true;
                                        }
                                        if (!blockExists) {
                                            id = BuiltInBlockIds.UNKNOWN_ID;
                                        }
                                        if (id > 0)
                                            ck.setBlock(id, x, cy + y, z, false);
                                        if (meta > 0)
                                            ck.setMeta(meta, x, cy + y, z);
                                        ck.setBlocklight(x, cy+y, z, blocklight);
                                        ck.setSunlight(x, cy+y, z, skylight);
                                        ck.finishAddingSun();
                                    } catch (CoordinatesOutOfBoundsException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }
                        cy += 16;
                    }
                    game.getWorld().addChunk(ck);
                }
            }
        }.start();
    }
}
