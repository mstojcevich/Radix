package sx.lambda.voxel.client.gui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.block.Block
import sx.lambda.voxel.block.NormalBlockRenderer
import sx.lambda.voxel.client.gui.GuiScreen
import sx.lambda.voxel.texture.TextureManager
import sx.lambda.voxel.util.gl.SpriteBatcher

import java.awt.*

@CompileStatic
class BlockSelectGUI implements GuiScreen {
    private final SpriteBatcher batcher
    private final SpriteBatcher blockBatcher
    private final Block[] blocks
    private final int guiTexture
    private SpriteBatcher.StaticRender render, blockRender

    private final int PADDING = 4
    private final int SLOT_SIZE = 32 + PADDING
    private final int BLOCK_SIZE = 24

    private Map<Point, Integer> idPositions = new HashMap<>()

    public BlockSelectGUI(TextureManager manager, Block[] blocks, int guiTexture) {
        this.batcher = new SpriteBatcher(manager)
        this.blockBatcher = new SpriteBatcher(manager)
        this.guiTexture = guiTexture
        this.blocks = blocks
    }

    @Override
    void init() {
        batcher.init()
        blockBatcher.init()
    }

    @Override
    void render(boolean inGame) {
        if(render == null) {
            int currentBlockNum = 0
            final int WIDTH = Gdx.graphics.width, HEIGHT = Gdx.graphics.height
            final int USABLE_WIDTH = WIDTH - PADDING*2, USABLE_HEIGHT = HEIGHT - PADDING*2
            final int BLOCK_RENDER_OFFSET = (int)((SLOT_SIZE - BLOCK_SIZE)/2)
            for(Block b : blocks) {
                if(b == null)continue;
                final int x = PADDING + (currentBlockNum * SLOT_SIZE) % USABLE_WIDTH
                final int y = PADDING + (int)((currentBlockNum * SLOT_SIZE) / USABLE_WIDTH)

                batcher.drawTexturedRect(x, y, x+SLOT_SIZE-PADDING, y+SLOT_SIZE-PADDING, 0.05f, 0, 0.1f, 0.05f)
                b.renderer.render2d(blockBatcher, x+BLOCK_RENDER_OFFSET-2, y+BLOCK_RENDER_OFFSET-2, BLOCK_SIZE)

                currentBlockNum++

                idPositions.put(new Point(x, y), b.ID)
            }
            render = batcher.renderStatic(guiTexture)
            blockRender = blockBatcher.renderStatic(NormalBlockRenderer.blockMap)
        }

        VoxelGameClient.instance.guiShader.enableTexturing()
        Gdx.gl.glEnable(GL20.GL_TEXTURE_2D)
        render.render()
        blockRender.render()
        Gdx.gl.glDisable(GL20.GL_TEXTURE_2D)
    }

    @Override
    void finish() {
        batcher.cleanup()
        blockBatcher.cleanup()
        render.destroy()
        blockRender.destroy()
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
