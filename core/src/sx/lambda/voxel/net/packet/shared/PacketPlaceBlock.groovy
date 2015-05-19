package sx.lambda.voxel.net.packet.shared

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.net.packet.SharedPacket
import sx.lambda.voxel.server.net.ConnectionStage
import sx.lambda.voxel.util.Vec3i
import sx.lambda.voxel.server.VoxelGameServer
import sx.lambda.voxel.server.net.ConnectedClient

@CompileStatic
class PacketPlaceBlock implements SharedPacket {

    private final int block
    private final Vec3i pos

    public PacketPlaceBlock(Vec3i pos, int block) {
        this.pos = pos
        this.block = block
    }

    @Override
    void handleServerReceive(VoxelGameServer server, ChannelHandlerContext ctx) {
        server.world.addBlock(block, pos)
        for(ConnectedClient client : server.getClientList()) {
            if(client.stage == ConnectionStage.PLAY) {
                client.context.writeAndFlush(this)
            }
        }
    }

    @Override
    void handleClientReceive(ChannelHandlerContext ctx) {
        VoxelGameClient.instance.world.addBlock(block, pos)
    }

}
