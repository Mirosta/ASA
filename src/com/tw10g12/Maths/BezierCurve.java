package com.tw10g12.Maths;

/**
 * Created by Tom on 07/04/2015.
 */
public class BezierCurve
{
    Vector3 start; //a
    Vector3 startControl; //b

    Vector3 end; //d
    Vector3 endControl; //c

    public BezierCurve(Vector3 start, Vector3 startControl, Vector3 end, Vector3 endControl)
    {
        this.start = start;
        this.startControl = startControl;
        this.end = end;
        this.endControl = endControl;
    }

    public Vector3 getStart()
    {
        return start;
    }

    public Vector3 getStartControl()
    {
        return startControl;
    }

    public Vector3 getEnd()
    {
        return end;
    }

    public Vector3 getEndControl()
    {
        return endControl;
    }

    public Vector3 getPointAlongCurve(double progress)
    {
        return BezierUtil.calculateBezierPoint(progress, this.start, this.end, this.startControl, this.endControl);
    }

    public Vector3[] getBoundingBox(int detail)
    {
        return BezierUtil.calculateBezierBoundingBox(this.start, this.end, this.startControl, this.endControl, detail);
    }
}
