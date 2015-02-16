package com.tw10g12.Maths;

public class Vector4
{
    protected double x;
    protected double y;
    protected double z;
    protected double w;

    public Vector4(double x, double y, double z, double w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector4(Vector3 v, double w)
    {
        this.x = v.getX();
        this.y = v.getY();
        this.z = v.getZ();
        this.w = w;
    }

    public Vector4(Vector2 v, double z, double w)
    {
        this.x = v.getX();
        this.y = v.getY();
        this.z = z;
        this.w = w;
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

    public double getW()
    {
        return w;
    }

    public Vector4 add(Vector4 v)
    {
        return Vector4.add(this, v);
    }

    public Vector4 multiply(double factor)
    {
        return Vector4.multiply(this, factor);
    }

    public Vector4 multiply(Vector4 v)
    {
        return Vector4.multiply(this, v);
    }

    public static Vector4 add(Vector4 v1, Vector4 v2)
    {
        return new Vector4(v1.getX()+v2.getX(),v1.getY()+v2.getY(),v1.getZ()+v2.getZ(),v1.getW()+v2.getW());
    }

    public static Vector4 multiply(Vector4 v1, Vector4 v2)
    {
        return new Vector4(v1.getX()*v2.getX(),v1.getY()*v2.getY(),v1.getZ()*v2.getZ(),v1.getW()*v2.getW());
    }

    public static Vector4 multiply(Vector4 v, double factor)
    {
        return new Vector4(v.getX()*factor,v.getY()*factor,v.getZ()*factor,v.getW()*factor);
    }

    public Vector3 getXYZ()
    {
        return new Vector3(x,y,z);
    }

    public float[] getArray()
    {
        // TODO Auto-generated method stub
        return new float[]{(float)x,(float)y,(float)z,(float)w};
    }

    public Vector3 toVector3()
    {
        return new Vector3(x/w, y/w, z/w);
    }

}
