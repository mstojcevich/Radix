package sx.lambda.voxel.net.packet.server

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.voxel.net.packet.ServerPacket

@CompileStatic
/**
 * Packet signifying a forced disconnect by the server
 */
class PacketKick implements ServerPacket {

    private String reason

    PacketKick(String reason) {
        this.reason = reason
    }

    @Override
    void handleClientReceive(ChannelHandlerContext ctx) {
        println "Kicked from server: $reason"
        ctx.disconnect();
    }

}
