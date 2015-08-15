package sx.lambda.voxel.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import org.spacehq.mc.protocol.data.game.ItemStack;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientChangeHeldItemPacket;
import org.spacehq.mc.protocol.packet.ingame.client.window.ClientCreativeInventoryActionPacket;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.api.RadixAPI;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.client.gui.GuiScreen;

import java.util.HashMap;
import java.util.Map;

public class BlockSelectGUI implements GuiScreen {

    private static final int PADDING = 4;
    private static final int SLOT_SIZE = 32 + PADDING;
    private static final int BLOCK_SIZE = 24;
    private static final int BLOCK_RENDER_OFFSET = (SLOT_SIZE - BLOCK_SIZE) / 2;

    private final Texture guiTexture;

    private FrameBuffer render;
    private Texture renderTexture;
    private boolean rendered;
    private int renderWidth, renderHeight;

    private Map<Vector2, Integer> idPositions = new HashMap<>();

    public BlockSelectGUI(Texture guiTexture) {
        this.guiTexture = guiTexture;
    }

    @Override
    public void init() {
        render = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        renderTexture = render.getColorBufferTexture();
    }

    private void rerender() {
        render.bind();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        SpriteBatch rerenderBatch = new SpriteBatch();
        rerenderBatch.begin();

        final int width = Gdx.graphics.getWidth() - PADDING*2;
        final int widthCount = width / SLOT_SIZE;
        final int startX = PADDING;
        final int startY = PADDING;

        // Looped twice to avoid texture rebinds
        int currentBlockNum = 0;
        for (Block b : RadixAPI.instance.getBlocks()) {
            if (b == null) continue;
            int yIndex = currentBlockNum / widthCount;
            int xIndex = currentBlockNum % widthCount;
            int x = startX + xIndex*SLOT_SIZE;
            int y = startY + yIndex*SLOT_SIZE;
            rerenderBatch.draw(guiTexture, x + PADDING/2, y + PADDING/2, SLOT_SIZE - PADDING, SLOT_SIZE - PADDING, 0.05f, 0, 0.1f, 0.05f);
            currentBlockNum++;
        }

        currentBlockNum = 0;
        for (Block b : RadixAPI.instance.getBlocks()) {
            if (b == null) continue;
            int yIndex = currentBlockNum / widthCount;
            int xIndex = currentBlockNum % widthCount;
            int x = startX + xIndex*SLOT_SIZE;
            int y = startY + yIndex*SLOT_SIZE;
            b.getRenderer().render2d(rerenderBatch, b.getTextureIndex(), (float) x + BLOCK_RENDER_OFFSET, (float) y + BLOCK_RENDER_OFFSET, BLOCK_SIZE);
            currentBlockNum++;
            idPositions.put(new Vector2(xIndex, yIndex), b.getID());
        }

        rerenderBatch.end();
        FrameBuffer.unbind();

        rerenderBatch.dispose();

        rendered = true;
        renderWidth = Gdx.graphics.getWidth();
        renderHeight = Gdx.graphics.getHeight();
    }

    @Override
    public void render(boolean inGame, SpriteBatch batch) {
        boolean sizeChanged = Gdx.graphics.getWidth() != renderWidth || Gdx.graphics.getHeight() != renderHeight;
        if(!rendered || sizeChanged) {
            if(sizeChanged) {
                finish();
                init();
            }
            rerender();
        }

        batch.draw(renderTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void finish() {
        render.dispose();
        renderTexture.dispose();
        rendered = false;
    }

    @Override
    public void onMouseClick(int button, boolean up) {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();

        Integer id = getBlockID(mouseX, mouseY);
        if (id != null) {
            RadixClient.getInstance().getPlayer().setItemInHand(id);

            if(RadixClient.getInstance().getMinecraftConn() != null) {
                RadixClient.getInstance().getMinecraftConn().getClient().getSession().send(
                        new ClientCreativeInventoryActionPacket(36 /* Bottom left of hot bar */, new ItemStack(id, 1))
                );
                RadixClient.getInstance().getMinecraftConn().getClient().getSession().send(
                        new ClientChangeHeldItemPacket(0)
                );
            }
        }
    }

    @Override
    public void mouseMoved(int x, int y) {}

    @Override
    public void keyTyped(char c) {

    }

    /**
     * Get the block ID at a certain position on the screen
     */
    private Integer getBlockID(int x, int y) {
        x = (x - PADDING) / SLOT_SIZE;
        y = (y - PADDING) / SLOT_SIZE;
        return idPositions.get(new Vector2(x, y));
    }

}
