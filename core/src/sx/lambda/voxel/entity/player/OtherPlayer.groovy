package sx.lambda.voxel.entity.player

import groovy.transform.CompileStatic
import sx.lambda.voxel.entity.EntityPosition
import sx.lambda.voxel.entity.EntityRotation
import sx.lambda.voxel.tasks.MovementHandler

/**
 * Other player connected to the same server
 */
@CompileStatic
class OtherPlayer extends Player {

    OtherPlayer(EntityPosition pos, EntityRotation rot) {
        super(pos, rot)
    }

    @Override
    void onUpdate() {}

    @Override
    void updateMovement(MovementHandler handler, float seconds) {}

}
