package com.tw10g12.ASA.GUI.Interaction

import com.tw10g12.Maths.{Vector3, Ray3}

/**
 * Created by Tom on 16/02/2015.
 */
class AABBIntersectable(start: Vector3, size: Vector3) extends Intersectable
{

    override def rayIntersects(ray: Ray3): Double =
    {
        //Left, Top, Front, Right, Bottom, Back
        val multipliers = List[Vector3](new Vector3(0,0,0), new Vector3(0,0,0), new Vector3(0,0,0), new Vector3(1,0,0), new Vector3(0,1,0), new Vector3(0,0,1))
        val sizeMultipliers = List[Vector3](new Vector3(0, 1, 1), new Vector3(1,0,1), new Vector3(1, 1, 0))
        var i = 0
        var smallestDistance: Double = Double.NaN
        for(i <- 0 to 5)
        {
            val distance: Double = rayIntersectsPlane(ray, start.add(size.multiply(multipliers(i))), size.multiply(sizeMultipliers(i % 3)))
            if(!distance.isNaN && smallestDistance.isNaN || distance < smallestDistance)
            {
                smallestDistance = distance
            }
        }
        return smallestDistance
    }

    private def rayIntersectsPlane(ray: Ray3, topCorner: Vector3, size: Vector3): Double =
    {
        var potentialFactor: Double = Double.NaN
        if(size.getX == 0)
        {
            //s.X + f*d.X = tC.X => f = (tC.X - s.X) / d.X
            potentialFactor = (topCorner.getX - ray.getRayStart.getX) / ray.getRayDir.getX
        }
        else if(size.getY == 0)
        {
            //s.Y + f*d.Y = tC.Y => f = (tC.Y - s.Y) / d.Y
            potentialFactor = (topCorner.getY - ray.getRayStart.getY) / ray.getRayDir.getY
        }
        else if(size.getZ == 0)
        {
            //s.Z + f*d.Z = tC.Z => f = (tC.Z - s.Z) / d.Z
            potentialFactor = (topCorner.getZ - ray.getRayStart.getZ) / ray.getRayDir.getZ
        }
        if(potentialFactor.isNaN) return potentialFactor

        val potentialPoint: Vector3 = ray.getPointAlongRay(potentialFactor)
        if(pointInArea(potentialPoint, topCorner, size)) return potentialFactor
        return Double.NaN
    }

    private def pointInArea(point: Vector3, topCorner: Vector3, size: Vector3): Boolean =
    {
        if(point.getX < topCorner.getX) return false
        if(point.getY < topCorner.getY) return false
        if(point.getZ < topCorner.getZ) return false
        if(point.getX > topCorner.getX + size.getX) return false
        if(point.getY > topCorner.getY + size.getY) return false
        if(point.getZ > topCorner.getZ + size.getZ) return false
        return true
    }
}
