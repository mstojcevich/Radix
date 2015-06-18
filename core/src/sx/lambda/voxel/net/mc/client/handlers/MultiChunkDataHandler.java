package sx.lambda.voxel.net.mc.client.handlers;

import com.badlogic.gdx.Gdx;
import org.spacehq.mc.protocol.data.game.Chunk;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerMultiChunkDataPacket;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.api.RadixAPI;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.biome.Biome;
import sx.lambda.voxel.world.chunk.FlatBlockStorage;

public class MultiChunkDataHandler implements PacketHandler<ServerMultiChunkDataPacket> {

    private final RadixClient game;

    public MultiChunkDataHandler(RadixClient game) {
        this.game = game;
    }

    @Override
    public void handle(final ServerMultiChunkDataPacket packet) {
        for(int column = 0; column < packet.getColumns(); column++) {
            int cx = packet.getX(column)*16;
            int cz = packet.getZ(column)*16;
            int biomeID = packet.getBiomeData(column)[0];

            Biome biome = RadixAPI.instance.getBiomeByID(biomeID);
            if(biome == null)
                biome = RadixAPI.instance.getBiomeByID(biomeID-128);
            if(biome == null)
                biome = RadixAPI.instance.getBiomeByID(0);

            sx.lambda.voxel.world.chunk.Chunk ck = (sx.lambda.voxel.world.chunk.Chunk)game.getWorld().getChunk(cx, cz);
            boolean hadChunk = ck != null;
            if(!hadChunk) {
                ck = new sx.lambda.voxel.world.chunk.Chunk(game.getWorld(), new Vec3i(cx, 0, cz), biome, false);
            }
            FlatBlockStorage[] blockStorages = ck.getBlockStorage();

            int yIndex = 0;
            int highestPoint = 0;
            for(Chunk c : packet.getChunks(column)) {
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
        }

        Gdx.graphics.requestRendering();
    }
}
