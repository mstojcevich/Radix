package sx.lambda.mstojcevich.voxel.net.packet.shared

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.block.Block
import sx.lambda.mstojcevich.voxel.net.packet.SharedPacket
import sx.lambda.mstojcevich.voxel.server.VoxelGameServer
import sx.lambda.mstojcevich.voxel.server.net.ConnectedClient
import sx.lambda.mstojcevich.voxel.server.net.ConnectionStage
import sx.lambda.mstojcevich.voxel.util.Vec3i

@CompileStatic
class PacketPlaceBlock implements SharedPacket {

    private final Block block
    private final Vec3i pos

    public PacketPlaceBlock(Vec3i pos, Block block) {
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
        VoxelGame.instance.world.addBlock(block, pos)
    }

}
