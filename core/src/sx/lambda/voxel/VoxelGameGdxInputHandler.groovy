package sx.lambda.voxel

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import groovy.transform.CompileStatic
import sx.lambda.voxel.api.VoxelGameAPI
import sx.lambda.voxel.client.gui.screens.BlockSelectGUI
import sx.lambda.voxel.client.keybind.Keybind
import sx.lambda.voxel.net.packet.shared.PacketBreakBlock
import sx.lambda.voxel.net.packet.shared.PacketPlaceBlock

@CompileStatic
public class VoxelGameGdxInputHandler implements InputProcessor {

    private final VoxelGameClient game;

    private final List<Keybind> keybindList = Collections.synchronizedList(new ArrayList<Keybind>());

    public VoxelGameGdxInputHandler(final VoxelGameClient game) {
        this.game = game;

        // Register the default keybinds
        registerKeybind(new Keybind("voxeltest.movement.jump", "Jump", Input.Keys.SPACE, new Runnable() {
            @Override
            void run() {
                if (game.world != null) {
                    if (game.getPlayer().onGround) {
                        game.getPlayer().setYVelocity(0.11f)
                        game.getPlayer().setOnGround(false)
                    }
                }
            }
        }));
        registerKeybind(new Keybind("voxeltest.gui.selectblock", "Select Block GUI", Input.Keys.E, new Runnable() {
            @Override
            void run() {
                if (game.world != null && game.currentScreen == game.hud) {
                    game.addToGLQueue(new Runnable() {
                        @Override
                        void run() {
                            game.setCurrentScreen(new BlockSelectGUI(VoxelGameAPI.instance.getBlocksSorted(), game.hud.icons))
                        }
                    })
                }
            }
        }));
        registerKeybind(new Keybind("voxeltest.gui.back", "Back", Input.Keys.ESCAPE, new Runnable() {
            @Override
            void run() {
                if (game.world != null) {
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
        }));
    }

    @Override
    public boolean keyDown(int keycode) {
        for (Keybind kb : keybindList) {
            if (kb.key == keycode) {
                kb.press()
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        game.currentScreen.onMouseClick(button)
        switch (button) {
            case 0:
                if (game.world != null) {
                    if (game.getSelectedBlock() != null && game.currentScreen == game.hud) {
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
                if (game.world != null) {
                    if (game.getNextPlacePos() != null && game.currentScreen == game.hud) {
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
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public void registerKeybind(Keybind kb) {
        this.keybindList.add(kb)
    }

}
