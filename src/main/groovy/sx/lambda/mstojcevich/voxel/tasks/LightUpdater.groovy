package sx.lambda.mstojcevich.voxel.tasks

import groovy.transform.CompileStatic
import sx.lambda.mstojcevich.voxel.VoxelGame

@CompileStatic
/**
 * Updates the propagation of light
 */
class LightUpdater implements RepeatedTask {

    private final VoxelGame game

    public LightUpdater(VoxelGame game) {
        this.game = game
    }

    @Override
    String getIdentifier() {
        return "Light Updater"
    }

    @Override
    void run() {
        try {
            while (!game.isDone()) {
//                if (game.world != null) {
//                    game.world.processLightQueue()
//                } Caused random dark spots. Investigation should be done later.
                sleep(1000)
            }
        } catch (Exception e) {
            game.handleCriticalException(e)
        }
    }

}
