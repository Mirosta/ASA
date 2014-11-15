package com.ASA.GUI.Draw

import java.util.Date

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
        drawTools.setModelView(camera.getMatrix(1.0))
        drawTools.start()
        //println("Rendering Main")
        //Matrix4.getIdentityMatrix.multiply(Matrix4.getTranslationMatrix(new Vector3(0,0,-100)).multiply(Matrix4.getRotationY(Math.sin(new Date().getTime()/20000.0)*180))));
        //drawTools.drawCuboid(new Vector3(0,0,0), new Vector3(10.0,10.0,10.0), Array(Colour.Green))

    }

    def after(drawTools: DrawTools): Unit =
    {
        drawTools.end()
    }
}
