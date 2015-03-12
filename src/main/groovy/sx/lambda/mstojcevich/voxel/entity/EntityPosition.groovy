package sx.lambda.mstojcevich.voxel.entity

import groovy.transform.CompileStatic;

@CompileStatic
public class EntityPosition implements Cloneable, Serializable {

    private synchronized float x, y, z;

    /**
     * @param x - North/south position of block
     * @param y - Up/down position of block
     * @param z - East/west position of block
     */
    public EntityPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
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
        synchronized (this) {
            this.x += x
            this.y += y
            this.z += z
        }
    }

    public void setPos(float x, float y, float z) {
        this.x = x
        this.y = y
        this.z = z
    }

    @Override
    public boolean equals(Object o) {
        if(o.is(this)) return true;
        if(!(o instanceof EntityPosition)) return false;

        EntityPosition other = (EntityPosition)o;
        return this.getX() == other.getX() && this.getY() == other.getY() && this.getZ() == other.getZ();
    }

    public float planeDistance(EntityPosition pos2) {
        return Math.sqrt(Math.pow(pos2.x - x, 2) + Math.pow(pos2.z - z, 2))
    }

    public float planeDistance(float xp, float zp) {
        return Math.sqrt(Math.pow(xp - x, 2) + Math.pow(zp - z, 2))
    }

    @Override
    //TODO Will this give any hash overlap?
    public int hashCode() {
        float hashCode = 1;
        hashCode = hashCode*2 + this.getX();
        hashCode = hashCode*2 + this.getY();
        hashCode = hashCode*2 + this.getZ();
        return hashCode;
    }

    public EntityPosition clone() {
        return new EntityPosition(x, y, z);
    }

}