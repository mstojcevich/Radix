package sx.lambda.mstojcevich.voxel.net.packet.shared

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.mstojcevich.voxel.net.packet.server.PacketEndChunkGroup
import sx.lambda.mstojcevich.voxel.net.packet.server.PacketEntityPosition
import sx.lambda.mstojcevich.voxel.net.packet.server.PacketKick
import sx.lambda.mstojcevich.voxel.net.packet.server.PacketStartChunkGroup
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.entity.EntityPosition
import sx.lambda.mstojcevich.voxel.entity.player.Player
import sx.lambda.mstojcevich.voxel.net.packet.SharedPacket
import sx.lambda.mstojcevich.voxel.net.packet.server.PacketChunkData
import sx.lambda.mstojcevich.voxel.server.VoxelGameServer
import sx.lambda.mstojcevich.voxel.server.net.ConnectedClient
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk

@CompileStatic
class PacketPlayerPosition implements SharedPacket {

    private float x, y, z

    public PacketPlayerPosition(EntityPosition pos) {
        this(pos.x, pos.y, pos.z)
    }

    public PacketPlayerPosition(float x, float y, float z) {
        this.x = x
        this.y = y
        this.z = z
    }

    @Override
    //TODO Right now we're going to trust the client, but we definitely shouldn't in the future
    void handleServerReceive(VoxelGameServer server, ChannelHandlerContext ctx) {
        ConnectedClient cc = server.getClient(ctx);
        Player p = cc.player;

        if(p != null) {
            p.getPosition().setPos(x, y, z)

            for(ConnectedClient client : server.clientList) {
                if(client.context != ctx)
                    client.context.writeAndFlush(new PacketEntityPosition(p))
            }

            if(cc.lastChunkSendPos == null) {
                sendChunks(server, ctx, p, server.config.viewDistance)
            } else {
                if (cc.info == null) {
                    if(cc.lastChunkSendPos.planeDistance(p.getPosition()) >= ((server.config.viewDistance/2f)*server.getWorld().getChunkSize()))
                        sendChunks(server, ctx, p, server.config.viewDistance)
                } else {
                    int viewDistance = Math.min(server.config.viewDistance, cc.info.viewDistance);
                    if(cc.lastChunkSendPos.planeDistance(p.getPosition()) >= ((viewDistance/2f)*server.getWorld().getChunkSize()))
                        sendChunks(server, ctx, p, viewDistance)
                }
            }

        } else {
            ctx.writeAndFlush(new PacketKick("Sent position before ready"))
            ctx.disconnect()
            server.rmClient(ctx)
        }
    }

    @Override
    void handleClientReceive(ChannelHandlerContext ctx) {
        VoxelGame.instance.getPlayer().getPosition().setPos(x, y, z)
    }

    private void sendChunks(VoxelGameServer server, ChannelHandlerContext ctx, Player p, int viewDistance) {
        //TODO only send chunks that the client doesn't already have
        IChunk[] chunkList = server.getWorld().getChunksInRange(p.getPosition(), viewDistance)
        ctx.writeAndFlush(new PacketStartChunkGroup())
        for (IChunk c : chunkList) {
            ctx.writeAndFlush(new PacketChunkData(c))
        }
        ctx.writeAndFlush(new PacketEndChunkGroup())
        server.getClient(ctx).lastChunkSendPos = p.getPosition().clone()
    }

}
