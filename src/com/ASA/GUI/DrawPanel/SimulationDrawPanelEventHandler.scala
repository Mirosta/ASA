package com.ASA.GUI.DrawPanel

import javax.media.opengl.GLAutoDrawable

import com.ASA.Controller.SimulationController
import com.ASA.GUI.Draw.RenderSimulation
import com.tw10g12.Draw.Engine.{Camera, OrbitCamera}
import com.tw10g12.Maths.Vector3

/**
 * Created by Tom on 20/10/2014.
 */
class SimulationDrawPanelEventHandler(val simulationController: SimulationController) extends DrawPanelEventHandler
{

    override def setupCamera(): Camera =
    {
        return new OrbitCamera(new Vector3(-1450, 1400, 0.0), 2700)
    }

    override def render(drawable: GLAutoDrawable): Unit =
    {
        RenderSimulation.render(simulationController.getSimulationState, camera.asInstanceOf[OrbitCamera], drawTools)
    }

}
