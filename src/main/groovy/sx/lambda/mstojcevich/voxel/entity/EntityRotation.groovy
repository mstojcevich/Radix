package sx.lambda.mstojcevich.voxel.entity

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
            this.pitch = (this.pitch+pitch)%360
//            if(this.pitch < 0) {
//                this.pitch = 360-pitch
//            }
            this.yaw = (this.yaw+yaw)%360
//            if(this.yaw < 0) {
//                this.yaw = 360-yaw
//            }
        }
    }

    public void setRot(float pitch, float yaw) {
        this.pitch = pitch
        this.yaw = yaw
    }

}
