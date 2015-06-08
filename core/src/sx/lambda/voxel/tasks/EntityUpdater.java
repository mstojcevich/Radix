package sx.lambda.voxel.tasks;

import groovy.transform.CompileStatic;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.api.VoxelGameAPI;
import sx.lambda.voxel.api.events.EventGameTick;

@CompileStatic
public class EntityUpdater implements RepeatedTask {

    private VoxelGameClient game;

    public EntityUpdater(VoxelGameClient game) {
        this.game = game;
    }

    @Override
    public String getIdentifier() {
        return "Entity Updater";
    }

    @Override
    public void run() {
        try {
            while (!game.isDone()) {
                if (game.getWorld() != null && game.getPlayer() != null) {
                    game.getWorld().getLoadedEntities().forEach(
                            sx.lambda.voxel.entity.Entity::onUpdate);

                    VoxelGameAPI.instance.getEventManager().push(new EventGameTick(game.getWorld()));
                    Thread.sleep(50);
                } else {
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            game.handleCriticalException(e);
        }
    }

}
