package sx.lambda.voxel.client.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class SceneTheme {

    private BitmapFont labelFont;

    private Texture buttonBgTex;
    private Texture buttonBgPressTex;
    private Texture buttonBgDisabledTex;

    private ImageTextButtonStyle buttonStyle;

    public void init() {
        this.labelFont = new BitmapFont();

        this.buttonBgTex = new Texture(Gdx.files.internal("textures/gui/guiButtonBackground.png"));
        this.buttonBgPressTex = new Texture(Gdx.files.internal("textures/gui/guiButtonBackground-pressed.png"));
        this.buttonBgDisabledTex = new Texture(Gdx.files.internal("textures/gui/guiButtonBackground-disabled.png"));

        SpriteDrawable buttonBgSprite = new SpriteDrawable(new Sprite(buttonBgTex));
        SpriteDrawable buttonBgPressSprite = new SpriteDrawable(new Sprite(buttonBgPressTex));
        SpriteDrawable buttonBgDisabledSprite =new SpriteDrawable(new Sprite(buttonBgDisabledTex));
        this.buttonStyle = new ImageTextButtonStyle(buttonBgSprite, buttonBgPressSprite, buttonBgDisabledSprite, labelFont);
    }

    public void dispose() {
        this.labelFont.dispose();

        this.buttonBgTex.dispose();
        this.buttonBgPressTex.dispose();
        this.buttonBgDisabledTex.dispose();
    }

    public ImageTextButtonStyle getButtonStyle() {
        return this.buttonStyle;
    }

}
