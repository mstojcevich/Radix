package sx.lambda.voxel.net.packet.server

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.net.packet.ServerPacket
import sx.lambda.voxel.entity.Entity

@CompileStatic
/**
 * Sent when an Entity should be removed from the client
 */
class PacketRmEntity implements ServerPacket {

    private final int entityId

    public PacketRmEntity(int entityId) {
        this.entityId = entityId
    }

    @Override
    void handleClientReceive(ChannelHandlerContext ctx) {
        //TODO the entity isn't actually getting removed. Make sure ID stuff is correct.
        println("WE GOT AN RM!")
        for(Entity e : VoxelGameClient.instance.world.loadedEntities) {
            if(e.ID == entityId) {
                VoxelGameClient.instance.world.loadedEntities.remove(e)
            }
        }
    }

}