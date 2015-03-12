package sx.lambda.mstojcevich.voxel.net.packet.client

import io.netty.channel.ChannelHandlerContext
import sx.lambda.mstojcevich.voxel.net.packet.ClientPacket
import sx.lambda.mstojcevich.voxel.server.VoxelGameServer
import sx.lambda.mstojcevich.voxel.server.net.ConnectedClient
import sx.lambda.mstojcevich.voxel.util.Vec3i
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk

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
