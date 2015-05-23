package sx.lambda.voxel.net.packet.client

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.voxel.api.VoxelGameAPI
import sx.lambda.voxel.api.events.server.EventClientJoin
import sx.lambda.voxel.entity.EntityPosition
import sx.lambda.voxel.entity.EntityRotation
import sx.lambda.voxel.entity.player.Player
import sx.lambda.voxel.net.packet.ClientPacket
import sx.lambda.voxel.net.packet.server.*
import sx.lambda.voxel.net.packet.shared.PacketPlayerPosition
import sx.lambda.voxel.server.VoxelGameServer
import sx.lambda.voxel.server.net.ConnectedClient
import sx.lambda.voxel.server.net.ConnectionStage
import sx.lambda.voxel.world.chunk.IChunk

@CompileStatic
/**
 * Information used for verifying authentication
 */
class PacketAuthInfo implements ClientPacket {

    private String username
    private long token

    /**
     * @param username Account username
     * @param token One-time token to verify login with login server
     */
    PacketAuthInfo(String username, long token) {
        this.username = username
        this.token = token
    }

    @Override
    void handleServerReceive(VoxelGameServer server, ChannelHandlerContext ctx) {
        //TODO verify auth
        ctx.writeAndFlush(new PacketAuthSuccess())

        //Send the world and position to the client
        //After client receives it's first position packet, it can assume we're in the play stage and can send play stuff to the server
        /*
         * TODO read from file to find last position of the player
         */
        Player cp = new Player(new EntityPosition(0, 256, 0), new EntityRotation(0, 0));
        cp.setID(server.world.loadedEntities.size() + 1)
        //The +1 prevents us sending the client it's own player id for someone else
        server.world.loadedEntities.add(cp)
        server.getClient(ctx).setPlayer(cp)
        server.getClient(ctx).setLastChunkSendPos(cp.getPosition().clone())
        IChunk[] chunkList = server.getWorld().getChunksInRange(cp.getPosition(), server.config.viewDistance)
        ctx.writeAndFlush(new PacketStartChunkGroup())
        for (IChunk c : chunkList) {
            System.out.println("Sending a chunk")
            ctx.writeAndFlush(new PacketChunkData(c))
            server.getClient(ctx).hadChunks.add(c)
        }
        ctx.writeAndFlush(new PacketEndChunkGroup())
        ctx.writeAndFlush(new PacketPlayerPosition(cp.getPosition()))
        server.getClient(ctx).setStage(ConnectionStage.PLAY)
        ctx.writeAndFlush(new PacketPlayBegin())

        for (ConnectedClient client : server.getClientList()) {
            if (client == server.getClient(ctx)) continue
            //Send them us
            client.getContext().writeAndFlush(new PacketNewEntity(cp)) //TODO don't send players that are out of range
            //Send us them
            ctx.writeAndFlush(new PacketNewEntity(client.getPlayer()))
        }

        VoxelGameAPI.instance.eventManager.push(new EventClientJoin(server.getClient(ctx)))
    }

}
