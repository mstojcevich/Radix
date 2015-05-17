package sx.lambda.voxel.client.gui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.api.BuiltInBlockIds
import sx.lambda.voxel.api.VoxelGameAPI
import sx.lambda.voxel.client.gui.BufferedGUIScreen

import static com.badlogic.gdx.graphics.GL20.*

@CompileStatic
class IngameHUD extends BufferedGUIScreen {

    private SpriteBatch guiBatcher
    private Texture icons

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
        guiBatcher.setProjectionMatrix(VoxelGameClient.instance.hudCamera.combined)

        VoxelGameClient.instance.guiShader.enableTexturing()

        Gdx.gl.glEnable(GL_BLEND)

        guiBatcher.begin()
        int blockInHead = VoxelGameClient.instance.player.getBlockInHead(VoxelGameClient.instance.world)
        if(blockInHead != null) {
            switch(blockInHead) {
                case BuiltInBlockIds.WATER_ID:
                    VoxelGameAPI.instance.getBlockByID(blockInHead).getRenderer().render2d(guiBatcher, 0, 0, Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()))
                    // TODO add ability to set transparency with a spritebatcher so that we can render transparent overlay for the water
                    break;
            }
        }

        VoxelGameAPI.instance.getBlockByID(VoxelGameClient.instance.player.itemInHand).renderer.render2d(guiBatcher, 0, 0, 50);
        guiBatcher.end()

        // Draw crosshair (after guiBatcher render because it needs to render with its own blending mode
        float centerX = Gdx.graphics.getWidth()/2f as float
        float centerY = Gdx.graphics.getHeight()/2f as float
        float crosshairSize = 32
        float halfCrosshairSize = crosshairSize/2f as float
        guiBatcher.setBlendFunction(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_COLOR)
        guiBatcher.begin()
        this.drawTexture(0, (int)Math.round(centerX-halfCrosshairSize), (int)Math.round(centerY-halfCrosshairSize), (int)Math.round(centerX+halfCrosshairSize), (int)Math.round(centerY+halfCrosshairSize))
        guiBatcher.end()
        guiBatcher.setBlendFunction(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glDisable(GL_BLEND)
    }

    @Override
    void onMouseClick(int clickType) {}

    @Override
    public void init() {
        super.init()
        this.icons = new Texture(Gdx.files.internal("textures/gui/icons.png"))
        this.guiBatcher = new SpriteBatch()
    }

    private void drawTexture(int number, int x, int y, int x2, int y2) {
        float u = ((number%9)*TEXTURE_PERCENT);
        float v = ((number/9)*TEXTURE_PERCENT);
        float u2 = u+TEXTURE_PERCENT-0.001f
        float v2 = v+TEXTURE_PERCENT-0.001f

        this.guiBatcher.draw(icons, x, y, x2, y2, u, v, u2, v2)
    }

    public Texture getIcons() { icons }

}
