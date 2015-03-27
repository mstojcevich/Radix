package sx.lambda.mstojcevich.voxel.client.gui.screens

import groovy.transform.CompileStatic
import org.lwjgl.BufferUtils
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.client.gui.VboBufferedGuiScreen
import sx.lambda.mstojcevich.voxel.util.gl.FontRenderer
import sx.lambda.mstojcevich.voxel.world.World

import java.awt.Font
import java.awt.Rectangle
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
                new MainMenuButton(this, "Singleplayer", enterSingleplayer, TARGET_BUTTON_SIZE),
                new MainMenuButton(this, "Multiplayer", enterMultiplayer, TARGET_BUTTON_SIZE),
                new MainMenuButton(this, "Settings", {println("Settings pressed!")}, TARGET_BUTTON_SIZE),
                new MainMenuButton(this, "Quit Game", {VoxelGame.instance.startShutdown()}, TARGET_BUTTON_SIZE),
        ]
    }

    private Closure enterMultiplayer = {
        VoxelGame.instance.enterRemoteWorld("127.0.0.1", (short)31173)
    }

    private Closure enterSingleplayer = {
        VoxelGame.instance.enterLocalWorld(new World(false, false))
    }

    private void resize() {
        int dWidth = Display.getWidth()
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
                    InputStream is = MainMenu.class.getResourceAsStream("/fonts/LiberationSans-Regular.ttf")
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
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(numVertices*PARTS_PER_VERTEX)
        for(MainMenuButton button : buttons) {
            button.render(vertexBuffer)
        }
        vertexBuffer.flip()
        renderVbo(vertexBuffer)
    }

    @Override
    public void render(boolean ingame) {
        GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY)
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        super.render(ingame)

        VoxelGame.instance.guiShader.enableTexturing()
        VoxelGame.instance.guiShader.disableColors()
        if(fontReady) {
            for (MainMenuButton b : buttons) {
                b.drawLabel()
            }
        }
        VoxelGame.instance.guiShader.enableColors()
        VoxelGame.instance.guiShader.disableTexturing()

        GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY)
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY)
    }

    @Override
    void onMouseClick(int clickType) {
        int mouseX = Mouse.getX()
        int mouseY = Display.getHeight()-Mouse.getY()

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
        }

        void setPosition(int x, int y) {
            this.bounds.setLocation(x, y)
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
            int textStartX = bounds.x+(bounds.width/2.0f - buttonFont.getWidth(title)/2.0f) as int
            int textStartY = bounds.y+(bounds.height/2.0f - buttonFont.getHeight(title)/2.0f) as int
            buttonFont.drawString(textStartX, textStartY, title, 1, 1)
        }
    }
}