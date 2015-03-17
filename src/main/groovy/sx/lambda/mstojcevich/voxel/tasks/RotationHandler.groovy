package sx.lambda.mstojcevich.voxel.tasks

import groovy.transform.CompileStatic
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import sx.lambda.mstojcevich.voxel.VoxelGame

@CompileStatic
class RotationHandler implements RepeatedTask {

    private final VoxelGame game

    public RotationHandler(VoxelGame game) {
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
                if(game.world == null) {
                    sleep(1000)
                } else {
                    if (Mouse.isInsideWindow() && Display.isActive()) {
                        float deltaYaw = (float) Mouse.getDX() * mouseSensitivity
                        float deltaPitch = (float) Mouse.getDY() * mouseSensitivity
                        float newPitch = Math.abs(game.getPlayer().getRotation().getPitch() + deltaPitch)
                        if (newPitch > 90) {
                            deltaPitch = 0
                        }
                        game.getPlayer().getRotation().offset(deltaPitch, deltaYaw)
                        game.updateSelectedBlock()
                        game.calculateFrustum()
                    }
                    sleep(10)
                }
            }
        } catch(Exception e) {
            VoxelGame.instance.handleCriticalException(e)
        }
    }

}
