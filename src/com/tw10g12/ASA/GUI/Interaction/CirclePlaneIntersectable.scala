package com.tw10g12.ASA.GUI.Interaction

import com.tw10g12.Maths.{Vector3, Ray3}

/**
 * Created by Tom on 07/04/2015.
 */
class CirclePlaneIntersectable(centre: Vector3, up: Vector3, right: Vector3, radius: Double, attachedModelObject: AnyRef) extends PlaneIntersectable(centre, up.cross(right), attachedModelObject)
{

    override def rayIntersects(ray: Ray3): Double =
    {
        val potentialPoint = getIntersectionPoint(ray)
        if(potentialPoint._1 == null) return Double.NaN
        val sqrDistance: Double = if(potentialPoint._1.getW == 0)
        {
            getSqrDistanceToLine(ray, centre)
        }
        else
        {
            potentialPoint._1.getXYZ.subtract(centre).lengthSquared()
        }
        if(sqrDistance < radius * radius) return sqrDistance
        else return Double.NaN
    }

    def getSqrDistanceToLine(ray: Ray3, point: Vector3): Double =
    {
        val pointDifference: Vector3 = ray.getRayStart.subtract(point)
        return pointDifference.subtract(ray.getRayDir.multiply(pointDifference.dot(ray.getRayDir))).lengthSquared()
    }

}
