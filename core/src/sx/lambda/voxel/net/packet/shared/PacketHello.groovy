package sx.lambda.voxel.net.packet.shared

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.voxel.net.ProtocolInfo
import sx.lambda.voxel.net.packet.SharedPacket
import sx.lambda.voxel.net.packet.client.PacketAuthInfo
import sx.lambda.voxel.net.packet.server.PacketKick
import sx.lambda.voxel.server.VoxelGameServer
import sx.lambda.voxel.server.net.ConnectionStage

@CompileStatic
class PacketHello implements SharedPacket {

    private short version = ProtocolInfo.VERSION
    private String message

    public PacketHello(boolean client) {
        message = "Hi, I'm a " + (client ? "client" : "server")
    }

    @Override
    void handleClientReceive(ChannelHandlerContext ctx) {
        String hostname = ((InetSocketAddress) ctx.channel().remoteAddress()).hostName
        println "Got a hello response from $hostname"
        ctx.writeAndFlush(new PacketAuthInfo("marcusant", 32390234)) //TODO get username and token from somewhere else
    }

    @Override
    void handleServerReceive(VoxelGameServer server, ChannelHandlerContext ctx) {
        String hostname = ((InetSocketAddress) ctx.channel().remoteAddress()).hostName
        println "Hello from $hostname"
        if (version == ProtocolInfo.VERSION) {
            ctx.writeAndFlush(new PacketHello(false))
            server.getClient(ctx).setStage(ConnectionStage.AUTH)
        } else {
            System.out.println("[$hostname] Kicking client for version mismatch. $version != $ProtocolInfo.VERSION")
            ctx.writeAndFlush(new PacketKick("Version mismatch. I'm on $version."))
        }
    }

}
