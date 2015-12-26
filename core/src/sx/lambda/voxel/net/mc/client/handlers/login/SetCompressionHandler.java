package sx.lambda.voxel.net.mc.client.handlers.login;

import org.spacehq.mc.protocol.packet.login.server.LoginSetCompressionPacket;
import sx.lambda.voxel.net.mc.client.handlers.PacketHandler;

/**
 * Created by louxiu on 12/17/15.
 */
public class SetCompressionHandler implements PacketHandler<LoginSetCompressionPacket> {
    @Override
    public void handle(LoginSetCompressionPacket packet) {
    }
}
