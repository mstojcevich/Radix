package sx.lambda.mstojcevich.voxel.tasks

import groovy.transform.CompileStatic
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import sx.lambda.mstojcevich.voxel.block.Block
import sx.lambda.mstojcevich.voxel.block.NormalBlockRenderer
import sx.lambda.mstojcevich.voxel.client.gui.screens.BlockSelectGUI
import sx.lambda.mstojcevich.voxel.client.keybind.Keybind
import sx.lambda.mstojcevich.voxel.net.packet.shared.PacketBreakBlock
import sx.lambda.mstojcevich.voxel.net.packet.shared.PacketPlaceBlock
import sx.lambda.mstojcevich.voxel.VoxelGame

import static org.lwjgl.input.Keyboard.KEY_E
import static org.lwjgl.input.Keyboard.KEY_ESCAPE
import static org.lwjgl.input.Keyboard.KEY_SPACE
import static org.lwjgl.input.Keyboard.KEY_UP
import static org.lwjgl.input.Keyboard.getEventKey
import static org.lwjgl.input.Keyboard.getEventKeyState
import static org.lwjgl.input.Keyboard.isKeyDown
import static org.lwjgl.input.Keyboard.next

@CompileStatic
class InputHandler implements RepeatedTask {

    private final VoxelGame game

    private final List<Keybind> keybindList = Collections.synchronizedList(new ArrayList<Keybind>())

    public InputHandler(VoxelGame game) {
        this.game = game

        // Register the default keybinds
        registerKeybind(new Keybind("voxeltest.movement.jump", "Jump", KEY_SPACE, new Runnable() {
            @Override
            void run() {
                if(game.world != null) {
                    if (game.getPlayer().onGround) {
                        game.getPlayer().setYVelocity(0.11f)
                        game.getPlayer().setOnGround(false)
                    }
                }
            }
        }))
        registerKeybind(new Keybind("voxeltest.gui.selectblock", "Select Block GUI", KEY_E, new Runnable() {
            @Override
            void run() {
                if(game.world != null  && game.currentScreen == game.hud) {
                    game.addToGLQueue(new Runnable() {
                        @Override
                        void run() {
                            game.setCurrentScreen(new BlockSelectGUI(game.textureManager, Block.values(), game.hud.icons))
                        }
                    })
                }
            }
        }))
        registerKeybind(new Keybind("voxeltest.gui.back", "Back", KEY_ESCAPE, new Runnable() {
            @Override
            void run() {
                if(game.world != null) {
                    if (game.currentScreen != game.hud) {
                        game.addToGLQueue(new Runnable() {
                            @Override
                            void run() {
                                game.setCurrentScreen(game.hud)
                            }
                        })
                    } else {
                        game.exitWorld() // TODO show ingame options
                    }
                }
            }
        }))
        registerKeybind(new Keybind("voxeltest.game.nextitem", "Next Item", KEY_UP, new Runnable() {
            @Override
            void run() {
                if(game.world != null) {
                    game.getPlayer().setItemInHand Block.values()[(game.getPlayer().getItemInHand().ordinal() + 1) % Block.values().size()]
                    game.addToGLQueue(new Runnable() {
                        @Override
                        public void run() {
                            game.hud.rerender(false)
                        }
                    })
                }
            }
        }))
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

                        for(Keybind kb : keybindList) {
                            if(kb.key == key) {
                                kb.press()
                            }
                        }
                    }
                }
                for(Keybind kb : keybindList) {
                    if(kb.shouldRepeat()) {
                        if(isKeyDown(kb.key)) {
                            kb.press()
                        }
                    }
                }
                while (Mouse.isCreated() && Mouse.next()) {
                    if (Mouse.getEventButtonState()) {
                        int button = Mouse.getEventButton()
                        game.currentScreen.onMouseClick(button)
                        switch (button) {
                            case 0:
                                if(game.world != null) {
                                    if (game.getSelectedBlock() != null) {
                                        if (game.isRemote() && game.serverChanCtx != null) {
                                            game.serverChanCtx.writeAndFlush(new PacketBreakBlock(
                                                    game.getSelectedBlock()))
                                        } else {
                                            game.getWorld().removeBlock(game.getSelectedBlock())
                                        }
                                    }
                                }
                                break;
                            case 1:
                                if(game.world != null) {
                                    if (game.getNextPlacePos() != null) {
                                        if (game.isRemote() && game.serverChanCtx != null) {
                                            game.serverChanCtx.writeAndFlush(new PacketPlaceBlock(
                                                    game.getNextPlacePos(),
                                                    game.getPlayer().getItemInHand()
                                            ));
                                        } else {
                                            game.getWorld().addBlock(game.getPlayer().getItemInHand(), game.getNextPlacePos())
                                        }
                                    }
                                }
                                break
                            default:
                                break
                        }
                    }
                }
                if(Keyboard.isCreated()) {
                    if (isKeyDown(KEY_SPACE) && game.player.getBlockInFeet(game.world) == Block.WATER) {
                        game.player.setYVelocity(0.02f);
                    }
                }
                sleep(10)
            }
        } catch (Exception e) {
            game.handleCriticalException(e)
        }
    }

    public void registerKeybind(Keybind kb) {
        this.keybindList.add(kb)
    }

}
