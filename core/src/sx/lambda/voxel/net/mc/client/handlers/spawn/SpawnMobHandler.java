package sx.lambda.voxel.net.mc.client.handlers.spawn;

import com.badlogic.gdx.Gdx;
import org.spacehq.mc.protocol.data.game.values.entity.MobType;
import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnMobPacket;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.entity.EntityPosition;
import sx.lambda.voxel.entity.EntityRotation;
import sx.lambda.voxel.entity.mob.Mob;
import sx.lambda.voxel.net.mc.client.handlers.PacketHandler;

/**
 * Created by louxiu on 12/19/15.
 */
public class SpawnMobHandler implements PacketHandler<ServerSpawnMobPacket> {
    private final RadixClient game;

    public SpawnMobHandler(RadixClient game) {
        this.game = game;
    }

    @Override
    public void handle(ServerSpawnMobPacket packet) {
        // TODO: support other type of mob
        if (packet.getType() == MobType.CREEPER || packet.getType() == MobType.ENDERMAN ||
                packet.getType() == MobType.SKELETON || packet.getType() == MobType.ZOMBIE) {
            EntityPosition position = new EntityPosition((float) packet.getX(), (float) packet.getY(), (float) packet.getZ());
            EntityRotation rotation = new EntityRotation(-packet.getYaw(), 180 - packet.getPitch());
            Mob mob = new Mob(packet.getType(), packet.getEntityId(), position, rotation);
            game.getWorld().addEntity(packet.getEntityId(), mob);
            game.getGameRenderer().calculateFrustum();
            Gdx.graphics.requestRendering();
        }
    }
}
