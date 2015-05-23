package sx.lambda.voxel.entity

import groovy.transform.CompileStatic

@CompileStatic
class EntityRotation implements Serializable {

    private synchronized float yaw, pitch

    public EntityRotation(float yaw, float pitch) {
        this.yaw = yaw
        this.pitch = pitch
    }

    public EntityRotation() {
        this.yaw = 0
        this.pitch = 0
    }

    public float getYaw() {
        this.yaw
    }

    public float getPitch() {
        this.pitch
    }

    public void offset(float pitch, float yaw) {
        synchronized (this) {
            this.pitch = Math.min(Math.max((this.pitch + pitch) % 360, -90), 90)
            this.yaw = (this.yaw + yaw) % 360
        }
    }

    public void setRot(float pitch, float yaw) {
        this.pitch = pitch
        this.yaw = yaw
    }

}
