package sx.lambda.voxel.client.gui.screens

import com.badlogic.gdx.Gdx
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.api.BuiltInBlockIds
import sx.lambda.voxel.block.NormalBlockRenderer
import sx.lambda.voxel.util.gl.TextureLoader
import sx.lambda.voxel.api.VoxelGameAPI
import sx.lambda.voxel.client.gui.BufferedGUIScreen
import sx.lambda.voxel.util.gl.SpriteBatcher

import static com.badlogic.gdx.graphics.GL20.*

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
        VoxelGameClient.instance.guiShader.enableTexturing()

        Gdx.gl.glEnable(GL_BLEND)

        int blockInHead = VoxelGameClient.instance.player.getBlockInHead(VoxelGameClient.instance.world)
        if(blockInHead != null) {
            switch(blockInHead) {
                case BuiltInBlockIds.WATER_ID:
                    VoxelGameAPI.instance.getBlockByID(blockInHead).getRenderer().render2d(blockBatcher, 0, 0, Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()))
                    // TODO add ability to set transparency with a spritebatcher so that we can render transparent overlay for the water
                    break;
            }
        }

        VoxelGameAPI.instance.getBlockByID(VoxelGameClient.instance.player.itemInHand).renderer.render2d(blockBatcher, 0, 0, 50);

        blockBatcher.render(NormalBlockRenderer.blockMap)

        // Draw crosshair (after guiBatcher render because it needs to render with its own blending mode
        float centerX = Gdx.graphics.getWidth()/2f as float
        float centerY = Gdx.graphics.getHeight()/2f as float
        float crosshairSize = 32
        float halfCrosshairSize = crosshairSize/2f as float
        Gdx.gl.glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_COLOR)
        this.drawTexture(0, (int)Math.round(centerX-halfCrosshairSize), (int)Math.round(centerY-halfCrosshairSize), (int)Math.round(centerX+halfCrosshairSize), (int)Math.round(centerY+halfCrosshairSize))
        guiBatcher.render(icons)
        Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glDisable(GL_BLEND)
    }

    @Override
    void onMouseClick(int clickType) {}

    @Override
    public void init() {
        super.init()
        this.icons = TextureLoader.loadTexture(Gdx.files.internal("textures/gui/icons.png").read(), VoxelGameClient.instance.textureManager)
        this.guiBatcher = new SpriteBatcher(VoxelGameClient.instance.textureManager)
        this.guiBatcher.init()
        this.blockBatcher = new SpriteBatcher(VoxelGameClient.instance.textureManager)
        this.blockBatcher.init()
    }

    private void drawTexture(int number, int x, int y, int x2, int y2) {
        float u = ((number%9)*TEXTURE_PERCENT);
        float v = ((number/9)*TEXTURE_PERCENT);
        float u2 = u+TEXTURE_PERCENT-0.001f
        float v2 = v+TEXTURE_PERCENT-0.001f

        this.guiBatcher.drawTexturedRect(x, y, x2, y2, u, v, u2, v2)
    }

    public int getIcons() { icons }

}
