package sx.lambda.voxel.net.packet

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext

/**
 * A packet that is sent by the server and received by the client
 */
@CompileStatic
interface ServerPacket extends Serializable {

    void handleClientReceive(ChannelHandlerContext ctx)

}