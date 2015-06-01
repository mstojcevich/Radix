package sx.lambda.voxel.net.packet.server

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.api.VoxelGameAPI
import sx.lambda.voxel.net.packet.ServerPacket
import sx.lambda.voxel.util.Vec3i
import sx.lambda.voxel.world.chunk.Chunk
import sx.lambda.voxel.world.chunk.IChunk

@CompileStatic
class PacketChunkData implements ServerPacket {

    private final int[][][] ids
    private final int x, y, z

    public PacketChunkData(IChunk chunk) {
        this.ids = chunk.blocksToIdInt()
        Vec3i chunkStartPosition = chunk.getStartPosition()
        this.x = chunkStartPosition.x
        this.y = chunkStartPosition.y
        this.z = chunkStartPosition.z
    }

    @Override
    void handleClientReceive(ChannelHandlerContext ctx) {
        new Thread("Chunk Add") {
            @Override
            public void run() {
                try {
                    VoxelGameClient.instance.getWorld().addChunk(new Chunk(VoxelGameClient.instance.getWorld(), new Vec3i(x, y, z), ids, VoxelGameAPI.instance.getBiomeByID(0)))
                } catch (Exception ex) {
                    VoxelGameClient.instance.handleCriticalException(ex)
                }
            }
        }.start()
    }
}
