package sx.lambda.voxel.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.client.gui.GuiScreen;

public class IngameMenu implements GuiScreen {

    private Stage stage;

    @Override
    public void init() {
        stage = new Stage();

        TextButtonStyle btnStyle = RadixClient.getInstance().getSceneTheme().getButtonStyle();

        Button visualSettingsBtn = new TextButton("Visual Settings", btnStyle);
        visualSettingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                RadixClient.getInstance().setCurrentScreen(new VisualSettingsMenu());
            }
        });

        Button returnButton = new TextButton("Return to Game", btnStyle);
        returnButton.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                RadixClient.getInstance().setCurrentScreen(RadixClient.getInstance().getHud());
            }
        });

        Button mainMenuButton = new TextButton("Exit to Main Menu", btnStyle);
        mainMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                RadixClient.getInstance().getMinecraftConn().getClient().getSession().disconnect("Leaving");
                RadixClient.getInstance().setCurrentScreen(RadixClient.getInstance().getMainMenu());
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.add(visualSettingsBtn);
        table.row();
        table.add(returnButton);
        table.row();
        table.add(mainMenuButton);
        stage.addActor(table);
    }

    @Override
    public void render(boolean inGame, SpriteBatch guiBatch) {
        stage.draw();
    }

    @Override
    public void finish() {
        stage.dispose();
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

}
