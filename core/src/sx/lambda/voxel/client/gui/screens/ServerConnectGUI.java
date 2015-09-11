package sx.lambda.voxel.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.client.gui.GuiScreen;


// TODO make an abstract class for Stage-based GUIs
public class ServerConnectGUI implements GuiScreen {

    private Stage stage;

    private Texture background;

    private Label errorLabel;

    @Override
    public void init() {
        stage = new Stage();

        // TODO memory manage

        background = new Texture(Gdx.files.internal("textures/block/obsidian.png"));
        background.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

        errorLabel = new Label(null, new LabelStyle(new BitmapFont(), Color.RED));

        TextFieldStyle fieldStyle = new TextFieldStyle();
        fieldStyle.font = new BitmapFont();
        fieldStyle.fontColor = Color.WHITE;
        TextField ipField = new TextField("IP:Port", fieldStyle);

        ImageTextButtonStyle btnStyle = new ImageTextButtonStyle(
                new SpriteDrawable(new Sprite(new Texture(Gdx.files.internal("textures/gui/guiButtonBackground.png")))),
                new SpriteDrawable(new Sprite(new Texture(Gdx.files.internal("textures/gui/guiButtonBackground-pressed.png")))),
                new SpriteDrawable(new Sprite(new Texture(Gdx.files.internal("textures/gui/guiButtonBackground-disabled.png")))),
                new BitmapFont());

        TextButton connectButton = new TextButton("Connect", btnStyle);
        connectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String[] ipPort = ipField.getText().split(":");

                if(ipPort.length != 2) {
                    invalidIpSyntax();
                    return;
                }

                try {
                    RadixClient.getInstance().enterRemoteWorld(ipPort[0], Short.parseShort(ipPort[1]));
                } catch (NumberFormatException ex) {
                    invalidPort();
                }
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.add(ipField);
        table.row();
        table.add(errorLabel);
        table.row();
        table.add(connectButton);
        stage.addActor(table);
    }

    @Override
    public void render(boolean inGame, SpriteBatch guiBatch) {
        stage.getBatch().begin();
        stage.getBatch().draw(background, 0, 0, stage.getWidth(), stage.getHeight(), 0, 0, stage.getWidth()/background.getWidth(), stage.getHeight()/background.getHeight());
        stage.getBatch().end();
        stage.draw();
    }

    @Override
    public void finish() {
        stage.dispose();
        background.dispose();
    }

    @Override
    public void onMouseClick(int button, boolean up) {
        if(up) {
            stage.touchUp(mouseX, mouseY, 0, button);
        } else {
            stage.touchDown(mouseX, mouseY, 0, button);
        }
    }

    int mouseX, mouseY;
    @Override
    public void mouseMoved(int x, int y) {
        mouseX = x; mouseY = y;
        stage.mouseMoved(x, y);
    }

    @Override
    public void keyTyped(char c) {
        stage.keyTyped(c);
    }

    private void invalidIpSyntax() {
        errorLabel.setText("Invalid IP syntax (should be ip:port)");
    }

    private void invalidPort() {
        errorLabel.setText("Invalid port. Should be a number from 0 to 65535");
    }

}
