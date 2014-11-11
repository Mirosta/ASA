package com.ASA.GUI.Draw

import com.ASA.Model.{SimulationState}
import com.tw10g12.Draw.Engine.{OrbitCamera, DrawTools}

/**
 * Created by Tom on 27/10/2014.
 */
object RenderSimulation
{
    def render(simulationState: SimulationState, camera: OrbitCamera, drawTools: DrawTools): Unit =
    {
       // simulationState.tiles.map(keyValue => if(keyValue._2 != null)RenderATAMTile.renderTile(keyValue._2, getLOD(RenderATAMTile.getDistanceSq(camera.getActualCameraPos, keyValue._2)), drawTools))
    }

    def getLOD(distance: Double): Int =
    {
        val absDistance: Double = Math.abs(distance)
        if(absDistance < 50*50) return 6
        if(absDistance < 100*100) return 5
        if(absDistance < 200*200) return 4
        if(absDistance < 300*300) return 3
        if(absDistance < 450*450) return 2
        if(absDistance < 600*600) return 1

        return 0
    }
}
