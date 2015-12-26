package sx.lambda.voxel.net.mc.client.handlers;

import com.badlogic.gdx.Gdx;
import org.spacehq.mc.protocol.data.game.EntityMetadata;
import org.spacehq.mc.protocol.packet.ingame.server.entity.*;
import org.spacehq.packetlib.packet.Packet;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.entity.Entity;
import sx.lambda.voxel.entity.EntityPosition;
import sx.lambda.voxel.entity.EntityRotation;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by louxiu on 12/21/15.
 */
public class EntityHandler implements PacketHandler<Packet> {

    private final RadixClient game;

    public EntityHandler(RadixClient game) {
        this.game = game;
    }

    @Override
    public void handle(Packet object) {
        if (object instanceof ServerEntityPositionPacket) {
            ServerEntityPositionPacket packet = (ServerEntityPositionPacket) object;
            ConcurrentHashMap<Integer, Entity> entities = game.getWorld().getLoadedEntities();
            Entity entity = entities.get(packet.getEntityId());
            if (entity != null) {
                EntityPosition pos = entity.getPosition();
                pos.add((float) packet.getMovementX(), (float) packet.getMovementY(), (float) packet.getMovementZ());
            }
        } else if (object instanceof ServerEntityMovementPacket) {
            if (object instanceof ServerEntityPositionRotationPacket) {
                ServerEntityPositionRotationPacket packet = (ServerEntityPositionRotationPacket) object;
                ConcurrentHashMap<Integer, Entity> entities = game.getWorld().getLoadedEntities();
                Entity entity = entities.get(packet.getEntityId());
                EntityPosition pos = entity.getPosition();
                pos.add((float) packet.getMovementX(), (float) packet.getMovementY(), (float) packet.getMovementZ());
                EntityRotation rotation = entity.getRotation();
                rotation.setRot(-rotation.getYaw() + rotation.getYaw(), rotation.getPitch() - packet.getPitch());
            } else {
                ServerEntityMovementPacket packet = (ServerEntityMovementPacket) object;
            }
        } else if (object instanceof ServerEntityHeadLookPacket) {
            ServerEntityHeadLookPacket packet = (ServerEntityHeadLookPacket) object;
        } else if (object instanceof ServerEntityMetadataPacket) {
            // http://wiki.vg/Entities#Entity_Metadata_Format
            ServerEntityMetadataPacket packet = (ServerEntityMetadataPacket) object;
            EntityMetadata[] entityMetadatas = packet.getMetadata();
            for (EntityMetadata metadata : entityMetadatas) {
                if (metadata.getId() == 6) {
                    Float health = (Float) metadata.getValue();
                    if (health < 0.0001F) {
                        ConcurrentHashMap<Integer, Entity> entities = game.getWorld().getLoadedEntities();
                        Entity entity = entities.remove(packet.getEntityId());
                        if (entity != null) {
                            Gdx.app.log("", "entity is dead, entityId=" + entity.getID());
                        }
                    }
                }
            }
        }

        game.getGameRenderer().calculateFrustum();
        Gdx.graphics.requestRendering();
    }
}
