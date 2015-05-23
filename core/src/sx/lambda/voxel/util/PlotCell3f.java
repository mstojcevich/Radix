package sx.lambda.voxel.util;

import com.badlogic.gdx.math.Vector3;

interface Plot<T> {
    boolean next();

    void reset();

    void end();

    T get();
}

public class PlotCell3f implements Plot<Vec3i> {

    private final Vector3 size = new Vector3();
    private final Vector3 off = new Vector3();
    private final Vector3 pos = new Vector3();
    private final Vector3 dir = new Vector3();

    private final Vec3i index = new Vec3i();

    private final Vector3 delta = new Vector3();
    private final Vec3i sign = new Vec3i();
    private final Vector3 max = new Vector3();

    private int limit;
    private int plotted;

    public PlotCell3f(float offx, float offy, float offz, float width, float height, float depth) {
        off.set(offx, offy, offz);
        size.set(width, height, depth);
    }

    public void plot(Vector3 position, Vector3 direction, int cells) {
        limit = cells;

        pos.set(position);

        // Normalize dir w/ direction
        float invLen = 1.0f / direction.len();
        dir.x = direction.x * invLen;
        dir.y = direction.y * invLen;
        dir.z = direction.z * invLen;

        delta.set(size);
        delta.set(delta.x / dir.x, delta.y / dir.y, delta.z / dir.z);

        sign.x = (dir.x > 0) ? 1 : (dir.x < 0 ? -1 : 0);
        sign.y = (dir.y > 0) ? 1 : (dir.y < 0 ? -1 : 0);
        sign.z = (dir.z > 0) ? 1 : (dir.z < 0 ? -1 : 0);

        reset();
    }

    @Override
    public boolean next() {
        if (plotted++ > 0) {
            float mx = sign.x * max.x;
            float my = sign.y * max.y;
            float mz = sign.z * max.z;

            if (mx < my && mx < mz) {
                max.x += delta.x;
                index.x += sign.x;
            } else if (mz < my && mz < mx) {
                max.z += delta.z;
                index.z += sign.z;
            } else {
                max.y += delta.y;
                index.y += sign.y;
            }
        }
        return (plotted <= limit);
    }

    @Override
    public void reset() {
        plotted = 0;

        index.x = (int) Math.floor((pos.x - off.x) / size.x);
        index.y = (int) Math.floor((pos.y - off.y) / size.y);
        index.z = (int) Math.floor((pos.z - off.z) / size.z);

        float ax = index.x * size.x + off.x;
        float ay = index.y * size.y + off.y;
        float az = index.z * size.z + off.z;

        max.x = (sign.x > 0) ? ax + size.x - pos.x : pos.x - ax;
        max.y = (sign.y > 0) ? ay + size.y - pos.y : pos.y - ay;
        max.z = (sign.z > 0) ? az + size.z - pos.z : pos.z - az;
        max.set(max.x / dir.x, max.y / dir.y, max.z / dir.z);
    }

    @Override
    public void end() {
        plotted = limit + 1;
    }

    @Override
    public Vec3i get() {
        return index;
    }

    public Vector3 actual() {
        return new Vector3(index.x * size.x + off.x,
                index.y * size.y + off.y,
                index.z * size.z + off.z);
    }

    public Vector3 size() {
        return size;
    }

    public void size(float w, float h, float d) {
        size.set(w, h, d);
    }

    public Vector3 offset() {
        return off;
    }

    public void offset(float x, float y, float z) {
        off.set(x, y, z);
    }

    public Vector3 position() {
        return pos;
    }

    public Vector3 direction() {
        return dir;
    }

    public Vec3i sign() {
        return sign;
    }

    public Vector3 delta() {
        return delta;
    }

    public Vector3 max() {
        return max;
    }

    public int limit() {
        return limit;
    }

    public int plotted() {
        return plotted;
    }


}