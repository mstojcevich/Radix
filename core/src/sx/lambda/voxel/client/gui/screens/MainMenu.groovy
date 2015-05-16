package sx.lambda.voxel.client.gui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.utils.BufferUtils
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.client.gui.VboBufferedGuiScreen
import sx.lambda.voxel.util.gl.FontRenderer
import sx.lambda.voxel.util.gl.SpriteBatcher

import java.awt.*
import java.nio.FloatBuffer

@CompileStatic
public class MainMenu extends VboBufferedGuiScreen {

    private static final int VERTICES_PER_BUTTON = 6
    private static final int TARGET_BUTTON_SIZE = 150
    private static final int BUTTON_SPACING = 4

    private static final int PARTS_PER_VERTEX = 6 // XYRGBA

    private final MainMenuButton[] buttons

    private FontRenderer buttonFont

    private boolean fontReady

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

    @Override
    public void init() {
        if(!initialized) {
            new Thread("Main Menu Font Loading") {
                @Override
                public void run() {
                    InputStream is = Gdx.files.internal("fonts/LiberationSans-Regular.ttf").read()
                    buttonFont = new FontRenderer(Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(16f), true)
                    fontReady = true
                }
            }.start()
            resize()

            super.init()

            initialized = true
        }
    }

    @Override
    protected void rerender() {
        int numVertices = buttons.length*VERTICES_PER_BUTTON
        FloatBuffer vertexBuffer = BufferUtils.newFloatBuffer(numVertices * PARTS_PER_VERTEX)
        for(MainMenuButton button : buttons) {
            button.render(vertexBuffer)
        }
        vertexBuffer.flip()
        renderVbo(vertexBuffer)
    }

    @Override
    public void render(boolean ingame) {
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        super.render(ingame)

        VoxelGameClient.instance.guiShader.enableTexturing()
        if(fontReady) {
            for (MainMenuButton b : buttons) {
                b.drawLabel()
            }
        }
        VoxelGameClient.instance.guiShader.disableTexturing()
    }

    @Override
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

    class MainMenuButton {
        private final String title
        private final Closure onClick
        private final MainMenu parent
        private final Rectangle bounds

        private SpriteBatcher.StaticRender labelRender

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

        private void render(FloatBuffer vertexBuffer) {
            float x1 = bounds.x as float
            float x2 = bounds.x+bounds.width as float
            float y1 = bounds.y as float
            float y2 = bounds.y+bounds.height as float
            float[] bottomRight = [x2, y2, 0.2f, 0.2f, 0.2f, 0.7f] as float[]
            float[] bottomLeft = [x1, y2, 0.2f, 0.2f, 0.2f, 0.7f] as float[]
            float[] topLeft = [x1, y1, 0.2f, 0.2f, 0.2f, 0.7f] as float[]
            float[] topRight = [x2, y1, 0.2f, 0.2f, 0.2f, 0.7f] as float[]

            vertexBuffer.put(topLeft).put(bottomLeft).put(topRight);
            vertexBuffer.put(bottomRight).put(topRight).put(bottomLeft);
        }

        void drawLabel() {
            if(buttonFont != null) {
                if (labelRender == null) {
                    rerenderLabel()
                }

                labelRender.render()
            }
        }

        void rerenderLabel() {
            if(labelRender != null) {
                labelRender.destroy()
            }
            int textStartX = bounds.x+(bounds.width/2.0f - buttonFont.getWidth(title)/2.0f) as int
            int textStartY = bounds.y+(bounds.height/2.0f - buttonFont.getHeight(title)/2.0f) as int
            labelRender = buttonFont.drawStringStatic(textStartX, textStartY, title, FontRenderer.ALIGN_LEFT)
        }
    }
}