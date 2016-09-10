package sx.lambda.voxel.net.mc.client;

import com.badlogic.gdx.Gdx;
import org.spacehq.mc.protocol.MinecraftProtocol;
import org.spacehq.mc.protocol.data.message.Message;
import org.spacehq.mc.protocol.data.message.TranslationMessage;
import org.spacehq.mc.protocol.packet.ingame.server.*;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityHeadLookPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityMetadataPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityMovementPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityPositionPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerAbilitiesPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerHealthPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnMobPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.*;
import org.spacehq.mc.protocol.packet.login.server.LoginSetCompressionPacket;
import org.spacehq.mc.protocol.packet.login.server.LoginSuccessPacket;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.event.session.DisconnectedEvent;
import org.spacehq.packetlib.event.session.PacketReceivedEvent;
import org.spacehq.packetlib.event.session.SessionAdapter;
import org.spacehq.packetlib.packet.Packet;
import org.spacehq.packetlib.tcp.TcpSessionFactory;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.net.mc.client.handlers.*;
import sx.lambda.voxel.net.mc.client.handlers.login.LoginSuccessHandler;
import sx.lambda.voxel.net.mc.client.handlers.login.SetCompressionHandler;
import sx.lambda.voxel.net.mc.client.handlers.player.PlayerAbilitiesHandler;
import sx.lambda.voxel.net.mc.client.handlers.player.UpdateHealthHandler;
import sx.lambda.voxel.net.mc.client.handlers.spawn.PlayerSpawnHandler;
import sx.lambda.voxel.net.mc.client.handlers.spawn.SpawnMobHandler;

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
        MinecraftProtocol protocol = new MinecraftProtocol("radix-dev");
        this.client = new Client(hostname, port, protocol, new TcpSessionFactory());
        this.chatHandler = new ChatHandler(game);

        handlerMap.put(ServerChunkDataPacket.class, new ChunkDataHandler(game));
        handlerMap.put(ServerPlayerPositionRotationPacket.class, new PlayerPositionHandler(game));
        handlerMap.put(ServerSpawnPlayerPacket.class, new PlayerSpawnHandler(game));
        handlerMap.put(ServerBlockChangePacket.class, new BlockChangeHandler(game));
        handlerMap.put(ServerMultiBlockChangePacket.class, new MultiBlockChangeHandler(game));
        handlerMap.put(ServerJoinGamePacket.class, new JoinGameHandler(game));
        handlerMap.put(ServerEntityMovementPacket.class, new EntityHandler(game));
        handlerMap.put(ServerEntityPositionPacket.class, new EntityHandler(game));
        handlerMap.put(ServerChatPacket.class, chatHandler);
        handlerMap.put(LoginSetCompressionPacket.class, new SetCompressionHandler());
        handlerMap.put(LoginSuccessPacket.class, new LoginSuccessHandler());
        handlerMap.put(ServerPluginMessagePacket.class, new PluginMessageHandler());
        handlerMap.put(ServerDifficultyPacket.class, new ServerDifficultyHandler());
        handlerMap.put(ServerPlayerAbilitiesPacket.class, new PlayerAbilitiesHandler());
        handlerMap.put(ServerSpawnPositionPacket.class, new SpawnPositionHandler(game));
        handlerMap.put(ServerUpdateTimePacket.class, new UpdateTimeHandler());
        handlerMap.put(ServerPlayerListEntryPacket.class, new PlayerListEntryHandler());
        handlerMap.put(ServerPlayerHealthPacket.class, new UpdateHealthHandler());
        handlerMap.put(ServerSpawnMobPacket.class, new SpawnMobHandler(game));
        handlerMap.put(ServerEntityHeadLookPacket.class, new EntityHandler(game));
        handlerMap.put(ServerEntityMetadataPacket.class, new EntityHandler(game));
        client.getSession().addListener(new SessionAdapter() {
            @Override
            public void packetReceived(PacketReceivedEvent event) {
                if (event.getPacket() == null) return;
                try {
                    PacketHandler handler = handlerMap.get(event.getPacket().getClass());
                    if (handler != null) {
                        handler.handle(event.getPacket());
                        return;
                    }
                    if (event.getPacket() instanceof ServerChatPacket) {
                        Message message = event.<ServerChatPacket>getPacket().getMessage();
                        if (message instanceof TranslationMessage) {
                            Gdx.app.debug("", "Received Translation Components: "
                                    + Arrays.toString(((TranslationMessage) message).getTranslationParams()));
                        } else {
                            Gdx.app.debug("", "Received Message: " + message.getFullText());
                        }
                    } else {
                        // Gdx.app.debug("", "Received Unknown Components: " + event.getPacket());
                    }
                } catch(Exception e) {
                    e.printStackTrace();
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
