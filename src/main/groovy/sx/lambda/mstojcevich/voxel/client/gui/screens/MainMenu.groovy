package sx.lambda.mstojcevich.voxel.client.gui.screens

import groovy.transform.CompileStatic
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import sx.lambda.mstojcevich.voxel.client.gui.VboBufferedGuiScreen

import java.awt.Rectangle
import java.nio.FloatBuffer
import java.nio.IntBuffer;

@CompileStatic
public class MainMenu extends VboBufferedGuiScreen {

    private static final int VERTICES_PER_BUTTON = 4
    private static final int TARGET_BUTTON_SIZE = 100

    private final MainMenuButton[] buttons

    private int backgroundVboVertices
    private int backgroundVboColors

    private boolean renderedBgBefore

    public MainMenu() {
        buttons = [
                new MainMenuButton(this, "Singleplayer", {println("Singleplayer pressed!")}, TARGET_BUTTON_SIZE),
                new MainMenuButton(this, "Multiplayer", {println("Multiplayer pressed!")}, TARGET_BUTTON_SIZE),
        ]
    }

    private void resize() {
        int dWidth = Display.getWidth()
        int buttonsPerRow = (int)(dWidth / TARGET_BUTTON_SIZE)

        int currentButtonNum = 0
        for(MainMenuButton button : buttons) {
            int btnX = (currentButtonNum % buttonsPerRow)*TARGET_BUTTON_SIZE
            int btnY = (int)(currentButtonNum / buttonsPerRow)*TARGET_BUTTON_SIZE
            button.setPosition(btnX, btnY)
            currentButtonNum++
        }
    }

    @Override
    public void init() {
        resize()

        super.init()
        IntBuffer buffer = BufferUtils.createIntBuffer(2);
        GL15.glGenBuffers(buffer);
        backgroundVboVertices = buffer.get(0);
        backgroundVboColors = buffer.get(1);
    }

    @Override
    protected void rerender() {
        int numVertices = buttons.length*VERTICES_PER_BUTTON
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(numVertices*2)
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(numVertices*4)
        for(MainMenuButton button : buttons) {
            button.render(vertexBuffer, colorBuffer)
        }
        renderVbo(vertexBuffer, colorBuffer)

        rerenderBackground()
    }

    private void renderBackground() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, backgroundVboVertices)
        GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, backgroundVboColors)
        GL11.glColorPointer(3, GL11.GL_FLOAT, 0, 0)

        GL11.glDrawArrays(GL11.GL_QUADS, 0, 4)
    }

    private void rerenderBackground() {
        FloatBuffer vertices = BufferUtils.createFloatBuffer(4*3)
        FloatBuffer colors = BufferUtils.createFloatBuffer(4*4)

        int x1 = 0, y1 = 0, x2 = Display.getWidth(), y2 = Display.getHeight()

        float[] vts = [
                x1, y1,
                x2, y1,
                x2, y2,
                x1, y2
        ]
        float [] cls = [
                0, 0, 0, 1,
                0, 0, 0, 1,
                0.6f, 0.6f,0.6f, 1
        ]
        vertices.put(vts)
        colors.put(cls)

        if(!renderedBgBefore) {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, backgroundVboVertices)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW)
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, backgroundVboColors)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colors, GL15.GL_STATIC_DRAW)
        } else {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, backgroundVboVertices)
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertices)
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, backgroundVboColors)
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, colors)
        }

        renderedBgBefore = true
    }

    @Override
    public void render(boolean ingame) {
        if(!renderedBefore) {
            if(!initialized) {
                this.init();
            }
            rerender();
            renderedBefore = true;
        }

        this.renderBackground()
        super.render(ingame)
    }

    private static class MainMenuButton {
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

        private void render(FloatBuffer vertexBuffer, FloatBuffer colorBuffer) {
            float[] points = [
                    bounds.x, bounds.y,
                    bounds.x+bounds.width, bounds.y,
                    bounds.x+bounds.width, bounds.y+bounds.height,
                    bounds.x, bounds.y+bounds.height
            ] as float[]
            float[] colors = [
                    1, 1, 1, 0.6f,
                    1, 1, 1, 0.6f,
                    1, 1, 1, 0.4f,
                    1, 1, 1, 0.4f
            ] as float[]
            vertexBuffer.put(points)
            colorBuffer.put(colors)
        }
    }
}
