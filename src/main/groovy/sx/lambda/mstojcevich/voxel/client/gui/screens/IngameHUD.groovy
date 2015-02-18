package sx.lambda.mstojcevich.voxel.client.gui.screens

import org.lwjgl.opengl.Display
import org.newdawn.slick.opengl.Texture
import org.newdawn.slick.opengl.TextureLoader
import sx.lambda.mstojcevich.voxel.VoxelGame

import static org.lwjgl.opengl.GL11.*
import sx.lambda.mstojcevich.voxel.client.gui.BufferedGUIScreen

class IngameHUD extends BufferedGUIScreen {

    private Texture icons

    private final float TEXTURE_PERCENT = 0.05f

    @Override
    public void rerender(boolean exec) {
        super.rerender(exec)

        glNewList(displayList, exec ? GL_COMPILE_AND_EXECUTE : GL_COMPILE)


        glEndList()
    }

    @Override
    public void render(boolean inGame) {
       // super.render(inGame)
        float centerX = Display.getWidth()/2f
        float centerY = Display.getHeight()/2f
        float crosshairSize = 32
        float halfCrosshairSize = crosshairSize/2f
        glEnable(GL_BLEND)
        glBlendFunc(775, 769)
        this.drawTexture(0, (int)Math.round(centerX-halfCrosshairSize), (int)Math.round(centerY-halfCrosshairSize), (int)Math.round(centerX+halfCrosshairSize), (int)Math.round(centerY+halfCrosshairSize))
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_BLEND)
    }

    @Override
    public void init() {
        super.init()
        this.icons = TextureLoader.getTexture("PNG", IngameHUD.class.getResourceAsStream("/textures/gui/icons.png"))
        this.icons.setTextureFilter(GL_NEAREST)
    }

    private void drawTexture(int number, int x, int y, int x2, int y2) {
        float u = ((number%9)*TEXTURE_PERCENT);
        float v = ((number/9)*TEXTURE_PERCENT);
        float u2 = u+TEXTURE_PERCENT-0.001f
        float v2 = v+TEXTURE_PERCENT-0.001f

        VoxelGame.getInstance().getTextureManager().bindTexture(this.icons.getTextureID())
        glBegin(GL_QUADS)
        glTexCoord2f(u, v)
        glVertex2f(x, y)
        glTexCoord2f(u, v2)
        glVertex2f(x, y2)
        glTexCoord2f(u2, v2)
        glVertex2f(x2, y2)
        glTexCoord2f(u2, v)
        glVertex2f(x2, y)
        glEnd()
    }

}
