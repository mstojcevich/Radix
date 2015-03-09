package sx.lambda.mstojcevich.voxel.tasks

import groovy.transform.CompileStatic
import org.lwjgl.input.Mouse
import sx.lambda.mstojcevich.voxel.block.Block
import sx.lambda.mstojcevich.voxel.net.packet.shared.PacketBreakBlock
import sx.lambda.mstojcevich.voxel.net.packet.shared.PacketPlaceBlock
import sx.lambda.mstojcevich.voxel.VoxelGame

import static org.lwjgl.input.Keyboard.KEY_ESCAPE
import static org.lwjgl.input.Keyboard.KEY_SPACE
import static org.lwjgl.input.Keyboard.KEY_UP
import static org.lwjgl.input.Keyboard.getEventKey
import static org.lwjgl.input.Keyboard.getEventKeyState
import static org.lwjgl.input.Keyboard.next

@CompileStatic
class InputHandler implements RepeatedTask {

    private final VoxelGame game

    public InputHandler(VoxelGame game) {
        this.game = game
    }

    @Override
    String getIdentifier() {
        return "Input Handler"
    }

    @Override
    void run() {
        try {
            while (!game.isDone()) {
                while (next()) {
                    if (getEventKeyState()) { //Press down, not release
                        int key = getEventKey()
                        switch (key) {
                            case KEY_SPACE:
                                if (game.getPlayer().onGround) {
                                    game.getPlayer().setYVelocity(0.11f)
                                    game.getPlayer().setOnGround(false)
                                }
                                break;
                            case KEY_UP:
                                game.getPlayer().setItemInHand Block.values()[(game.getPlayer().getItemInHand().ordinal() + 1) % Block.values().size()]
                                game.addToGLQueue(new Runnable() {
                                    @Override
                                    public void run() {
                                        game.rerenderHud()
                                    }
                                })
                                break;
                            case KEY_ESCAPE:
                                game.startShutdown()
                                break
                            default:
                                break;
                        }

                    }
                }
                while (Mouse.next()) {
                    if (Mouse.getEventButtonState()) {
                        int button = Mouse.getEventButton()
                        switch (button) {
                            case 0:
                                if (game.getSelectedBlock() != null) {
                                    if (VoxelGame.instance.isRemote() && VoxelGame.instance.serverChanCtx != null) {
                                        VoxelGame.instance.serverChanCtx.writeAndFlush(new PacketBreakBlock(
                                                game.getSelectedBlock()))
                                    } else {
                                        game.getWorld().removeBlock(game.getSelectedBlock())
                                    }
                                }
                                break;
                            case 1:
                                if (game.getNextPlacePos() != null) {
                                    if (VoxelGame.instance.isRemote() && VoxelGame.instance.serverChanCtx != null) {
                                        VoxelGame.instance.serverChanCtx.writeAndFlush(new PacketPlaceBlock(
                                                game.getNextPlacePos(),
                                                game.getPlayer().getItemInHand()
                                        ));
                                    } else {
                                        game.getWorld().addBlock(game.getPlayer().getItemInHand(), game.getNextPlacePos())
                                    }
                                }
                                break
                            default:
                                break;
                        }
                    }
                }
                sleep(10)
            }
        } catch (Exception e) {
            VoxelGame.instance.handleCriticalException(e)
        }
    }

}
