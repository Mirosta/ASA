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
        drawTools.setModelView(camera.getMatrix(1.0))
        //drawTools.drawCuboid(new Vector3(15,-10,-0.05), new Vector3(0.5,1.5,0.1), Array(Colour.Green))

    }

    def after(drawTools: DrawTools): Unit =
    {
        drawTools.end()
    }
}
