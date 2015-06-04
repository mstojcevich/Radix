package sx.lambda.voxel.net.mc.client.handlers;

import org.spacehq.mc.protocol.data.message.Message;
import org.spacehq.mc.protocol.packet.ingame.server.ServerChatPacket;
import sx.lambda.voxel.VoxelGameClient;

import java.util.ArrayList;
import java.util.List;

public class ChatHandler implements PacketHandler<ServerChatPacket> {

    private final List<ChatMessageListener> listeners = new ArrayList<>();

    public ChatHandler(VoxelGameClient game) {}

    @Override
    public void handle(ServerChatPacket packet) {
        for(ChatMessageListener listener : listeners) {
            listener.onChatMessage(packet.getMessage());
        }
    }

    public void addMessageListener(ChatMessageListener listener) {
        this.listeners.add(listener);
    }

    public void removeMessageListener(ChatMessageListener listener) {
        this.listeners.remove(listener);
    }

    public interface ChatMessageListener {
        void onChatMessage(Message message);
    }

}
