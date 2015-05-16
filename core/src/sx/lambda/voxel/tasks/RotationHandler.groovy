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

    @Override
    void run() {
        try {
            final float mouseSensitivity = 0.05f //TODO Config - allow changeable mouse sensitivity
            while (!game.isDone()) {
                if(game.world == null || game.player == null) {
                    sleep(1000)
                } else {
                    if (true) { // TODO check if window is in focus
                        float deltaYaw = (float) Gdx.input.getDeltaX() * mouseSensitivity
                        float deltaPitch = (float) Gdx.input.getDeltaY() * mouseSensitivity
                        float newPitch = Math.abs(game.getPlayer().getRotation().getPitch() + deltaPitch)
                        if (newPitch > 90) {
                            deltaPitch = 0
                        }
                        game.getPlayer().getRotation().offset(deltaPitch, deltaYaw)
                        game.updateSelectedBlock()
                        game.gameRenderer.calculateFrustum()
                    }
                    sleep(10)
                }
            }
        } catch(Exception e) {
            game.handleCriticalException(e)
        }
    }

}
