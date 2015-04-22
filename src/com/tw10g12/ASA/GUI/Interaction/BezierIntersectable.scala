package com.tw10g12.ASA.GUI.Interaction

import com.tw10g12.Maths.{BezierUtil, BezierCurve, Vector3, Ray3}

/**
 * Created by Tom on 07/04/2015.
 */
object BezierIntersectableUtil
{
    def getBezierBoundingBox(start: Vector3, end: Vector3, startControl: Vector3, endControl: Vector3, detail: Int, maxDistance: Double): (Vector3, Vector3) =
    {
        val bezier = new BezierCurve(start, startControl, end, endControl)
        val boundingBox = bezier.getBoundingBox(detail)
        val sizeDelta = new Vector3(maxDistance, maxDistance, maxDistance)
        val size = boundingBox(1).subtract(boundingBox(0)).add(sizeDelta.multiply(2))

        return (boundingBox(0).subtract(sizeDelta).add(size.multiply(new Vector3(0, 0.0, 1))), size) //Increase bounding box by max distance from line
    }
}

class BezierIntersectable(start: Vector3 , end: Vector3, startControl: Vector3, endControl: Vector3, maxDistance: Double, detail: Int, attachedModelObject: AnyRef) extends AABBIntersectable(BezierIntersectableUtil.getBezierBoundingBox(start, end, startControl, endControl, detail, maxDistance), attachedModelObject)
{
    def this(bezierCurve: BezierCurve, maxDistance: Double, detail: Int, attachedModelObject: AnyRef) = this(bezierCurve.getStart, bezierCurve.getEnd, bezierCurve.getStartControl, bezierCurve.getEndControl, maxDistance, detail, attachedModelObject)

    override def rayIntersects(ray: Ray3): Double =
    {
        //if(super.rayIntersects(ray).isNaN)
        //{
        //    return Double.NaN
        //}
        return detailedIntersect(ray)._1
    }

    def getIntersectDistances(ray: Ray3) : List[(Double, Double, Vector3)] =
    {
        var prevPoint: Vector3 = BezierUtil.calculateBezierPoint(0.0, start, end, startControl, endControl)

        val distances: List[(Double, Double, Vector3)] = (1 to detail).map(i =>
        {
            val progress = i.asInstanceOf[Double] / detail.asInstanceOf[Double]
            val nextPoint: Vector3 = BezierUtil.calculateBezierPoint(progress, start, end, startControl, endControl)
            val lineSegRay = new Ray3(prevPoint, nextPoint.subtract(prevPoint), true)
            val retVal = calculateSkewDistance(lineSegRay, lineSegRay.getPointAlongRay(nextPoint), ray)
            prevPoint = nextPoint
            (retVal._1, retVal._2, nextPoint)
        }
        ).filter(pair => pair._1 < maxDistance).sortWith((pair1, pair2) => if(pair1._1 == pair2._1) pair1._2 < pair2._2 else pair1._1 < pair2._1).toList
        return distances
    }

    def detailedIntersect(ray: Ray3): (Double, Vector3) =
    {
        val distances = getIntersectDistances(ray)
        if(distances.isEmpty) return (Double.NaN, null)
        else return (distances.head._2, distances.head._3)
    }

    def calculateSkewDistance(ray1: Ray3, maxPoint: Double, ray2: Ray3): (Double, Double, Vector3) =
    {
        val crossedDirections = ray1.getRayDir.cross(ray2.getRayDir)
        val diffVector = ray1.getRayStart.subtract(ray2.getRayStart)
        if(crossedDirections.lengthSquared() == 0)
        {
            val projected = ray1.getRayDir.multiply(ray1.getRayDir.dot(diffVector) / ray1.getRayDir.dot(ray1.getRayDir))
            return (projected.lengthSquared(), 0, ray2.getRayStart)
        }

        val selfDotCrossDir = crossedDirections.dot(crossedDirections)
        val toDot = diffVector.cross(crossedDirections.divide(selfDotCrossDir))
        var lineOneOffset = toDot.dot(ray2.getRayDir)
        if(lineOneOffset < 0)lineOneOffset = 0
        else if(lineOneOffset > maxPoint) lineOneOffset = maxPoint

        val lineTwoOffset = toDot.dot(ray1.getRayDir)
        val pointOnLineOne = ray1.getPointAlongRay(lineOneOffset)
        return (pointOnLineOne.subtract(ray2.getPointAlongRay(lineTwoOffset)).lengthSquared(), lineTwoOffset, pointOnLineOne)
    }
}
