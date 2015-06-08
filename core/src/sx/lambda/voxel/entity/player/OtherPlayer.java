package sx.lambda.voxel.entity.player;

import sx.lambda.voxel.entity.EntityPosition;
import sx.lambda.voxel.entity.EntityRotation;
import sx.lambda.voxel.tasks.MovementHandler;

/**
 * Other player connected to the same server
 */
public class OtherPlayer extends Player {

    public OtherPlayer(EntityPosition pos, EntityRotation rot) {
        super(pos, rot);
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void updateMovement(MovementHandler handler, float seconds) {
    }

}
