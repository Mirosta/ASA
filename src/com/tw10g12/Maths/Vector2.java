package com.tw10g12.Maths;

public class Vector2
{
    private double x;
    private double y;

    public Vector2(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public Vector2 add(Vector2 v)
    {
        return Vector2.add(this, v);
    }

    public Vector2 subtract(Vector2 v)
    {
        return Vector2.subtract(this, v);
    }

    public Vector2 multiply(double factor)
    {
        return Vector2.multiply(this, factor);
    }

    public Vector2 multiply(Vector2 v)
    {
        return Vector2.multiply(this, v);
    }

    public Vector2 divide(Vector2 v)
    {
        return Vector2.divide(this, v);
    }

    public double lengthSquared()
    {
        return Vector2.lengthSquared(this);
    }

    public double length()
    {
        return Vector2.length(this);
    }

    public static Vector2 add(Vector2 v1, Vector2 v2)
    {
        return new Vector2(v1.x + v2.x, v1.y+v2.y);
    }

    public static Vector2 subtract(Vector2 v1, Vector2 v2)
    {
        return new Vector2(v1.x-v2.x, v1.y-v2.y);
    }

    public static Vector2 multiply(Vector2 v1, Vector2 v2)
    {
        return new Vector2(v1.getX()*v2.getX(), v1.getY()*v2.getY());
    }

    public static Vector2 multiply(Vector2 v, double factor)
    {
        return new Vector2(v.x * factor, v.y * factor);
    }

    public static Vector2 divide(Vector2 v1, Vector2 v2)
    {
        return new Vector2(v1.x/v2.x, v1.y/v2.y);
    }

    public static double lengthSquared(Vector2 v)
    {
        return v.x * v.x + v.y * v.y;
    }

    public static double length(Vector2 v)
    {
        return Math.sqrt(Vector2.lengthSquared(v));
    }
}
