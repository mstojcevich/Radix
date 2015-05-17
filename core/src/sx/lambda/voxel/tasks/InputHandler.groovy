//package sx.lambda.voxel.tasks
//
//import com.badlogic.gdx.Gdx
//import com.badlogic.gdx.Input.Keys
//import groovy.transform.CompileStatic
//import sx.lambda.voxel.VoxelGameClient
//import sx.lambda.voxel.api.BuiltInBlockIds
//import sx.lambda.voxel.api.VoxelGameAPI
//import sx.lambda.voxel.client.gui.screens.BlockSelectGUI
//import sx.lambda.voxel.client.keybind.Keybind
//import sx.lambda.voxel.net.packet.shared.PacketBreakBlock
//import sx.lambda.voxel.net.packet.shared.PacketPlaceBlock
//
//@CompileStatic
//// TODO Use GDX input handler
//class InputHandler implements RepeatedTask {
//
//    private final VoxelGameClient game
//
//    private final List<Keybind> keybindList = Collections.synchronizedList(new ArrayList<Keybind>())
//
//    public InputHandler(VoxelGameClient game) {
//        this.game = game
//
//        // Register the default keybinds
//        registerKeybind(new Keybind("voxeltest.movement.jump", "Jump", Keys.SPACE, new Runnable() {
//            @Override
//            void run() {
//                if(game.world != null) {
//                    if (game.getPlayer().onGround) {
//                        game.getPlayer().setYVelocity(0.11f)
//                        game.getPlayer().setOnGround(false)
//                    }
//                }
//            }
//        }))
//        registerKeybind(new Keybind("voxeltest.gui.selectblock", "Select Block GUI", Keys.E, new Runnable() {
//            @Override
//            void run() {
//                if(game.world != null  && game.currentScreen == game.hud) {
//                    game.addToGLQueue(new Runnable() {
//                        @Override
//                        void run() {
//                            game.setCurrentScreen(new BlockSelectGUI(game.textureManager, VoxelGameAPI.instance.getBlocksSorted(), game.hud.icons))
//                        }
//                    })
//                }
//            }
//        }))
//        registerKeybind(new Keybind("voxeltest.gui.back", "Back", Keys.ESCAPE, new Runnable() {
//            @Override
//            void run() {
//                if(game.world != null) {
//                    if (game.currentScreen != game.hud) {
//                        game.addToGLQueue(new Runnable() {
//                            @Override
//                            void run() {
//                                game.setCurrentScreen(game.hud)
//                            }
//                        })
//                    } else {
//                        game.exitWorld() // TODO show ingame options
//                    }
//                }
//            }
//        }))
//    }
//
//    @Override
//    String getIdentifier() {
//        return "Input Handler"
//    }
//
//    @Override
//    void run() {
//        /*try {
//            while (!game.isDone()) {
//                while (org.lwjgl.input.Keyboard.next()) {
//                    if (org.lwjgl.input.Keyboard.getEventKeyState()) { //Press down, not release
//                        int key = org.lwjgl.input.Keyboard.getEventKey()
//
//                        for(Keybind kb : keybindList) {
//                            if(kb.key == key) {
//                                kb.press()
//                            }
//                        }
//                    }
//                }
//                for(Keybind kb : keybindList) {
//                    if(kb.shouldRepeat()) {
//                        if(org.lwjgl.input.Keyboard.isKeyDown(kb.key)) {
//                            kb.press()
//                        }
//                    }
//                }
//                while (Gdx.input.isCreated() && Mouse.next()) {
//                    if (Mouse.getEventButtonState()) {
//                        int button = Mouse.getEventButton()
//                        game.currentScreen.onMouseClick(button)
//                        switch (button) {
//                            case 0:
//                                if(game.world != null) {
//                                    if (game.getSelectedBlock() != null) {
//                                        if (game.isRemote() && game.serverChanCtx != null) {
//                                            game.serverChanCtx.writeAndFlush(new PacketBreakBlock(
//                                                    game.getSelectedBlock()))
//                                        } else {
//                                            game.getWorld().removeBlock(game.getSelectedBlock())
//                                        }
//                                    }
//                                }
//                                break;
//                            case 1:
//                                if(game.world != null) {
//                                    if (game.getNextPlacePos() != null) {
//                                        if (game.isRemote() && game.serverChanCtx != null) {
//                                            game.serverChanCtx.writeAndFlush(new PacketPlaceBlock(
//                                                    game.getNextPlacePos(),
//                                                    game.getPlayer().getItemInHand()
//                                            ));
//                                        } else {
//                                            game.getWorld().addBlock(game.getPlayer().getItemInHand(), game.getNextPlacePos())
//                                        }
//                                    }
//                                }
//                                break
//                            default:
//                                break
//                        }
//                    }
//                }
//                if(Keyboard.isCreated()) {
//                    if (org.lwjgl.input.Keyboard.isKeyDown(Keys.SPACE) && game.player.getBlockInFeet(game.world) == BuiltInBlockIds.WATER_ID) {
//                        game.player.setYVelocity(0.02f);
//                    }
//                }
//                sleep(10)
//            }
//        } catch (Exception e) {
//            game.handleCriticalException(e)
//        }*/
//    }
//
//    public void registerKeybind(Keybind kb) {
//        this.keybindList.add(kb)
//    }
//
//}
