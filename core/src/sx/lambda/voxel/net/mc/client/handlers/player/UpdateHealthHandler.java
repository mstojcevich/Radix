package sx.lambda.voxel.net.mc.client.handlers.player;

import com.badlogic.gdx.Gdx;
import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerUpdateHealthPacket;
import sx.lambda.voxel.net.mc.client.handlers.PacketHandler;

/**
 * Created by louxiu on 12/19/15.
 */
public class UpdateHealthHandler implements PacketHandler<ServerUpdateHealthPacket> {
    @Override
    public void handle(ServerUpdateHealthPacket packet) {
        Gdx.app.debug("", "Player, health=" + packet.getHealth() + ", saturation=" + packet.getSaturation());
        if (packet.getHealth() < 0.0001D) {
            Gdx.app.log("", "Player are dead!!!!!!!");
        }
    }
}
