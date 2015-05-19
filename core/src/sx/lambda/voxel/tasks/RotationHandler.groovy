package sx.lambda.voxel.tasks

import com.badlogic.gdx.Gdx
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient


@CompileStatic
class RotationHandler implements RepeatedTask {

    private final VoxelGameClient game

    public RotationHandler(VoxelGameClient game) {
        this.game = game
    }

    @Override
    String getIdentifier() {
        return "Rotation Handler"
    }

    private int lastX = -1, lastY = -1;

    @Override
    void run() {
        try {
            final float mouseSensitivity = 0.05f //TODO Config - allow changeable mouse sensitivity
            while (!game.isDone()) {
                if(game.world == null || game.player == null) {
                    sleep(1000)
                } else {
                    if(game.currentScreen != game.hud) {
                        sleep(10)
                    } else {
                        if (lastX == -1) lastX = Gdx.input.getX()
                        if (lastY == -1) lastY = Gdx.input.getY()
                        float deltaYaw = (float) (Gdx.input.getX() - lastX) * mouseSensitivity
                        float deltaPitch = (float) (lastY - Gdx.input.getY()) * mouseSensitivity
                        lastX = Gdx.input.getX()
                        lastY = Gdx.input.getY()
                        float newPitch = Math.abs(game.getPlayer().getRotation().getPitch() + deltaPitch)
                        if (newPitch > 90) {
                            deltaPitch = 0
                        }
                        game.getPlayer().getRotation().offset(deltaPitch, deltaYaw)
                        game.updateSelectedBlock()
                        if (Math.abs(deltaPitch) > 0 || Math.abs(deltaYaw) > 0) {
                            game.gameRenderer.calculateFrustum()
                        }
                        sleep(10)
                    }
                }
            }
        } catch(Exception e) {
            game.handleCriticalException(e)
        }
    }

}
