package com.ASA.GUI.Draw

import com.tw10g12.Draw.Engine.{Camera, DrawTools, Colour}
import com.tw10g12.Maths.{Vector3, Matrix4}

/**
 * Created by Tom on 27/10/2014.
 */
object RenderMain
{
    def before(drawTools: DrawTools, camera: Camera): Unit=
    {
        drawTools.clear(Colour.White)
        drawTools.start()
        //println("Rendering Main")
        drawTools.setModelView(Matrix4.getIdentityMatrix)
        drawTools.drawCuboid(new Vector3(0,0,-10), new Vector3(10.0,10.0,10.0), Array(Colour.Green))

    }

    def after(drawTools: DrawTools): Unit =
    {
        drawTools.end()
    }
}
