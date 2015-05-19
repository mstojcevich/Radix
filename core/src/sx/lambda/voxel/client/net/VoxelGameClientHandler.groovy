package sx.lambda.voxel.client.net

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.net.packet.server.*
import sx.lambda.voxel.net.packet.server.PacketChunkData
import sx.lambda.voxel.net.packet.server.PacketEntityPosition
import sx.lambda.voxel.net.packet.server.PacketNewEntity
import sx.lambda.voxel.net.packet.server.PacketPlayBegin
import sx.lambda.voxel.net.packet.server.PacketStartChunkGroup
import sx.lambda.voxel.net.packet.shared.PacketPlaceBlock
import sx.lambda.voxel.net.packet.shared.PacketPlayerPosition
import sx.lambda.voxel.net.packet.ServerPacket
import sx.lambda.voxel.net.packet.server.PacketAuthSuccess
import sx.lambda.voxel.net.packet.server.PacketEndChunkGroup
import sx.lambda.voxel.net.packet.server.PacketKick
import sx.lambda.voxel.net.packet.server.PacketRmEntity
import sx.lambda.voxel.net.packet.shared.PacketBreakBlock
import sx.lambda.voxel.net.packet.shared.PacketHello

@CompileStatic
class VoxelGameClientHandler extends ChannelHandlerAdapter  {

    private Class<ServerPacket>[] receiveablePackets = [
            PacketHello.class,
            PacketKick.class,
            PacketAuthSuccess.class,
            PacketPlayerPosition.class,
            PacketChunkData.class,
            PacketStartChunkGroup.class,
            PacketEndChunkGroup.class,
            PacketPlayBegin.class,
            PacketBreakBlock.class,
            PacketPlaceBlock.class,
            PacketNewEntity.class,
            PacketEntityPosition.class,
            PacketRmEntity.class
    ]

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        super.channelActive(ctx);
        ctx.writeAndFlush(new PacketHello(true))
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            for (Class<ServerPacket> c : receiveablePackets) {
                if (c.isInstance(msg)) {
                    c.cast(msg).handleClientReceive(ctx)
                }
            }
        } catch(Exception e) {
            VoxelGameClient.instance.handleCriticalException(e)
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush()
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace()
        ctx.close()
    }

}
