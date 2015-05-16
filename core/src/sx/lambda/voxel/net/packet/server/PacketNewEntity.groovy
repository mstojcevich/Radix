package sx.lambda.voxel.net.packet.server

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.entity.Entity
import sx.lambda.voxel.net.packet.ServerPacket

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
        VoxelGameClient.instance.addToGLQueue(new Runnable() {
            @Override
            void run() {
                entity.init()
            }
        })
        VoxelGameClient.instance.getWorld().addEntity(entity)
    }

}
