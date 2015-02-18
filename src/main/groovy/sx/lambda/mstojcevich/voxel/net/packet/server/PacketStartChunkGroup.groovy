package sx.lambda.mstojcevich.voxel.net.packet.server

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.mstojcevich.voxel.net.packet.ServerPacket

@CompileStatic
class PacketStartChunkGroup implements ServerPacket {
    @Override
    void handleClientReceive(ChannelHandlerContext ctx) {
    }
}
