package sx.lambda.voxel.entity;

import com.badlogic.gdx.math.MathUtils;

import java.io.Serializable;

public class EntityRotation implements Serializable {
    private float yaw;
    private float pitch;

    public EntityRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public EntityRotation() {
        this.yaw = 0;
        this.pitch = 0;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void offset(float pitch, float yaw) {
        synchronized (this) {
            this.pitch = MathUtils.clamp((this.pitch + pitch) % 360, -90, 90);
            this.yaw = (this.yaw + yaw) % 360;
        }

    }

    public void setRot(float pitch, float yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
    }
}
