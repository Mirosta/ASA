package com.tw10g12.ASA.GUI.Interaction

import com.tw10g12.Maths.{Vector4, Ray3, Vector3}

/**
 * Created by Tom on 07/04/2015.
 */
class PlaneIntersectable(origin: Vector3, normal: Vector3, var attachedModelObject: AnyRef) extends Intersectable
{
    override def rayIntersects(ray: Ray3): Double =
    {
        val intersectionPoint = getIntersectionPoint(ray)
        return intersectionPoint._2
    }

    def getIntersectionPoint(ray: Ray3): (Vector4, Double) =
    {
        val rayDotNormal = ray.getRayDir.dot(normal)
        if(rayDotNormal == 0)
        {
            if(origin.subtract(ray.getRayDir).dot(normal) == 0) return (new Vector4(ray.getRayDir, 0), 0)
            else return (null, Double.NaN)
        }
        val distance = (origin.subtract(ray.getRayStart)).dot(normal) / rayDotNormal
        return (new Vector4(ray.getPointAlongRay(distance), 1), distance)
    }

    override def getAttachedModelObject: AnyRef = attachedModelObject

    override def setAttachedModelObject(modelObject: AnyRef): Unit =
    {
        attachedModelObject = modelObject
    }
}
