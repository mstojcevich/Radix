package sx.lambda.voxel.client.gui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.block.Block
import sx.lambda.voxel.client.gui.GuiScreen

import java.awt.*

@CompileStatic
class BlockSelectGUI implements GuiScreen {
    private final SpriteBatch batcher
    private final Block[] blocks
    private final Texture guiTexture

    private final int PADDING = 4
    private final int SLOT_SIZE = 32 + PADDING
    private final int BLOCK_SIZE = 24

    private Map<Point, Integer> idPositions = new HashMap<>()

    public BlockSelectGUI(Block[] blocks, Texture guiTexture) {
        this.batcher = new SpriteBatch()
        batcher.setTransformMatrix(VoxelGameClient.instance.hudCamera.combined)
        this.guiTexture = guiTexture
        this.blocks = blocks
    }

    @Override
    void init() {
    }

    @Override
    void render(boolean inGame) {
        int currentBlockNum = 0
        final int WIDTH = Gdx.graphics.width, HEIGHT = Gdx.graphics.height
        final int USABLE_WIDTH = WIDTH - PADDING*2, USABLE_HEIGHT = HEIGHT - PADDING*2
        final int BLOCK_RENDER_OFFSET = (int)((SLOT_SIZE - BLOCK_SIZE)/2)
        batcher.begin()
        for(Block b : blocks) {
            if(b == null)continue;
            final int x = PADDING + (currentBlockNum * SLOT_SIZE) % USABLE_WIDTH
            final int y = PADDING + (int)((currentBlockNum * SLOT_SIZE) / USABLE_WIDTH)
            batcher.draw(guiTexture, x, y, x+SLOT_SIZE-PADDING, y+SLOT_SIZE-PADDING, 0.05f, 0, 0.1f, 0.05f)
            b.renderer.render2d(batcher, x+BLOCK_RENDER_OFFSET-2, y+BLOCK_RENDER_OFFSET-2, BLOCK_SIZE)
            currentBlockNum++
            idPositions.put(new Point(x, y), b.ID)
        }
        batcher.end()
    }

    @Override
    void finish() {
        batcher.dispose()
    }

    @Override
    void onMouseClick(int button) {
        int mouseX = Gdx.input.getX()
        int mouseY = Gdx.graphics.getHeight()-Gdx.input.getY()

        Integer id = getBlockID(mouseX, mouseY)
        if(id != null) {
            VoxelGameClient.instance.player.setItemInHand(id)
        }
    }

    /**
     * Get the block ID at a certain position on the screen
     */
    private Integer getBlockID(int x, int y) {
        x = x - (x % SLOT_SIZE) + PADDING
        y = y - (y % SLOT_SIZE) + PADDING
        return idPositions.get(new Point(x, y))
    }

}
