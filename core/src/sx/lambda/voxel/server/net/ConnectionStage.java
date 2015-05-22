package sx.lambda.voxel.server.net;

import groovy.transform.CompileStatic;
import sx.lambda.voxel.net.packet.ClientPacket;
import sx.lambda.voxel.net.packet.client.PacketAuthInfo;
import sx.lambda.voxel.net.packet.client.PacketClientInfo;
import sx.lambda.voxel.net.packet.client.PacketLeaving;
import sx.lambda.voxel.net.packet.client.PacketUnloadChunk;
import sx.lambda.voxel.net.packet.shared.PacketBreakBlock;
import sx.lambda.voxel.net.packet.shared.PacketHello;
import sx.lambda.voxel.net.packet.shared.PacketPlaceBlock;
import sx.lambda.voxel.net.packet.shared.PacketPlayerPosition;

@CompileStatic
public enum ConnectionStage {
    PREAUTH(PacketLeaving.class, PacketHello.class),
    AUTH(PacketLeaving.class, PacketAuthInfo.class),
    PLAY(PacketLeaving.class, PacketClientInfo.class,
            PacketPlayerPosition.class, PacketBreakBlock.class,
            PacketPlaceBlock.class, PacketUnloadChunk.class);

    private final Class<? extends ClientPacket>[] receivablePackets;

    ConnectionStage(Class<? extends ClientPacket>... receivablePackets) {
        this.receivablePackets = receivablePackets;
    }

    public Class<? extends ClientPacket>[] getReceivablePackets() {
        return receivablePackets;
    }
}
