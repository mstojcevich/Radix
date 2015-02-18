package sx.lambda.mstojcevich.voxel.net.packet.server

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.entity.Entity
import sx.lambda.mstojcevich.voxel.net.packet.ServerPacket

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
        for(Entity e : VoxelGame.instance.world.loadedEntities) {
            if(e.ID == entityId) {
                VoxelGame.instance.world.loadedEntities.remove(e)
            }
        }
    }

}