package sx.lambda.voxel.net.mc.client;

import com.badlogic.gdx.Gdx;
import org.spacehq.mc.protocol.MinecraftProtocol;
import org.spacehq.mc.protocol.data.message.Message;
import org.spacehq.mc.protocol.data.message.TranslationMessage;
import org.spacehq.mc.protocol.packet.ingame.server.ServerChatPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityMovementPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerBlockChangePacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerMultiBlockChangePacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerMultiChunkDataPacket;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.event.session.DisconnectedEvent;
import org.spacehq.packetlib.event.session.PacketReceivedEvent;
import org.spacehq.packetlib.event.session.SessionAdapter;
import org.spacehq.packetlib.packet.Packet;
import org.spacehq.packetlib.tcp.TcpSessionFactory;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.net.mc.client.handlers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MinecraftClientConnection {

    private final RadixClient game;
    private final Client client;
    private final ChatHandler chatHandler;

    private final Map<Class<? extends Packet>, PacketHandler> handlerMap = new HashMap<>();

    public MinecraftClientConnection(final RadixClient game, String hostname, short port) {
        this.game = game;
        MinecraftProtocol protocol = new MinecraftProtocol("voxeltest-dev");
        this.client = new Client(hostname, port, protocol, new TcpSessionFactory());
        this.chatHandler = new ChatHandler(game);

        handlerMap.put(ServerChunkDataPacket.class, new ChunkDataHandler(game));
        handlerMap.put(ServerMultiChunkDataPacket.class, new MultiChunkDataHandler(game));
        handlerMap.put(ServerPlayerPositionRotationPacket.class, new PlayerPositionHandler(game));
        handlerMap.put(ServerSpawnPlayerPacket.class, new PlayerSpawnHandler(game));
        handlerMap.put(ServerBlockChangePacket.class, new BlockChangeHandler(game));
        handlerMap.put(ServerMultiBlockChangePacket.class, new MultiBlockChangeHandler(game));
        handlerMap.put(ServerJoinGamePacket.class, new JoinGameHandler());
        handlerMap.put(ServerEntityMovementPacket.class, new EntityPositionHandler());
        handlerMap.put(ServerChatPacket.class, chatHandler);

        client.getSession().addListener(new SessionAdapter() {
            @Override
            public void packetReceived(PacketReceivedEvent event) {
                if (event.getPacket() == null) return;
                PacketHandler handler = handlerMap.get(event.getPacket().getClass());
                if (handler != null) {
                    handler.handle(event.getPacket());
                }
                if (event.getPacket() instanceof ServerChatPacket) {
                    Message message = event.<ServerChatPacket>getPacket().getMessage();
                    Gdx.app.debug("", "Received Message: " + message.getFullText());
                    if (message instanceof TranslationMessage) {
                        Gdx.app.debug("", "Received Translation Components: "
                                + Arrays.toString(((TranslationMessage) message).getTranslationParams()));
                    }
                }
            }

            @Override
            public void disconnected(DisconnectedEvent event) {
                RadixClient.getInstance().addToGLQueue(() -> {
                            RadixClient.getInstance().setCurrentScreen(RadixClient.getInstance().getMainMenu());
                            Gdx.app.log("", "Disconnected: " + Message.fromString(event.getReason()).getFullText());
                        }
                );
            }
        });
    }

    public void start() {
        client.getSession().connect();
    }

    public Client getClient() { return client; }

    public ChatHandler getChatHandler() {
        return this.chatHandler;
    }

}
