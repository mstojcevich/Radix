package sx.lambda.mstojcevich.voxel.client.gui.screens

import groovy.transform.CompileStatic
import org.lwjgl.opengl.Display
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.block.Block
import sx.lambda.mstojcevich.voxel.block.NormalBlockRenderer
import sx.lambda.mstojcevich.voxel.util.gl.SpriteBatcher
import sx.lambda.mstojcevich.voxel.util.gl.TextureLoader

import static org.lwjgl.opengl.GL11.*
import sx.lambda.mstojcevich.voxel.client.gui.BufferedGUIScreen

@CompileStatic
class IngameHUD extends BufferedGUIScreen {

    private SpriteBatcher guiBatcher
    private SpriteBatcher blockBatcher
    private int icons

    private final float TEXTURE_PERCENT = 0.05f

    public IngameHUD() {
        super()
    }

    @Override
    public void rerender(boolean exec) {
        super.rerender(exec)
    }

    @Override
    public void render(boolean inGame) {
        super.render(inGame)
        VoxelGame.instance.guiShader.enableTexturing()
        VoxelGame.instance.guiShader.disableColors()

        glEnable(GL_BLEND)

        Block blockInHead = VoxelGame.instance.player.getBlockInHead(VoxelGame.instance.world)
        if(blockInHead != null) {
            switch(blockInHead) {
                case Block.WATER:
                    Block.WATER.getRenderer().render2d(blockBatcher, 0, 0, Math.max(Display.getWidth(), Display.getHeight()))
                    // TODO add ability to set transparency with a spritebatcher so that we can render transparent overlay for the water
                    break;
            }
        }

        VoxelGame.instance.player.itemInHand.renderer.render2d(blockBatcher, 0, 0, 50);

        blockBatcher.render(NormalBlockRenderer.blockMap)

        // Draw crosshair (after guiBatcher render because it needs to render with its own blending mode
        float centerX = Display.getWidth()/2f as float
        float centerY = Display.getHeight()/2f as float
        float crosshairSize = 32
        float halfCrosshairSize = crosshairSize/2f as float
        glBlendFunc(775, 769)
        this.drawTexture(0, (int)Math.round(centerX-halfCrosshairSize), (int)Math.round(centerY-halfCrosshairSize), (int)Math.round(centerX+halfCrosshairSize), (int)Math.round(centerY+halfCrosshairSize))
        guiBatcher.render(icons)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_BLEND)
    }

    @Override
    void onMouseClick(int clickType) {}

    @Override
    public void init() {
        super.init()
        this.icons = TextureLoader.loadTexture(IngameHUD.class.getResourceAsStream("/textures/gui/icons.png"), VoxelGame.instance.textureManager)
        this.guiBatcher = new SpriteBatcher(VoxelGame.instance.textureManager)
        this.guiBatcher.init()
        this.blockBatcher = new SpriteBatcher(VoxelGame.instance.textureManager)
        this.blockBatcher.init()
    }

    private void drawTexture(int number, int x, int y, int x2, int y2) {
        float u = ((number%9)*TEXTURE_PERCENT);
        float v = ((number/9)*TEXTURE_PERCENT);
        float u2 = u+TEXTURE_PERCENT-0.001f
        float v2 = v+TEXTURE_PERCENT-0.001f

        this.guiBatcher.drawTexturedRect(x, y, x2, y2, u, v, u2, v2)
    }

}
