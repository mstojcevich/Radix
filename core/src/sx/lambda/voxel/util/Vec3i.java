/*******************************************************************************
 * Copyright 2012 Martijn Courteaux <martijn.courteaux@skynet.be>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package sx.lambda.voxel.util;

import java.io.Serializable;

public class Vec3i implements Serializable
{
    public int x, y, z;

    /**
     * Constructs a new Vec3i with this value: (0, 0, 0)
     */
    public Vec3i()
    {
        this(0, 0, 0);
    }

    public Vec3i(int x, int y, int z)
    {
        super();
        set(x, y, z);
    }

    /**
     * Constructs a new Vec3i and copies the values of the passed vector.
     * @param v the vector to be copied
     */
    public Vec3i(Vec3i v)
    {
        this(v.x, v.y, v.z);
    }

    public void set(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Subtracts this vector with the passed vector.
     *
     * @param v
     *            the vector to subtract from this
     * @return {@code this}
     */
    public Vec3i sub(Vec3i v)
    {
        set(x - v.x, y - v.y, z - v.z);
        return this;
    }

    /**
     * Adds the passed vector to this vector
     *
     * @param v
     *            the vector to add
     * @return {@code this}
     */
    public Vec3i add(Vec3i v)
    {
        set(x + v.x, y + v.y, z + v.z);
        return this;
    }


    /**
     * Performs a scalar product on this vector
     * @param f Scale factor
     * @return {@code this}
     */
    public Vec3i scale(float f)
    {
        set((int)Math.floor(x * f), (int)Math.floor(y * f), (int)Math.floor(z * f));
        return this;
    }

    /**
     * Uses cache.
     *
     * @return the squared length of this vector
     */
    public float lengthSquared()
    {
        return x * x + y * y + z * z;
    }

    /**
     * Uses cache.
     *
     * @return the length of this vector
     */
    public float length()
    {
        return (float)Math.sqrt(lengthSquared());
    }

    /**
     * This vector will be the result of the cross product, performed on the two
     * vectors passed. Returns {@code this} vector.
     *
     * @param a Vector 1
     * @param b Vector 2
     * @return {@code this}
     */
    public Vec3i cross(Vec3i a, Vec3i b)
    {
        set(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
        return this;
    }

    /**
     * Performs a dot product on the two specified vectors.
     * @param a Vector 1
     * @param b Vector 2
     * @return the result of the dot product.
     */
    public static int dot(Vec3i a, Vec3i b)
    {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public void set(Vec3i vec)
    {
        set(vec.x, vec.y, vec.z);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vec3i other = (Vec3i) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        if (z != other.z)
            return false;
        return true;
    }

    public Vec3i translate(int x, int y, int z) {
        return new Vec3i(this.x + x, this.y + y, this.z + z);
    }



    @Override
    public String toString()
    {
        return "Vec3i [x=" + x + ", y=" + y + ", z=" + z + "]";
    }

    public boolean equals(int x, int y, int z)
    {
        return this.x == x && this.y == y && this.z == z;
    }
}