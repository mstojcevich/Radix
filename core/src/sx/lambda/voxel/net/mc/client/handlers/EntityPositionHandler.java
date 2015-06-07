package sx.lambda.voxel.net.mc.client.handlers;

import com.badlogic.gdx.Gdx;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityMovementPacket;

public class EntityPositionHandler implements PacketHandler<ServerEntityMovementPacket> {
    @Override
    public void handle(ServerEntityMovementPacket packet) {
        Gdx.graphics.requestRendering();
    }
}
