package sx.lambda.voxel.client.gui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.block.Block
import sx.lambda.voxel.client.gui.GuiScreen

@CompileStatic
class BlockSelectGUI implements GuiScreen {
    private final Block[] blocks
    private final Texture guiTexture

    private final int PADDING = 4
    private final int SLOT_SIZE = 32 + PADDING
    private final int BLOCK_SIZE = 24

    private Map<Vector2, Integer> idPositions = new HashMap<>()

    public BlockSelectGUI(Block[] blocks, Texture guiTexture) {
        this.guiTexture = guiTexture
        this.blocks = blocks
    }

    @Override
    void init() {
    }

    @Override
    void render(boolean inGame, SpriteBatch batch) {
        int currentBlockNum = 0
        final int WIDTH = Gdx.graphics.width, HEIGHT = Gdx.graphics.height
        final int USABLE_WIDTH = WIDTH - PADDING * 2, USABLE_HEIGHT = HEIGHT - PADDING * 2
        final int BLOCK_RENDER_OFFSET = (int) ((SLOT_SIZE - BLOCK_SIZE) / 2)
        // Looped twice to avoid texture rebinds
        for(Block b : blocks) {
            if (b == null) continue;
            final int x = PADDING + (currentBlockNum * SLOT_SIZE) % USABLE_WIDTH
            final int regularY = PADDING + (int) ((currentBlockNum * SLOT_SIZE) / USABLE_WIDTH)
            final int y = Gdx.graphics.height - (regularY + SLOT_SIZE)
            batch.draw(guiTexture, x, y, SLOT_SIZE - PADDING, SLOT_SIZE - PADDING, 0.05f, 0, 0.1f, 0.05f)
        }
        for (Block b : blocks) {
            if (b == null) continue;
            final int x = PADDING + (currentBlockNum * SLOT_SIZE) % USABLE_WIDTH
            final int regularY = PADDING + (int) ((currentBlockNum * SLOT_SIZE) / USABLE_WIDTH)
            final int y = Gdx.graphics.height - (regularY + SLOT_SIZE)
            b.renderer.render2d(batch, x + BLOCK_RENDER_OFFSET - 2, y + BLOCK_RENDER_OFFSET - 2, BLOCK_SIZE)
            currentBlockNum++
            idPositions.put(new Vector2(x, regularY), b.ID)
        }
    }

    @Override
    void finish() {
    }

    @Override
    void onMouseClick(int button) {
        int mouseX = Gdx.input.getX()
        int mouseY = Gdx.input.getY()

        Integer id = getBlockID(mouseX, mouseY)
        if (id != null) {
            VoxelGameClient.instance.player.setItemInHand(id)
        }
    }

    /**
     * Get the block ID at a certain position on the screen
     */
    private Integer getBlockID(int x, int y) {
        x = x - (x % SLOT_SIZE) + PADDING
        y = y - (y % SLOT_SIZE) + PADDING
        println(x)
        println(y)
        return idPositions.get(new Vector2(x, y))
    }

}
