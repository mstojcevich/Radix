package sx.lambda.mstojcevich.voxel.net.packet.shared

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.mstojcevich.voxel.server.net.ConnectionStage
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.net.packet.SharedPacket
import sx.lambda.mstojcevich.voxel.server.VoxelGameServer
import sx.lambda.mstojcevich.voxel.server.net.ConnectedClient
import sx.lambda.mstojcevich.voxel.util.Vec3i

@CompileStatic
class PacketBreakBlock implements SharedPacket {

    private final Vec3i blockPos

    public PacketBreakBlock(Vec3i blockPos) {
        this.blockPos = blockPos
    }

    @Override
    void handleServerReceive(VoxelGameServer server, ChannelHandlerContext ctx) {
        server.world.removeBlock(blockPos)
        for(ConnectedClient client : server.getClientList()) {
            if(client.stage == ConnectionStage.PLAY) {
                client.context.writeAndFlush(this)
            }
        }
    }

    @Override
    void handleClientReceive(ChannelHandlerContext ctx) {
        VoxelGame.instance.world.removeBlock(blockPos)
    }

}
