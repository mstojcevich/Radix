package sx.lambda.mstojcevich.voxel.server.net

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.mstojcevich.voxel.entity.player.Player
import sx.lambda.mstojcevich.voxel.entity.EntityPosition
import sx.lambda.mstojcevich.voxel.net.packet.client.PacketClientInfo
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk

import java.util.concurrent.ConcurrentHashMap

@CompileStatic
class ConnectedClient {

    private Player player
    private ConnectionStage stage = ConnectionStage.PREAUTH
    private EntityPosition lastChunkSendPos
    private PacketClientInfo.ClientInfo info
    private ChannelHandlerContext ctx
    public final Set<IChunk> hadChunks = Collections.newSetFromMap(new ConcurrentHashMap<IChunk, Boolean>())

    ConnectedClient(ChannelHandlerContext ctx) {
        this.ctx = ctx
    }

    Player getPlayer() { player }

    ConnectionStage getStage() { stage }

    void setStage(ConnectionStage stage) { this.stage = stage }

    void setPlayer(Player p) {
        this.player = p
    }

    void setLastChunkSendPos(EntityPosition pos) {
        this.lastChunkSendPos = pos
    }

    public EntityPosition getLastChunkSendPos() {
        this.lastChunkSendPos
    }

    public PacketClientInfo.ClientInfo getInfo() { this.info }

    public void setClientInfo(PacketClientInfo.ClientInfo info) { this.info = info }

    public ChannelHandlerContext getContext() { ctx }

}
