package sx.lambda.voxel.entity

import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.collision.BoundingBox
import groovy.transform.CompileStatic
import sx.lambda.voxel.tasks.MovementHandler
import sx.lambda.voxel.world.IWorld
import sx.lambda.voxel.world.chunk.IChunk

@CompileStatic
public abstract class LivingEntity extends Entity implements Serializable {

    private synchronized transient float yVelocity
    private boolean onGround
    private final float width, height

    //Needed for serialization
    public LivingEntity() {
        this(getDefaultModel(), new EntityPosition(0, 0, 0), new EntityRotation())
    }

    public LivingEntity(Model model, EntityPosition pos, EntityRotation rot) {
        this(model, pos, rot, 1, 1);
    }

    public LivingEntity(Model model, EntityPosition pos, EntityRotation rot, float width, float height) {
        super(model, pos, rot)
        this.width = width
        this.height = height
    }

    public float getYVelocity() { this.yVelocity }

    public boolean isOnGround() { this.onGround }

    public void updateMovement(MovementHandler handler) {
        if (this.onGround) {
            this.yVelocity = Math.max(this.yVelocity, 0)
        }
        if (!handler.checkDeltaCollision(this, 0, yVelocity, 0)) {
            this.getPosition().offset(0, this.yVelocity, 0)
        } else {
            yVelocity = 0
        }
    }

    public void setYVelocity(float velocity) {
        synchronized (this) {
            this.yVelocity = velocity
        }
    }

    public int getWidth() { this.width }

    public float getHeight() { this.height }

    public void setOnGround(boolean onGround) {
        synchronized (this) {
            this.onGround = onGround
        }
    }

    public int getBlockInFeet(IWorld world) {
        IChunk chunk = world.getChunkAtPosition(MathUtils.floor(position.x), MathUtils.floor(position.z))
        if (chunk != null) {
            return chunk.getBlockId(
                    MathUtils.floor(position.x) & (world.getChunkSize()-1),
                    MathUtils.floor(position.y),
                    MathUtils.floor(position.z) & (world.getChunkSize()-1))
        } else {
            return 0
        }
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

}
