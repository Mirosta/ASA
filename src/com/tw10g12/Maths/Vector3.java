package com.tw10g12.Maths;

public class Vector3
{
    protected double x;
    protected double y;
    protected double z;

    public Vector3()
    {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Vector3(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(Vector2 v, double z)
    {
        this.x = v.getX();
        this.y = v.getY();
        this.z = z;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public void setZ(double z)
    {
        this.z = z;
    }

    public Vector3 multiply(Vector3 v)
    {
        return Vector3.multiply(this, v);
    }

    public Vector3 multiply(double factor)
    {
        return Vector3.multiply(this, factor);
    }

    public Vector3 add(Vector3 v)
    {
        return Vector3.add(this, v);
    }

    public Vector3 subtract(Vector3 v)
    {
        return Vector3.subtract(this, v);
    }

    public Vector3 divide(double divisor)
    {
        return Vector3.divide(this, divisor);
    }

    public double length()
    {
        return Vector3.length(this);
    }

    public double lengthSquared()
    {
        return Vector3.lengthSquared(this);
    }

    public Vector3 normalise()
    {
        return Vector3.normalise(this);
    }

    public Vector3 cross(Vector3 v)
    {
        return Vector3.cross(this, v);
    }

    public double dot(Vector3 v)
    {
        return Vector3.dot(this, v);
    }

    //http://www.beosil.com/download/CollisionDetectionHashing_VMV03.pdf
    //http://stackoverflow.com/questions/5928725/hashing-2d-3d-and-nd-vectors
    @Override
    public int hashCode()
    {
        double p1 = 27644437, p2 = 73856093, p3 = 83492791;
        long hash = (long) (this.x * p1) ^ (long) (this.y * p2) ^ (long) (this.z * p3);
        return (int) (((hash) % 2l * (long) Integer.MAX_VALUE) + (long) Integer.MIN_VALUE);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Vector3)) return false;
        Vector3 vec = (Vector3) obj;
        if (vec.x != this.x) return false;
        if (vec.y != this.y) return false;
        if (vec.z != this.z) return false;

        return true;
    }

    @Override
    public String toString()
    {
        return this.x + "," + this.y + "," + this.z;
    }

    public static Vector3 multiply(Vector3 v1, Vector3 v2)
    {
        return new Vector3(v1.x * v2.x, v1.y * v2.y, v1.z * v2.z);
    }

    public static Vector3 multiply(Vector3 v, double factor)
    {
        return new Vector3(v.x * factor, v.y * factor, v.z * factor);
    }

    public static Vector3 add(Vector3 v1, Vector3 v2)
    {
        return new Vector3(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
    }

    public static Vector3 subtract(Vector3 v1, Vector3 v2)
    {
        return new Vector3(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }

    public static Vector3 divide(Vector3 v, double divisor)
    {
        return new Vector3(v.x / divisor, v.y / divisor, v.z / divisor);
    }

    public static double length(Vector3 v)
    {
        return Math.sqrt(Vector3.lengthSquared(v));
    }

    public static double lengthSquared(Vector3 v)
    {
        return v.getX() * v.getX() + v.getY() * v.getY() + v.getZ() * v.getZ();
    }

    public static Vector3 normalise(Vector3 v)
    {
        double length = Vector3.length(v);
        return Vector3.divide(v, length);
    }

    public static Vector3 cross(Vector3 v1, Vector3 v2)
    {
        return new Vector3(v1.getY() * v2.getZ() - v1.getZ() * v2.getY(), v1.getZ() * v2.getX() - v1.getX() * v2.getZ(), v1.getX() * v2.getY() - v1.getY() * v2.getX());
    }

    public static double dot(Vector3 v1, Vector3 v2)
    {
        return v1.getX() * v2.getX() + v1.getY() * v2.getY() + v1.getZ() * v2.getZ();
    }

    public static boolean equiv(Vector3 v1, Vector3 v2)
    {
        if (v1.getX() != v2.getX()) return false;
        if (v1.getY() != v2.getY()) return false;
        if (v1.getZ() != v2.getZ()) return false;
        return true;
    }
}
