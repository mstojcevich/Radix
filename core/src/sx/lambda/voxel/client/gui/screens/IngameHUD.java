package sx.lambda.voxel.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.api.RadixAPI;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.client.gui.GuiScreen;

public class IngameHUD implements GuiScreen {

    private final float TEXTURE_PERCENT = 0.05f;
    private Texture icons;

    public IngameHUD() {
        super();
    }

    @Override
    public void render(boolean inGame, SpriteBatch guiBatch) {
        int blockInHead = RadixClient.getInstance().getPlayer().getBlockInHead(RadixClient.getInstance().getWorld());
        switch (blockInHead) {
            case BuiltInBlockIds.WATER_FLOW_ID:
            case BuiltInBlockIds.WATER_ID:
                guiBatch.setColor(1, 1, 1, 0.6f);
                Block bl = RadixAPI.instance.getBlock(blockInHead);
                bl.getRenderer().render2d(guiBatch, bl.getTextureIndex(), 0, 0, Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
                guiBatch.setColor(1, 1, 1, 1);
                break;
        }

        Block handBlock = RadixAPI.instance.getBlock(RadixClient.getInstance().getPlayer().getItemInHand());
        handBlock.getRenderer().render2d(guiBatch, handBlock.getTextureIndex(), 0, 0, 50);

        // Draw crosshair (after guiBatcher render because it needs to render with its own blending mode
        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;
        int crosshairSize = 32;
        float halfCrosshairSize = crosshairSize / 2f;
        this.drawTexture(guiBatch, 0, MathUtils.round(centerX - halfCrosshairSize), MathUtils.round(centerY - halfCrosshairSize), crosshairSize, crosshairSize);

        RadixClient.getInstance().getChatGUI().renderHud(guiBatch);
    }

    @Override
    public void finish() {
        icons.dispose();
    }

    @Override
    public void onMouseClick(int clickType, boolean up) {
    }

    @Override
    public void mouseMoved(int x, int y) {
    }

    @Override
    public void keyTyped(char c) {
    }

    @Override
    public void init() {
        this.icons = new Texture(Gdx.files.internal("textures/gui/icons.png"));
    }

    private void drawTexture(SpriteBatch batch, int number, int x, int y, int width, int height) {
        float u = ((number % 9) * TEXTURE_PERCENT);
        float v = ((number / 9) * TEXTURE_PERCENT);
        float u2 = u + TEXTURE_PERCENT - 0.001f;
        float v2 = v + TEXTURE_PERCENT - 0.001f;

        batch.draw(icons, x, y, width, height, u, v, u2, v2);
    }

    public Texture getIcons() {
        return icons;
    }

}
