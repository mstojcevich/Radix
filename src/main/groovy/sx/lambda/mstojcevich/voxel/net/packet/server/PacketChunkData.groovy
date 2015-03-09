package sx.lambda.mstojcevich.voxel.net.packet.server

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.net.packet.ServerPacket
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk

@CompileStatic
class PacketChunkData implements ServerPacket {

    private final IChunk chunk

    public PacketChunkData(IChunk chunk) {
        this.chunk = chunk
    }

    @Override
    void handleClientReceive(ChannelHandlerContext ctx) {
        new Thread("Chunk Add") {
            @Override
            public void run() {
                try {
                    VoxelGame.instance.getWorld().addChunk(chunk)
                } catch(Exception ex) {
                    VoxelGame.instance.handleCriticalException(ex)
                }
            }
        }.start()
    }
}
