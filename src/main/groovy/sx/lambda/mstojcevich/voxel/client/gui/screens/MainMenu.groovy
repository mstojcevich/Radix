package sx.lambda.mstojcevich.voxel.client.gui.screens

import groovy.transform.CompileStatic
import org.lwjgl.BufferUtils
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.newdawn.slick.UnicodeFont
import org.newdawn.slick.font.effects.ColorEffect
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.client.gui.VboBufferedGuiScreen

import java.awt.Color
import java.awt.Font
import java.awt.Rectangle
import java.nio.FloatBuffer

@CompileStatic
public class MainMenu extends VboBufferedGuiScreen {

    private static final int VERTICES_PER_BUTTON = 6
    private static final int TARGET_BUTTON_SIZE = 100
    private static final int BUTTON_SPACING = 4

    private final MainMenuButton[] buttons

    private UnicodeFont buttonFont

    public MainMenu() {
        buttons = [
                new MainMenuButton(this, "Singleplayer", {println("Singleplayer pressed!")}, TARGET_BUTTON_SIZE),
                new MainMenuButton(this, "Multiplayer", {println("Multiplayer pressed!")}, TARGET_BUTTON_SIZE),
                new MainMenuButton(this, "Settings", {println("Settings pressed!")}, TARGET_BUTTON_SIZE),
        ]
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
        buttonFont = new UnicodeFont(new Font(Font.SANS_SERIF, Font.BOLD, 16))
        buttonFont.effects.add(new ColorEffect(Color.white))
        buttonFont.addAsciiGlyphs()
        buttonFont.loadGlyphs()
        resize()

        super.init()
    }

    @Override
    protected void rerender() {
        int numVertices = buttons.length*VERTICES_PER_BUTTON
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(numVertices*2)
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(numVertices*4)
        for(MainMenuButton button : buttons) {
            button.render(vertexBuffer, colorBuffer)
        }
        vertexBuffer.flip()
        colorBuffer.flip()
        renderVbo(vertexBuffer, colorBuffer)
    }

    @Override
    public void render(boolean ingame) {
        VoxelGame.instance.shaderManager.disableTexturing()
        GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY)
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        super.render(ingame)

        VoxelGame.instance.shaderManager.enableTexturing()
        for(MainMenuButton b : buttons) {
            b.drawLabel()
        }
        VoxelGame.instance.shaderManager.disableTexturing()

        GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY)
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY)
        VoxelGame.instance.shaderManager.enableTexturing()
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
        private float textStartX, textStartY

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
            textStartX = bounds.x+(bounds.width/2.0f - buttonFont.getWidth(title)/2.0f) as float
            textStartY = bounds.y+(bounds.height/2.0f - buttonFont.getHeight(title)/2.0f) as float
        }

        void setPosition(int x, int y) {
            this.bounds.setLocation(x, y)
            textStartX = bounds.x+(bounds.width/2.0f - buttonFont.getWidth(title)/2.0f) as float
            textStartY = bounds.y+(bounds.height/2.0f - buttonFont.getHeight(title)/2.0f) as float
        }

        private void render(FloatBuffer vertexBuffer, FloatBuffer colorBuffer) {
            float x1 = bounds.x as float
            float x2 = bounds.x+bounds.width as float
            float y1 = bounds.y as float
            float y2 = bounds.y+bounds.height as float
            float[] bottomRight = [x2, y2] as float[]
            float[] bottomLeft = [x1, y2] as float[]
            float[] topLeft = [x1, y1] as float[]
            float[] topRight = [x2, y1] as float[]

            vertexBuffer.put(topLeft).put(bottomLeft).put(topRight);
            vertexBuffer.put(bottomRight).put(topRight).put(bottomLeft);

            float[] colors = [
                    1, 1, 1, 0.6f,
                    1, 1, 1, 0.4f,
                    1, 1, 1, 0.6f,
                    1, 1, 1, 0.4f,
                    1, 1, 1, 0.6f,
                    1, 1, 1, 0.4f
            ] as float[]
            colorBuffer.put(colors)
        }

        void drawLabel() {
            buttonFont.drawString(textStartX, textStartY, title)
        }
    }
}