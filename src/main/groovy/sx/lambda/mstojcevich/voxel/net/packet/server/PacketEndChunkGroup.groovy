package sx.lambda.mstojcevich.voxel.net.packet.server

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.net.packet.ServerPacket

@CompileStatic
class PacketEndChunkGroup implements ServerPacket {

    @Override
    void handleClientReceive(ChannelHandlerContext ctx) {
        VoxelGame.instance.getWorld().gcChunks(VoxelGame.instance.getPlayer().position, VoxelGame.instance.settingsManager.visualSettings.viewDistance)
    }

}
