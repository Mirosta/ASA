package com.tw10g12.Maths

import com.tw10g12.ASA.GUI.Interaction.{AABBIntersectable, CirclePlaneIntersectable, PlaneIntersectable}
import com.tw10g12.Test.UnitSpec

/**
 * Created by Tom on 17/02/2015.
 */
class Ray3Test
extends UnitSpec
{
    "A Ray3" should "provide a method to move along the ray" in
    {
        val ray: Ray3 = new Ray3(new Vector3(10,10,10), new Vector3(1,1,1))
        ray.getPointAlongRay(0) should matchVector (10,10,10)
        ray.getPointAlongRay(1) should matchVector (11,11,11)
        ray.getPointAlongRay(2.5) should matchVector (12.5, 12.5, 12.5)
        ray.getPointAlongRay(-1.25) should matchVector (8.75, 8.75, 8.75)
    }

    "A Ray3" should "provide a way to normalise the direction" in
    {
        val ray: Ray3 = new Ray3(new Vector3(10,10,10), new Vector3(1,1,1), true)
        val ray2: Ray3 = new Ray3(new Vector3(10,10,10), new Vector3(1,1,1), false)
        ray.getRayDir.length() should be (1.0)
        ray2.getRayDir.lengthSquared() should be (3.0)
    }

    "A Ray3" should "correctly collide with a plane" in
    {
        val ray: Ray3 = new Ray3(new Vector3(0, 0, 0), new Vector3(1,2,3), true)
        val notInParallelPlane: PlaneIntersectable = new PlaneIntersectable(new Vector3(10, 2, 1), new Vector3(-3, 0, 1).normalise(), null)
        val inParallelPlane: PlaneIntersectable = new PlaneIntersectable(new Vector3(1, 2, 3), new Vector3(-3, 0, 1).normalise(), null)
        val intersectablePlane: PlaneIntersectable = new PlaneIntersectable(new Vector3(5, 0, 4), new Vector3(0, 0, 1), null)

        notInParallelPlane.rayIntersects(ray).isNaN should be (true)
        inParallelPlane.rayIntersects(ray) should be (0)
        intersectablePlane.rayIntersects(ray).isNaN should be (false)
    }

    "A Ray3" should "correctly collide with a circular plane" in
    {
        val ray: Ray3 = new Ray3(new Vector3(0, 0, 0), new Vector3(1,2,3), true)
        val notInParallelPlane: CirclePlaneIntersectable = new CirclePlaneIntersectable(new Vector3(10, 2, 1), new Vector3(-3, 0, 1).normalise(), 50,  null)
        val inParallelPlaneOutRadius: CirclePlaneIntersectable = new CirclePlaneIntersectable(new Vector3(1, 2, 3).add(new Vector3(-1,0,-3)), new Vector3(-3, 0, 1).normalise(), 1, null)
        val inParallelPlaneInRadius: CirclePlaneIntersectable = new CirclePlaneIntersectable(new Vector3(1, 2, 3), new Vector3(-3, 0, 1).normalise(),5, null)
        val intersectableInRadiusPlane: CirclePlaneIntersectable = new CirclePlaneIntersectable(new Vector3(5, 2, 3), new Vector3(0, 0, 1), 6, null)
        val intersectableOutRadiusPlane: CirclePlaneIntersectable = new CirclePlaneIntersectable(new Vector3(10, 2, 40), new Vector3(0, 1, 0), 1, null)

        //(5, 0, 4)
        //

        notInParallelPlane.rayIntersects(ray).isNaN should be (true)
        inParallelPlaneOutRadius.rayIntersects(ray).isNaN should be (true)
        inParallelPlaneInRadius.rayIntersects(ray).isNaN should be (false)
        intersectableInRadiusPlane.rayIntersects(ray).isNaN should be (false)
        intersectableOutRadiusPlane.rayIntersects(ray).isNaN should be (true)
    }

    "A Ray3" should "correctly collide with an axis aligned box" in
    {
        val ray: Ray3 = new Ray3(new Vector3(0, 0, 0), new Vector3(1,2,3), true)
        val notInBox: AABBIntersectable = new AABBIntersectable(new Vector3(10, -10, 5), new Vector3(1,2,3), null)
        val inBox: AABBIntersectable = new AABBIntersectable(new Vector3(1, -1, 1), new Vector3(4, 10, 15), null)

        notInBox.rayIntersects(ray).isNaN should be (true)
        inBox.rayIntersects(ray).isNaN should be (false)
    }
}
