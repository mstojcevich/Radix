package sx.lambda.voxel.net.packet.server

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.net.packet.ServerPacket

@CompileStatic
class PacketEndChunkGroup implements ServerPacket {

    @Override
    void handleClientReceive(ChannelHandlerContext ctx) {
        VoxelGameClient.instance.getWorld().gcChunks(
                VoxelGameClient.instance.getPlayer().position,
                VoxelGameClient.instance.settingsManager.visualSettings.viewDistance)
    }

}
