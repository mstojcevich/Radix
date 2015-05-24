package sx.lambda.voxel.net.mc.client.handlers;

import org.spacehq.packetlib.packet.Packet;

public interface PacketHandler <E extends Packet> {

    void handle(E packet);

}
