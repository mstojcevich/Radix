package sx.lambda.voxel.client.gui.screens;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.spacehq.mc.protocol.data.message.Message;
import org.spacehq.mc.protocol.data.message.TranslationMessage;
import sx.lambda.voxel.client.gui.GuiScreen;
import sx.lambda.voxel.net.mc.client.MinecraftClientConnection;
import sx.lambda.voxel.net.mc.client.handlers.ChatHandler;

import java.util.ArrayList;
import java.util.List;

public class ChatGUI implements GuiScreen, ChatHandler.ChatMessageListener {

    private List<String> chatMessages = new ArrayList<>();

    private BitmapFont font;
    private BitmapFontCache fontCache;

    private MinecraftClientConnection connection;

    @Override
    public void init() {
        font = new BitmapFont();
        fontCache = font.newFontCache();
    }

    @Override
    public void render(boolean inGame, SpriteBatch guiBatch) {
        // TODO implement
    }

    @Override
    public void finish() {
        font.dispose();
    }

    @Override
    public void onMouseClick(int button) {

    }

    /**
     * Setup the chat GUI with a new Minecraft server connection
     */
    public void setup(MinecraftClientConnection conn) {
        if(this.connection != null)
            this.connection.getChatHandler().removeMessageListener(this);
        this.connection = conn;
        chatMessages.clear();
        conn.getChatHandler().addMessageListener(this);
    }

    public void renderHud(SpriteBatch batch) {
        // TODO only render most recent x messages
        if(chatMessages.size() > 0) {
            fontCache.draw(batch);
        }
    }

    @Override
    public void onChatMessage(Message message) {
        if (message instanceof TranslationMessage) {
            TranslationMessage tmsg = (TranslationMessage)message;
            try {
                chatMessages.add(String.format("<%s> %s", tmsg.getTranslationParams()[0], tmsg.getTranslationParams()[1]));
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                chatMessages.add(message.getFullText());
            }
        } else {
            chatMessages.add(message.getFullText());
        }

        StringBuilder messages = new StringBuilder();
        for(String s : chatMessages) {
            messages.append(s).append("\n");
        }
        fontCache.clear();
        GlyphLayout layout = fontCache.addText(messages, 0, 0);
        fontCache.setPosition(4, layout.height + 50);
    }

}
