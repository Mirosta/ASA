package com.tw10g12.Maths;

import java.util.*;

/**
 * Created by Tom on 07/04/2015.
 */
public class BezierUtil
{
    //http://devmag.org.za/2011/04/05/bzier-curves-a-tutorial/
    public static Vector3 calculateBezierPoint(double progress, Vector3 start, Vector3 end, Vector3 startControl, Vector3 endControl)
    {
        double reverseProgress = 1.0 - progress;
        double progressSqr = progress*progress;
        double reverseProgressSqr = reverseProgress*reverseProgress;
        double reverseProgressCubed = reverseProgressSqr * reverseProgress;
        double progressCubed = progressSqr * progress;

        Vector3 pos = start.multiply(reverseProgressCubed); //first term
        pos = pos.add(startControl.multiply(3.0 * reverseProgressSqr * progress)); //second term
        pos = pos.add(endControl.multiply(3.0 * reverseProgress * progressSqr)); //third term
        pos = pos.add(end.multiply(progressCubed)); //fourth term

        return pos;
    }

    public static Vector3[] calculateBezierBoundingBox(Vector3 start, Vector3 end, Vector3 startControl, Vector3 endControl, int detail)
    {
        Vector3 min = new Vector3(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        Vector3 max = new Vector3(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);

        List<Vector3> checkVectors = new ArrayList<Vector3>();

        for(int i = 0; i <= detail; i++)
        {
            float progress = (float) i / (float) detail;

            checkVectors.add(calculateBezierPoint(progress, start, end, startControl, endControl));
        }
/*
        double[][] checkValues =
            {
                {start.getX(), startControl.getX(), endControl.getX(), end.getX()},
                {start.getY(), startControl.getY(), endControl.getY(), end.getY()},
                {start.getZ(), startControl.getZ(), endControl.getZ(), end.getZ()}
            };

        for(double[] values : checkValues)
        {
            for(Double solution : solveTurningPoints(values[0], values[1], values[2], values[3]))
            {
                if(solution >= 0 && solution <= 1) checkVectors.add(calculateBezierPoint(solution, start, end, startControl, endControl));
            }
        }
*/
        for(Vector3 checkVector : checkVectors)
        {
            min = min.min(checkVector);
            max = max.max(checkVector);
        }

        return new Vector3[]{min, max};
    }

    private static Collection<Double> solveTurningPoints(double a, double b, double c, double d)
    {
        double insideSqrRt = (b*b + c*c + a * (-c + d) - b * (c + d));
        if(insideSqrRt < 0) return new HashSet<Double>(); //Only imaginary solutions, so return empty set

        Set<Double> currentSolutions = new HashSet<Double>();
        double minusQB = -6*(a - 2*b +c);
        double plusMinusPart = 6 * Math.sqrt(insideSqrRt);
        double divisor = 6*(a - 3*b + 3*c -d);

        currentSolutions.add((minusQB + plusMinusPart) / divisor);
        currentSolutions.add((minusQB - plusMinusPart) / divisor);

        return currentSolutions;
    }
}
