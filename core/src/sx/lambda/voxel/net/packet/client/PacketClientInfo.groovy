package sx.lambda.voxel.net.packet.client

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import sx.lambda.voxel.net.packet.ClientPacket
import sx.lambda.voxel.server.VoxelGameServer

@CompileStatic
class PacketClientInfo implements ClientPacket {

    private final ClientInfo info

    public PacketClientInfo(ClientInfo info) {
        this.info = info
    }

    @Override
    void handleServerReceive(VoxelGameServer server, ChannelHandlerContext ctx) {
        server.getClient(ctx).setClientInfo(this.info)
    }

    static class ClientInfo {

        private final int viewDistance

        ClientInfo(int viewDistance) {
            this.viewDistance = viewDistance
        }

        public int getViewDistance() {
            return viewDistance
        }

    }

    static class ClientInfoBuilder {

        private int viewDistance

        public ClientInfoBuilder setViewDistance(int viewDistance) {
            this.viewDistance = viewDistance
            this
        }

        public ClientInfo build() {
            new ClientInfo(viewDistance)
        }

    }

}
