package sx.lambda.mstojcevich.voxel.tasks

import groovy.transform.CompileStatic
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.api.VoxelGameAPI
import sx.lambda.mstojcevich.voxel.api.events.EventGameTick

@CompileStatic
class EntityUpdater implements RepeatedTask {

    private VoxelGame game

    public EntityUpdater(VoxelGame game) {
        this.game = game
    }

    @Override
    String getIdentifier() {
        return "Entity Updater"
    }

    @Override
    void run() {
        try {
            while (!game.done) {
                if (game.world != null && game.player != null) {
                    game.player.onUpdate() // TODO update the rest of the entities
                    VoxelGameAPI.instance.eventManager.push(new EventGameTick(game.world))
                    sleep(50)
                } else {
                    sleep(1000)
                }
            }
        } catch (Exception e) {
            game.handleCriticalException(e)
        }
    }

}
