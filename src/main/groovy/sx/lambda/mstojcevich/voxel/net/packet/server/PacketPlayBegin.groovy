package sx.lambda.mstojcevich.voxel.net.packet.server

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.net.packet.ServerPacket
import sx.lambda.mstojcevich.voxel.net.packet.client.PacketClientInfo

@CompileStatic
class PacketPlayBegin implements ServerPacket {
    @Override
    void handleClientReceive(ChannelHandlerContext ctx) {
        PacketClientInfo.ClientInfoBuilder cib = new PacketClientInfo.ClientInfoBuilder()
        PacketClientInfo.ClientInfo info = cib
                .setViewDistance(VoxelGame.instance.settingsManager.visualSettings.viewDistance)
                .build()
        ctx.writeAndFlush(new PacketClientInfo(info))
        VoxelGame.instance.setServerChanCtx(ctx);
    }
}
