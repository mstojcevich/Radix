package sx.lambda.voxel.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import sx.lambda.voxel.client.gui.GuiScreen;

public class IngameMenu implements GuiScreen {

    private Stage stage;

    @Override
    public void init() {
        stage = new Stage();

        // TODO memory manage
        ImageTextButtonStyle btnStyle = new ImageTextButtonStyle(
                new SpriteDrawable(new Sprite(new Texture(Gdx.files.internal("textures/gui/guiButtonBackground.png")))),
                new SpriteDrawable(new Sprite(new Texture(Gdx.files.internal("textures/gui/guiButtonBackground-pressed.png")))),
                new SpriteDrawable(new Sprite(new Texture(Gdx.files.internal("textures/gui/guiButtonBackground-disabled.png")))),
                new BitmapFont());

        Button returnButton = new TextButton("Return to Game", btnStyle);
        stage.addActor(returnButton);
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
    }

}
