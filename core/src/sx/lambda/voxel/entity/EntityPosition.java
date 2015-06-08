package sx.lambda.voxel.entity;

import com.badlogic.gdx.math.Vector3;

public class EntityPosition extends Vector3 implements Cloneable {

    public EntityPosition(float x, float y, float z) {
        super(x, y, z);
    }

    public float planeDistance(float xp, float zp) {
        return (float)Math.sqrt(Math.pow(xp - getX(), 2) + Math.pow(zp - getZ(), 2));
    }

    public float planeDistance(Vector3 pos2) {
        return (float)Math.sqrt(Math.pow(pos2.x - getX(), 2) + Math.pow(pos2.z - getZ(), 2));
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public void offset(float x, float y, float z) {
        super.add(x, y, z);
    }

}
