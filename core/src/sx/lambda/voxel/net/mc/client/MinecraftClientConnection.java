package sx.lambda.voxel.net.mc.client;

import org.spacehq.mc.protocol.MinecraftProtocol;
import org.spacehq.mc.protocol.data.game.values.setting.ChatVisibility;
import org.spacehq.mc.protocol.data.game.values.setting.SkinPart;
import org.spacehq.mc.protocol.data.message.Message;
import org.spacehq.mc.protocol.data.message.TranslationMessage;
import org.spacehq.mc.protocol.packet.ingame.client.ClientChatPacket;
import org.spacehq.mc.protocol.packet.ingame.client.ClientSettingsPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerChatPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerMultiChunkDataPacket;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.event.session.DisconnectedEvent;
import org.spacehq.packetlib.event.session.PacketReceivedEvent;
import org.spacehq.packetlib.event.session.SessionAdapter;
import org.spacehq.packetlib.packet.Packet;
import org.spacehq.packetlib.tcp.TcpSessionFactory;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.net.mc.client.handlers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MinecraftClientConnection {

    private final VoxelGameClient game;
    private final Client client;

    private final Map<Class<? extends Packet>, PacketHandler> handlerMap = new HashMap<>();

    public MinecraftClientConnection(final VoxelGameClient game, String hostname, short port) {
        this.game = game;
        MinecraftProtocol protocol = new MinecraftProtocol("marcusant");
        this.client = new Client(hostname, port, protocol, new TcpSessionFactory());

        handlerMap.put(ServerChunkDataPacket.class, new ChunkDataHandler(game));
        handlerMap.put(ServerMultiChunkDataPacket.class, new MultiChunkDataHandler(game));
        handlerMap.put(ServerPlayerPositionRotationPacket.class, new PlayerPositionHandler(game));
        handlerMap.put(ServerSpawnPlayerPacket.class, new PlayerSpawnHandler(game));

        client.getSession().addListener(new SessionAdapter() {
            @Override
            public void packetReceived(PacketReceivedEvent event) {
                if(event.getPacket() == null)return;
                PacketHandler handler = handlerMap.get(event.getPacket().getClass());
                if(handler != null) {
                    handler.handle(event.getPacket());
                }
                if (event.getPacket() instanceof ServerJoinGamePacket) {
                    event.getSession().send(new ClientChatPacket("Hello, this is a test of VoxelTest."));
                    event.getSession().send(new ClientSettingsPacket("en_US", 2, ChatVisibility.FULL, false, SkinPart.HAT));
                } else if (event.getPacket() instanceof ServerChatPacket) {
                    Message message = event.<ServerChatPacket>getPacket().getMessage();
                    System.out.println("Received Message: " + message.getFullText());
                    if (message instanceof TranslationMessage) {
                        System.out.println("Received Translation Components: " + Arrays.toString(((TranslationMessage) message).getTranslationParams()));
                    }
                }
            }

            @Override
            public void disconnected(DisconnectedEvent event) {
                System.out.println("Disconnected: " + Message.fromString(event.getReason()).getFullText());
            }
        });
    }

    public void start() {
        client.getSession().connect();
    }

    public Client getClient() { return client; }

}
