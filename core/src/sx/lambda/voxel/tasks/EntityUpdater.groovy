package sx.lambda.voxel.tasks

import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.api.VoxelGameAPI
import sx.lambda.voxel.api.events.EventGameTick
import sx.lambda.voxel.entity.Entity

@CompileStatic
class EntityUpdater implements RepeatedTask {

    private VoxelGameClient game

    public EntityUpdater(VoxelGameClient game) {
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
                    for(Entity e : game.world.getLoadedEntities()) {
                        e.onUpdate()
                    }
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
