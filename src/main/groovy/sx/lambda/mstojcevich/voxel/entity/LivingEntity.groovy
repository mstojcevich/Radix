package sx.lambda.mstojcevich.voxel.entity

import groovy.transform.CompileStatic
import sx.lambda.mstojcevich.voxel.util.gl.ObjModel

@CompileStatic
public abstract class LivingEntity extends Entity implements Serializable {

    private synchronized transient float yVelocity
    private boolean onGround
    private final float width, height

    //Needed for serialization
    public LivingEntity() {
        this(getDefaultModel(), new EntityPosition(0,0,0), new EntityRotation())
    }

    public LivingEntity(ObjModel model, EntityPosition pos, EntityRotation rot) {
        this(model, pos, rot, 1, 1);
    }

    public LivingEntity(ObjModel model, EntityPosition pos, EntityRotation rot, float width, float height) {
        super(model, pos, rot)
        this.width = width
        this.height = height
    }

    public float getYVelocity(){ this.yVelocity }

    public boolean isOnGround() { this.onGround }

    public void updateMovement(){
        if(this.onGround) {
            this.yVelocity = Math.max(this.yVelocity, 0)
        }
        this.getPosition().offset(0, this.yVelocity, 0)
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

    /**
     * Called 20 times a second
     */
    public void onUpdate() {}

}
