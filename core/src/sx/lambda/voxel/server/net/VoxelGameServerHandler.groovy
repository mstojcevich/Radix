package sx.lambda.voxel.server.net

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import sx.lambda.voxel.net.packet.ClientPacket
import sx.lambda.voxel.net.packet.server.PacketKick
import sx.lambda.voxel.server.VoxelGameServer

/**
 * Handler for the voxel game server
 */
@CompileStatic
class VoxelGameServerHandler extends ChannelHandlerAdapter {

    private final VoxelGameServer server;

    public VoxelGameServerHandler(VoxelGameServer gameServer) {
        this.server = gameServer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        super.channelActive(ctx);
        server.addClient(ctx, new ConnectedClient(ctx));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        boolean validPacket = false
        for (Class<? extends ClientPacket> c : server.getClient(ctx).stage.receivablePackets) {
            if (c.isInstance(msg)) {
                c.cast(msg).handleServerReceive(server, ctx)
                validPacket = true
            }
        }
        if (!validPacket) {
            String hostname = ((InetSocketAddress) ctx.channel().remoteAddress()).hostName
            String className = msg.class.name
            println "[$hostname] Kicking client for invalid object (type $className) in stage " + server.getClient(ctx).getStage().name()
            println "Valid packets are " + server.getClient(ctx).stage.receivablePackets
            ctx.writeAndFlush(new PacketKick("Invalid object received"))
            ctx.disconnect()
            server.rmClient(ctx)
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush()
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        server.rmClient(ctx);
        cause.printStackTrace()
        ctx.close()
    }

}
