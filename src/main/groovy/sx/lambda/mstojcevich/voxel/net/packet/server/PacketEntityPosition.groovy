package sx.lambda.mstojcevich.voxel.net.packet.server

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.entity.Entity
import sx.lambda.mstojcevich.voxel.entity.EntityPosition
import sx.lambda.mstojcevich.voxel.entity.EntityRotation
import sx.lambda.mstojcevich.voxel.net.packet.ServerPacket

@CompileStatic
class PacketEntityPosition implements ServerPacket {

    private final int entityID
    private final EntityPosition pos
    private final EntityRotation rot

    public PacketEntityPosition(Entity entity) {
        this.entityID = entity.getID()
        this.pos = entity.getPosition()
        this.rot = entity.getRotation()
    }

    @Override
    void handleClientReceive(ChannelHandlerContext ctx) {
        for(Entity e : VoxelGame.instance.world.loadedEntities) {
            if(e.getID() == entityID) {
                e.position.setPos(pos.x, pos.y, pos.z)
                e.rotation.setRot(rot.pitch, rot.yaw)
            }
        }
    }

}
