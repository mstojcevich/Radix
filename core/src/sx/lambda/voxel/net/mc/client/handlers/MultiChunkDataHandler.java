package sx.lambda.voxel.net.mc.client.handlers;

import org.spacehq.mc.protocol.data.game.Chunk;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerMultiChunkDataPacket;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.api.VoxelGameAPI;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.util.Vec3i;
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
                    IChunk ck = game.getWorld().getChunkAtPosition(cx, cz);
                    if(ck == null) {
                        ck = new sx.lambda.voxel.world.chunk.Chunk(game.getWorld(), new Vec3i(cx, 0, cz), new int[16][256][16]);
                        game.getWorld().addChunk(ck);
                    }
                    int cy = 0;
                    for(Chunk c : packet.getChunks(column)) {
                        if(c == null) {
                            cy += 16;
                            continue;
                        }
                        for(int x = 0; x < 16; x++) {
                            for(int z = 0; z < 16; z++) {
                                for(int y = 0; y < 16; y++) {
                                    int id = c.getBlocks().getBlock(x, y, z);
                                    int ll = c.getBlockLight().get(x, y, z);
                                    boolean blockExists = false;
                                    if(id != 0) {
                                        for (Block b : VoxelGameAPI.instance.getBlocks()) {
                                            if (b.getID() == id) blockExists = true;
                                        }
                                    } else {
                                        blockExists = true;
                                    }
                                    if(!blockExists) {
                                        System.out.println(id + " DOESN'T EXIST");
                                        id = BuiltInBlockIds.UNKNOWN_ID;
                                    }
                                    ck.addBlock(id, x, cy+y, z, false);
                                    ck.setSunlight(x, cy+y, z, ll, false);
                                }
                            }
                        }
                        cy += 16;
                    }
                    ck.finishChangingSunlight();
                }
            }
        }.start();
    }
}
