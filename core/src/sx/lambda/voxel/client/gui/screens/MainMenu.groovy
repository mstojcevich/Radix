package sx.lambda.voxel.client.gui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.client.gui.GuiScreen

import java.awt.*

@CompileStatic
public class MainMenu implements GuiScreen {

    private static final int TARGET_BUTTON_SIZE = 150
    private static final int BUTTON_SPACING = 4

    private final MainMenuButton[] buttons
    private boolean initialized
    private SpriteBatch batch

    private FrameBuffer mmPrerenderFbo
    private Texture mmPrerender

    private BitmapFont buttonFont

    private Texture mmButtonTexture

    private OrthographicCamera camera

    public MainMenu() {
        buttons = [
                new MainMenuButton(this, "Multiplayer (Local)", enterMPLocal, TARGET_BUTTON_SIZE),
                new MainMenuButton(this, "Multiplayer (Lambda)", enterMPLambda, TARGET_BUTTON_SIZE),
                new MainMenuButton(this, "Settings", {println("Settings pressed!")}, TARGET_BUTTON_SIZE),
                new MainMenuButton(this, "Quit Game", {VoxelGameClient.instance.startShutdown()}, TARGET_BUTTON_SIZE),
        ]
    }

    private Closure enterMPLocal = {
        println("Entering local mp")
        VoxelGameClient.instance.enterRemoteWorld("127.0.0.1", (short)31173)
    }

    private Closure enterMPLambda = {
        println("Entering lambda mp")
        VoxelGameClient.instance.enterRemoteWorld("mc.stoj.pw", (short)31173)
    }

    private void resize() {
        camera.setToOrtho(true, Gdx.graphics.width, Gdx.graphics.height)
        camera.update()

        if(mmPrerenderFbo != null)
            mmPrerenderFbo.dispose()
        mmPrerenderFbo = new FrameBuffer(Pixmap.Format.RGBA4444, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false)
        mmPrerender = mmPrerenderFbo.getColorBufferTexture()

        int dWidth = Gdx.graphics.getWidth()
        int buttonsPerRow = (int)(dWidth / (TARGET_BUTTON_SIZE+BUTTON_SPACING))

        int currentButtonNum = 0
        for(MainMenuButton button : buttons) {
            int btnX = BUTTON_SPACING+(currentButtonNum % buttonsPerRow)*(TARGET_BUTTON_SIZE+BUTTON_SPACING)
            int btnY = BUTTON_SPACING+((int)(currentButtonNum / buttonsPerRow))*(TARGET_BUTTON_SIZE+BUTTON_SPACING)
            button.setPosition(btnX, btnY)
            currentButtonNum++
        }
    }

    public void init() {
        if(!initialized) {
            camera = new OrthographicCamera()
            batch = new SpriteBatch()
            batch.setTransformMatrix(VoxelGameClient.instance.hudCamera.combined)
            batch.setTransformMatrix(camera.combined)
            mmButtonTexture = new Texture(Gdx.files.internal("textures/gui/mmBtn.png"))

            buttonFont = new BitmapFont()

            resize()
            rerender()

            initialized = true
        }
    }

    protected void rerender() {
        mmPrerenderFbo.begin()
        batch.begin()
        Gdx.gl.glClearColor(0, 0, 0, 1)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        for(MainMenuButton button : buttons) {
            button.drawLabel()
            button.render()
        }
        batch.end()
        mmPrerenderFbo.end()
    }

    @Override
    public void render(boolean ingame) {
        batch.begin()
        batch.draw(mmPrerender, 0, 0)
        batch.end()
    }

    void onMouseClick(int clickType) {
        int mouseX = Gdx.input.getX()
        int mouseY = Gdx.graphics.getHeight()-Gdx.input.getY()

        if(clickType == 0) {
            for(MainMenuButton b : buttons) {
                if(b.bounds.contains(mouseX, mouseY)) {
                    b.click()
                }
            }
        }
    }

    @Override
    public void finish() {
        if(mmPrerenderFbo != null)
            mmPrerenderFbo.dispose()
        if(batch != null)
            batch.dispose()
    }

    class MainMenuButton {
        private final String title
        private final Closure onClick
        private final MainMenu parent
        private final Rectangle bounds

        private GlyphLayout labelLayout

        MainMenuButton(MainMenu parent, String title, Closure onClick, int size) {
            this.parent = parent
            this.title = title
            this.onClick = onClick
            this.bounds = new Rectangle(size, size)
        }

        Rectangle getBounds() {
            return this.bounds
        }

        void click() {
            onClick()
        }

        void setSize(int size) {
            this.bounds.setSize(size, size)
            if(buttonFont != null) {
                rerenderLabel()
            }
        }

        void setPosition(int x, int y) {
            this.bounds.setLocation(x, y)
            if(buttonFont != null) {
                rerenderLabel()
            }
        }

        private void render() {
            batch.draw(mmButtonTexture, bounds.x as float, bounds.y as float, bounds.width as float, bounds.height as float)
        }

        void drawLabel() {
            if(buttonFont != null) {
                if (labelLayout == null) {
                    labelLayout = buttonFont.draw(batch, title, -1000, -1000)
                }

                int textStartX = bounds.x+(bounds.width/2.0f - labelLayout.width/2.0f) as int
                int textStartY = bounds.y+(bounds.height/2.0f - labelLayout.height/2.0f) as int
                buttonFont.draw(batch, title, textStartX, textStartY)
            }
        }

        void rerenderLabel() {
            labelLayout = null // force redraw next frame
        }
    }
}