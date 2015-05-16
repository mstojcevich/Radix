package sx.lambda.voxel.bounding

import sx.lambda.voxel.entity.EntityPosition

class BoundingBox {

    private final float x, y, z, x2, y2, z2;

    private final float xMid, yMid, zMid;

    private final float xRadius, yRadius, zRadius;

    public BoundingBox(float x, float y, float z, float xDiff, float yDiff, float zDiff) {
        this.x = x
        this.y = y
        this.z = z
        this.x2 = x+xDiff
        this.y2 = y+yDiff
        this.z2 = z+zDiff
        this.xMid = x + xDiff/2
        this.yMid = y + yDiff/2
        this.zMid = z + zDiff/2
        this.xRadius = xDiff/2
        this.yRadius = yDiff/2
        this.zRadius = zDiff/2
    }

    public BoundingBox(EntityPosition position, float xDiff, float yDiff, float zDiff) {
        this(position.getX(), position.getY(), position.getZ(), xDiff, yDiff, zDiff)
    }

    public float getX() { this.x }
    public float getY() { this.y }
    public float getZ() { this.z }
    public float getX2() { this.x2 }
    public float getY2() { this.y2 }
    public float getZ2() { this.z2 }

    public boolean collides(BoundingBox boundingBox) {
        if (Math.abs(this.getCenterX() - boundingBox.getCenterX()) > (this.getXRadius() + boundingBox.getXRadius())) return false;
        if (Math.abs(box1.center.y - box2.center.y) > (box1.r[1] + box2.r[1])) return false
        if (Math.abs(box1.center.z - box2.center.z) > (box1.r[2] + box2.r[2])) return false
        return true
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) return true;
        if(!(o instanceof BoundingBox)) return false;

        BoundingBox other = (BoundingBox)o;
        return this.getX() == other.getX() && this.getY() == other.getY() && this.getZ() == other.getZ() &&
                this.getX2() == other.getX2() && this.getY2() == other.getY2() && this.getZ2() == other.getZ2();
    }

    @Override
    public int hashCode() {
        float hashCode = 1;
        hashCode = hashCode*2 + this.x;
        hashCode = hashCode*2 + this.y;
        hashCode = hashCode*2 + this.z;
        hashCode = hashCode*2 + this.x2;
        hashCode = hashCode*2 + this.y2;
        hashCode = hashCode*2 + this.z2;
        return hashCode;
    }

}
