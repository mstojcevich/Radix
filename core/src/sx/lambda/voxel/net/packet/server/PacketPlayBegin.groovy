package sx.lambda.voxel.net.packet.server

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.net.packet.ServerPacket
import sx.lambda.voxel.net.packet.client.PacketClientInfo

@CompileStatic
class PacketPlayBegin implements ServerPacket {
    @Override
    void handleClientReceive(ChannelHandlerContext ctx) {
        PacketClientInfo.ClientInfoBuilder cib = new PacketClientInfo.ClientInfoBuilder()
        PacketClientInfo.ClientInfo info = cib
                .setViewDistance(VoxelGameClient.instance.settingsManager.visualSettings.viewDistance)
                .build()
        ctx.writeAndFlush(new PacketClientInfo(info))
    }
}
