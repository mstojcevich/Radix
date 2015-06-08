package sx.lambda.voxel.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import org.spacehq.mc.protocol.data.message.Message;
import org.spacehq.mc.protocol.data.message.TranslationMessage;
import org.spacehq.mc.protocol.packet.ingame.client.ClientChatPacket;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.client.gui.GuiScreen;
import sx.lambda.voxel.net.mc.client.MinecraftClientConnection;
import sx.lambda.voxel.net.mc.client.handlers.ChatHandler;

import java.util.ArrayList;
import java.util.List;

public class ChatGUI implements GuiScreen, ChatHandler.ChatMessageListener {

    private final List<String> chatMessages = new ArrayList<>();

    private BitmapFont font;
    private BitmapFontCache fontCache;
    private Texture chatBackground;

    private MinecraftClientConnection connection;

    private GlyphLayout chatAreaLayout;

    private String currentMessage = "";

    public ChatGUI() {
        font = new BitmapFont();
        fontCache = font.newFontCache();
        chatBackground = new Texture(Gdx.files.internal("textures/gui/chatbackground.png"));
    }

    @Override
    public void init() {
    }

    @Override
    public void render(boolean inGame, SpriteBatch guiBatch) {
        if(chatAreaLayout != null) {
            guiBatch.draw(chatBackground, fontCache.getX() - 4, fontCache.getY() + 4, chatAreaLayout.width + 8, -(chatAreaLayout.height));
            renderHud(guiBatch);
        }
        font.draw(guiBatch, "> " + currentMessage + (System.currentTimeMillis() % 1000 >= 500 ? "_" : ""), 4, 4 + font.getLineHeight());
    }

    @Override
    public void finish() {
    }

    @Override
    public void onMouseClick(int button) {

    }

    @Override
    public void keyTyped(char c) {
        if(c == 8 && currentMessage.length() > 0) { // backspace
            currentMessage = currentMessage.substring(0, currentMessage.length()-1);
            return;
        }
        if(c == 13 && currentMessage.length() > 0) { // enter
            RadixClient.getInstance().getMinecraftConn().getClient().getSession().send(new ClientChatPacket(currentMessage));
            currentMessage = "";
            RadixClient.getInstance().setCurrentScreen(RadixClient.getInstance().getHud());
            return;
        }
        if(currentMessage.length() < 100 && c >= 32 && c <= 126) { // Ascii printable range
            currentMessage += c;
        }
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
        chatAreaLayout = fontCache.addText(messages, 0, 0, 512, Align.left, true);
        fontCache.setPosition(4, chatAreaLayout.height + 50);
    }

}
