package sx.lambda.voxel.net.packet.client

import io.netty.channel.ChannelHandlerContext
import sx.lambda.voxel.net.packet.ClientPacket
import sx.lambda.voxel.server.VoxelGameServer
import sx.lambda.voxel.util.Vec3i
import sx.lambda.voxel.world.chunk.IChunk
import sx.lambda.voxel.server.net.ConnectedClient

class PacketUnloadChunk implements ClientPacket {

    private final Vec3i chunkPosition

    public PacketUnloadChunk(Vec3i chunkPosition) {
        this.chunkPosition = chunkPosition
    }

    @Override
    void handleServerReceive(VoxelGameServer server, ChannelHandlerContext ctx) {
        ConnectedClient cc = server.getClient(ctx)
        for(IChunk c : cc.hadChunks) {
            if(c.startPosition.equals(chunkPosition)) {
                cc.hadChunks.remove(c)
            }
        }
    }

}
