package com.tw10g12.Maths;

/**
 * Created by Tom on 16/02/2015.
 */
public class Ray3
{
    private Vector3 rayStart;
    private Vector3 rayDir;

    public Ray3(Vector3 rayStart, Vector3 rayDir)
    {
        this(rayStart, rayDir, false);
    }

    public Ray3(Vector3 rayStart, Vector3 rayDir, boolean normaliseDirection)
    {
        this.rayStart = rayStart;
        this.rayDir = (normaliseDirection ? rayDir.normalise() : rayDir);
    }

    public Vector3 getRayStart()
    {
        return rayStart;
    }

    public Vector3 getRayDir()
    {
        return rayDir;
    }

    public Vector3 getPointAlongRay(double factor)
    {
        return rayStart.add(rayDir.multiply(factor));
    }
}
