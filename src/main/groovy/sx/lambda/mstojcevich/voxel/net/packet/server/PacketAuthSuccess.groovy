package sx.lambda.mstojcevich.voxel.net.packet.server

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.mstojcevich.voxel.net.packet.ServerPacket

/**
 * Packet sent to signify a successful auth and a stage change to play
 */
@CompileStatic
class PacketAuthSuccess implements ServerPacket {

    @Override
    void handleClientReceive(ChannelHandlerContext ctx) {
        println "Auth successful"
    }

}
