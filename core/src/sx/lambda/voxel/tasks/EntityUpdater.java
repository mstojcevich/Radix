package sx.lambda.voxel.tasks;

import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.api.RadixAPI;
import sx.lambda.voxel.api.events.EventGameTick;

public class EntityUpdater implements RepeatedTask {

    private RadixClient game;

    public EntityUpdater(RadixClient game) {
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

                    RadixAPI.instance.getEventManager().push(new EventGameTick(game.getWorld()));
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
