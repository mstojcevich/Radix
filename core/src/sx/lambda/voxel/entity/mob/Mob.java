package sx.lambda.voxel.entity.mob;

import org.spacehq.mc.protocol.data.game.values.entity.MobType;
import sx.lambda.voxel.entity.EntityModel;
import sx.lambda.voxel.entity.EntityPosition;
import sx.lambda.voxel.entity.EntityRotation;
import sx.lambda.voxel.entity.LivingEntity;

import java.io.Serializable;

/**
 * Created by louxiu on 12/20/15.
 */
public class Mob extends LivingEntity implements Serializable {
    private transient boolean moved = false;

    public Mob(MobType type, int entityId, EntityPosition pos, EntityRotation rot) {
        super(EntityModel.getModel(type), EntityModel.getTexture(type), pos, rot);
        setID(entityId);
    }
}
