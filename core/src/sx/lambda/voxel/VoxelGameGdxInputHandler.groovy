package sx.lambda.voxel

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import groovy.transform.CompileStatic
import sx.lambda.voxel.api.VoxelGameAPI
import sx.lambda.voxel.client.gui.screens.BlockSelectGUI
import sx.lambda.voxel.client.keybind.Keybind

@CompileStatic
public class VoxelGameGdxInputHandler implements InputProcessor {

    private static final float mouseSensitivity = 0.03f //TODO Config - allow changeable mouse sensitivity

    private final VoxelGameClient game;

    private final List<Keybind> keybindList = Collections.synchronizedList(new ArrayList<Keybind>());

    private int lastMouseX = -Integer.MAX_VALUE, lastMouseY = -Integer.MAX_VALUE;

    public VoxelGameGdxInputHandler(final VoxelGameClient game) {
        this.game = game;

        // Register the default keybinds
        registerKeybind(new Keybind("voxeltest.movement.jump", "Jump", Input.Keys.SPACE, new Runnable() {
            @Override
            void run() {
                if (game.world != null && game.currentScreen == game.hud) {
                    game.movementHandler.jump();
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
        registerKeybind(new Keybind("voxeltest.gui.chat", "Open Chat", Input.Keys.T, new Runnable() {
            @Override
            void run() {
                if (game.world != null) {
                    if (game.currentScreen == game.hud) {
                        game.addToGLQueue(new Runnable() {
                            @Override
                            void run() {
                                game.setCurrentScreen(game.chatGUI)
                            }
                        })
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
        VoxelGameClient.instance.currentScreen.keyTyped(character)
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        game.currentScreen.onMouseClick(button)
        switch (button) {
            case 0:
                if (game.world != null) {
                    game.breakBlock();
                }
                break;
            case 1:
                if (game.world != null) {
                    game.placeBlock();
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
        if(!game.onAndroid())
            updateRotation(screenX, screenY)

        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if(!game.onAndroid())
            updateRotation(screenX, screenY)

        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public void registerKeybind(Keybind kb) {
        this.keybindList.add(kb)
    }

    private void updateRotation(int newMouseX, int newMouseY) {
        if(lastMouseX > -Integer.MAX_VALUE) {
            if ((game.world != null || game.player != null) && (game.currentScreen == null || game.currentScreen == game.hud)) {
                int deltaX = newMouseX - lastMouseX
                int deltaY = newMouseY - lastMouseY
                float deltaYaw = deltaX * mouseSensitivity;
                float deltaPitch = -deltaY * mouseSensitivity;

                float newPitch = Math.abs(game.player.rotation.pitch + deltaPitch);
                if (newPitch > 90) {
                    deltaPitch = 0;
                }
                game.getPlayer().getRotation().offset(deltaPitch, deltaYaw);

                if (Math.abs(deltaPitch) > 0 || Math.abs(deltaYaw) > 0) {
                    game.player.rotation.offset(deltaPitch, deltaYaw);
                    game.updateSelectedBlock();
                    game.gameRenderer.calculateFrustum();
                }
            }
        }
        lastMouseX = newMouseX;
        lastMouseY = newMouseY;
    }

}
