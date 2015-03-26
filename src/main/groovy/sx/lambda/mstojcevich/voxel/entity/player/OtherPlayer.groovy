package sx.lambda.mstojcevich.voxel.entity.player

import groovy.transform.CompileStatic
import sx.lambda.mstojcevich.voxel.entity.EntityRotation
import sx.lambda.mstojcevich.voxel.entity.EntityPosition
import sx.lambda.mstojcevich.voxel.tasks.MovementHandler

/**
 * Other player connected to the same server
 */
@CompileStatic
class OtherPlayer extends Player {

    OtherPlayer(EntityPosition pos, EntityRotation rot) {
        super(pos, rot)
    }

    @Override
    void onUpdate(){}

    @Override
    void updateMovement(MovementHandler handler){}

}
