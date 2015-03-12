package sx.lambda.mstojcevich.voxel.net.packet.server

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.net.packet.ServerPacket
import sx.lambda.mstojcevich.voxel.util.Vec3i
import sx.lambda.mstojcevich.voxel.world.chunk.Chunk
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk

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
                    VoxelGame.instance.getWorld().addChunk(new Chunk(VoxelGame.instance.getWorld(), new Vec3i(x, y, z), ids))
                } catch(Exception ex) {
                    VoxelGame.instance.handleCriticalException(ex)
                }
            }
        }.start()
    }
}
