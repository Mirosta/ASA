package com.tw10g12.ASA.GUI.Interaction

import com.tw10g12.Maths.Ray3

/**
 * Created by Tom on 16/02/2015.
 */
trait Intersectable
{
    def rayIntersects(ray: Ray3): Double
    def getAttachedModelObject: AnyRef
    def setAttachedModelObject(modelObject: AnyRef): Unit
}
