package sx.lambda.voxel.net.mc.client.handlers;

import com.badlogic.gdx.Gdx;
import org.spacehq.mc.protocol.data.game.Chunk;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.api.RadixAPI;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.biome.Biome;
import sx.lambda.voxel.world.chunk.FlatBlockStorage;

public class ChunkDataHandler implements PacketHandler<ServerChunkDataPacket> {

    private final RadixClient game;

    public ChunkDataHandler(RadixClient game) {
        this.game = game;
    }

    @Override
    public void handle(ServerChunkDataPacket scdp) {
        if(scdp.getChunks().length == 0) {
            RadixClient.getInstance().getWorld().rmChunk(RadixClient.getInstance().getWorld().getChunk(scdp.getX(), scdp.getZ()));
        }

        int biomeID = 0;
        if(scdp.getBiomeData() != null) {
            biomeID = scdp.getBiomeData()[0];
        }
        Biome biome = RadixAPI.instance.getBiomeByID(biomeID);
        if(biome == null)
            biome = RadixAPI.instance.getBiomeByID(biomeID-128);
        if(biome == null)
            biome = RadixAPI.instance.getBiomeByID(0);

        int cx = scdp.getX()*16;
        int cz = scdp.getZ()*16;
        sx.lambda.voxel.world.chunk.Chunk ck = (sx.lambda.voxel.world.chunk.Chunk)game.getWorld().getChunk(cx, cz);
        boolean hadChunk = ck != null;
        if(!hadChunk) {
            ck = new sx.lambda.voxel.world.chunk.Chunk(game.getWorld(), new Vec3i(cx, 0, cz), biome, false);
        }
        FlatBlockStorage[] blockStorages = ck.getBlockStorage();

        int yIndex = 0;
        int highestPoint = 0;
        for(Chunk c : scdp.getChunks()) {
            if(c == null) {
                yIndex++;
                continue;
            }

            FlatBlockStorage storage = blockStorages[yIndex];
            if(storage == null) {
                storage = blockStorages[yIndex] = new FlatBlockStorage(16, 16, 16, false);
            }

            short[] blockData = c.getBlocks().getData();
            for(int i = 0; i < blockData.length; i++) {
                int data = blockData[i];
                if(data == 0)
                    continue;
                int id = data >> 4;
                boolean exists = RadixAPI.instance.getBlock(id) != null;
                if(!exists) {
                    blockData[i] = BuiltInBlockIds.UNKNOWN_ID << 4;
                }
            }

            storage.setBlocks(blockData);
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
