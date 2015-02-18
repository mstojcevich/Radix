package sx.lambda.mstojcevich.voxel.net.packet.server

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.entity.Entity
import sx.lambda.mstojcevich.voxel.net.packet.ServerPacket

@CompileStatic
/**
 * Sent when an Entity becomes "new" to a client
 */
class PacketNewEntity implements ServerPacket {

    private final Entity entity

    public PacketNewEntity(Entity entity) {
        this.entity = entity
    }

    @Override
    void handleClientReceive(ChannelHandlerContext ctx) {
        entity.model = entity.getDefaultModel()
        VoxelGame.instance.addToGLQueue(new Runnable() {
            @Override
            void run() {
                entity.init()
            }
        })
        VoxelGame.instance.getWorld().addEntity(entity)
    }

}
