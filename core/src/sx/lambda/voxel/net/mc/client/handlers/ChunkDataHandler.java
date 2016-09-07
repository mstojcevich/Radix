package sx.lambda.voxel.net.mc.client.handlers;

import com.badlogic.gdx.Gdx;
import org.spacehq.mc.protocol.data.game.chunk.Chunk;
import org.spacehq.mc.protocol.data.game.world.block.BlockState;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.api.RadixAPI;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.biome.Biome;
import sx.lambda.voxel.world.chunk.BlockStorage;
import sx.lambda.voxel.world.chunk.FlatBlockStorage;

public class ChunkDataHandler implements PacketHandler<ServerChunkDataPacket> {

    private final RadixClient game;

    public ChunkDataHandler(RadixClient game) {
        this.game = game;
    }

    @Override
    public void handle(ServerChunkDataPacket scdp) {
        if(scdp.getColumn().getChunks().length == 0) {
            RadixClient.getInstance().getWorld().rmChunk(RadixClient.getInstance().getWorld().getChunk(scdp.getColumn().getX(), scdp.getColumn().getZ()));
        }

        int biomeID = 0;
        if(scdp.getColumn().getBiomeData() != null) {
            biomeID = scdp.getColumn().getBiomeData()[0];
        }
        Biome biome = RadixAPI.instance.getBiomeByID(biomeID);
        if(biome == null)
            biome = RadixAPI.instance.getBiomeByID(biomeID-128);
        if(biome == null)
            biome = RadixAPI.instance.getBiomeByID(0);

        int cx = scdp.getColumn().getX()*16;
        int cz = scdp.getColumn().getZ()*16;
        sx.lambda.voxel.world.chunk.Chunk ck = (sx.lambda.voxel.world.chunk.Chunk)game.getWorld().getChunk(cx, cz);
        boolean hadChunk = ck != null;
        if(!hadChunk) {
            ck = new sx.lambda.voxel.world.chunk.Chunk(game.getWorld(), new Vec3i(cx, 0, cz), biome, false);
        }
        FlatBlockStorage[] blockStorages = ck.getBlockStorage();

        int yIndex = 0;
        int highestPoint = 0;
        for(Chunk c : scdp.getColumn().getChunks()) {
            if(c == null) {
                yIndex++;
                continue;
            }

            FlatBlockStorage storage = blockStorages[yIndex];
            if(storage == null) {
                storage = blockStorages[yIndex] = new FlatBlockStorage(16, 16, 16);
            }

            for(int y = 0; y < 16; y++) {
                for(int z = 0; z < 16; z++) {
                    for(int x = 0; x < 16; x++) {
                        BlockState blk = c.getBlocks().get(x, y, z);
                        int id = blk.getId();

                        if(id == 0)
                            continue;

                        int meta = blk.getData();

                        boolean exists = RadixAPI.instance.getBlock(id) != null;
                        if(!exists) {
                            try {
                                storage.setId(x, y, z, BuiltInBlockIds.UNKNOWN_ID);
                            } catch (BlockStorage.CoordinatesOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                storage.setId(x, y, z, id);
                                storage.setMeta(x, y, z, meta);
                            } catch (BlockStorage.CoordinatesOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            storage.setSunlight(c.getSkyLight());
            storage.setBlocklight(c.getBlockLight());

            yIndex++;
            highestPoint = yIndex*16;
        }
        ck.setHighestPoint(Math.max(ck.getHighestPoint(), highestPoint));
        ck.finishAddingSun();

        if(!hadChunk) {
            game.getWorld().addChunk(ck);
        }

        Gdx.graphics.requestRendering();
    }

}
