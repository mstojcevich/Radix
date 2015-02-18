package sx.lambda.mstojcevich.voxel.net.packet

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.mstojcevich.voxel.server.VoxelGameServer

/**
 * A packet that is sent by the client and received by the server
 */
@CompileStatic
interface ClientPacket extends Serializable {

    void handleServerReceive(VoxelGameServer server, ChannelHandlerContext ctx)

}