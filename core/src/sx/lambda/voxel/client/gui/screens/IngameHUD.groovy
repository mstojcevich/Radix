package sx.lambda.voxel.client.gui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.api.BuiltInBlockIds
import sx.lambda.voxel.api.VoxelGameAPI
import sx.lambda.voxel.client.gui.GuiScreen

import static com.badlogic.gdx.graphics.GL20.GL_BLEND

@CompileStatic
class IngameHUD implements GuiScreen {

    private Texture icons

    private final float TEXTURE_PERCENT = 0.05f

    public IngameHUD() {
        super()
    }

    @Override
    public void render(boolean inGame, SpriteBatch guiBatch) {
        Gdx.gl.glEnable(GL_BLEND)

        int blockInHead = VoxelGameClient.instance.player.getBlockInHead(VoxelGameClient.instance.world)
        if (blockInHead != null) {
            switch (blockInHead) {
                case BuiltInBlockIds.WATER_ID:
                    VoxelGameAPI.instance.getBlockByID(blockInHead).getRenderer().render2d(guiBatch, 0, 0, Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()))
                    // TODO add ability to set transparency with a spritebatcher so that we can render transparent overlay for the water
                    break;
            }
        }

        VoxelGameAPI.instance.getBlockByID(VoxelGameClient.instance.player.itemInHand).renderer.render2d(guiBatch, 0, 0, 50);

        // Draw crosshair (after guiBatcher render because it needs to render with its own blending mode
        float centerX = Gdx.graphics.getWidth() / 2f as float
        float centerY = Gdx.graphics.getHeight() / 2f as float
        int crosshairSize = 32
        float halfCrosshairSize = crosshairSize / 2f as float
        this.drawTexture(guiBatch, 0, (int) Math.round(centerX - halfCrosshairSize), (int) Math.round(centerY + halfCrosshairSize), crosshairSize, crosshairSize)
    }

    @Override
    void finish() {
        icons.dispose()
    }

    @Override
    void onMouseClick(int clickType) {}

    @Override
    public void init() {
        this.icons = new Texture(Gdx.files.internal("textures/gui/icons.png"))
    }

    private void drawTexture(SpriteBatch batch, int number, int x, int y, int width, int height) {
        float u = ((number % 9) * TEXTURE_PERCENT);
        float v = ((number / 9) * TEXTURE_PERCENT);
        float u2 = u + TEXTURE_PERCENT - 0.001f
        float v2 = v + TEXTURE_PERCENT - 0.001f

        batch.draw(icons, x, y, width, height, u, v, u2, v2)
    }

    public Texture getIcons() { icons }

}
