package sx.lambda.voxel.net.mc.client.handlers;

import com.badlogic.gdx.Gdx;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerBlockChangePacket;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.api.RadixAPI;
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
        int id = packet.getRecord().getBlock().getId();
        int meta = packet.getRecord().getBlock().getData();

        IChunk chunk = game.getWorld().getChunk(x, z);
        if(chunk != null) {
            if (id > 0) {
                boolean blockExists = RadixAPI.instance.getBlocks()[id] != null;
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
