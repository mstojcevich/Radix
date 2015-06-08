package sx.lambda.voxel.net.mc.client.handlers;

import com.badlogic.gdx.Gdx;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerBlockChangePacket;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.api.RadixAPI;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.world.chunk.BlockStorage.CoordinatesOutOfBoundsException;
import sx.lambda.voxel.world.chunk.IChunk;

public class BlockChangeHandler implements PacketHandler<ServerBlockChangePacket> {

    private final RadixClient game;

    public BlockChangeHandler(RadixClient game) {
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
                boolean blockExists = false;
                for(Block b : RadixAPI.instance.getBlocks()) {
                    if(b.getID() == id) {
                        blockExists = true;
                        break;
                    }
                }
                try {
                    chunk.setBlock(blockExists ? id : BuiltInBlockIds.UNKNOWN_ID, chunkRelativeX, y, chunkRelativeZ);
                    chunk.setMeta((short) (blockExists ? meta : 0), chunkRelativeX, y, chunkRelativeZ);
                } catch (CoordinatesOutOfBoundsException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    chunk.removeBlock(chunkRelativeX, y, chunkRelativeZ);
                    chunk.setMeta((short) 0, chunkRelativeX, y, chunkRelativeZ);
                } catch (CoordinatesOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }

        Gdx.graphics.requestRendering();
    }
}
