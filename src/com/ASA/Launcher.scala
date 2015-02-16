package com.ASA

import com.ASA.Controller.SimulationController
import com.ASA.GUI.{EditorWindow, SimulationWindow}
import com.ASA.Model.ATAM.{ATAMGlue, ATAMTile}
import com.ASA.Model.{Simulation, Tile}
import com.tw10g12.Draw.Engine.Colour
import com.tw10g12.Maths.Vector3

import scala.actors.threadpool.TimeUnit

/**
 * Created by Tom on 20/10/2014.
 */
object Launcher
{
    lazy val simulationController: SimulationController = new SimulationController(getSimulation())
    lazy val simulationWindow: SimulationWindow = new SimulationWindow(simulationController)
    lazy val editorWindow: EditorWindow = new EditorWindow((null, List[Tile]()))

    def main(args:Array[String]): Unit =
    {

        simulationWindow.onSetup = null
        simulationWindow.onClosed = Util.toRunnable(() => onClose(simulationController))
        simulationWindow.startSetup()

        editorWindow.setVisible(false)
        editorWindow.onSetup = null
        editorWindow.startSetup()
    }

    def onClose(simulationController: SimulationController): Unit =
    {
        simulationController.pauseSimulation()
        Util.threadPool.shutdownNow()
        Util.threadPool.awaitTermination(0, TimeUnit.SECONDS)
        println("Threadpool terminated")
    }

    def getSimulation(): Simulation =
    {
        val startingTile = new ATAMTile(Vector(new ATAMGlue("N", 2), null, null, new ATAMGlue("W", 2)), Vector[Colour](Colour.PleasantBlue), new Vector3(0,0,0), -1)
        val otherTiles = Vector[Tile](
            new ATAMTile(Vector(new ATAMGlue("N", 2), null, new ATAMGlue("N", 2), new ATAMGlue("1", 1)), Vector[Colour](Colour.PleasantBlue), new Vector3(0,0,0), 0),
            new ATAMTile(Vector(new ATAMGlue("1", 1), new ATAMGlue("W", 2), null, new ATAMGlue("W", 2)), Vector[Colour](Colour.PleasantBlue), new Vector3(0,0,0), 1),
            new ATAMTile(Vector(new ATAMGlue("1", 1), new ATAMGlue("0", 1), new ATAMGlue("1", 1), new ATAMGlue("1", 1)), Vector[Colour](Colour.PleasantBlue), new Vector3(0,0,0), 2),
            new ATAMTile(Vector(new ATAMGlue("1", 1), new ATAMGlue("1", 1), new ATAMGlue("0", 1), new ATAMGlue("1", 1)), Vector[Colour](Colour.PleasantBlue), new Vector3(0,0,0), 3),
            new ATAMTile(Vector(new ATAMGlue("0", 1), new ATAMGlue("0", 1), new ATAMGlue("0", 1), new ATAMGlue("0", 1)), Vector[Colour](Colour.Orange), new Vector3(0,0,0), 4),
            new ATAMTile(Vector(new ATAMGlue("0", 1), new ATAMGlue("1", 1), new ATAMGlue("1", 1), new ATAMGlue("0", 1)), Vector[Colour](Colour.Orange), new Vector3(0,0,0), 5)
        )
        val adderTiles = Vector[Tile](
            new ATAMTile(Vector(new ATAMGlue("N", 2), null, new ATAMGlue("N", 2), new ATAMGlue("1", 1)), Vector[Colour](Colour.Green), new Vector3(0,0,0), 0),
            new ATAMTile(Vector(new ATAMGlue("1", 1), new ATAMGlue("W", 2), null, new ATAMGlue("W", 2)), Vector[Colour](Colour.Green), new Vector3(0,0,0), 1),
            new ATAMTile(Vector(new ATAMGlue("1", 1), new ATAMGlue("0", 1), new ATAMGlue("1", 1), new ATAMGlue("0", 1)), Vector[Colour](Colour.PleasantBlue), new Vector3(0,0,0), 2),
            new ATAMTile(Vector(new ATAMGlue("1", 1), new ATAMGlue("1", 1), new ATAMGlue("0", 1), new ATAMGlue("0", 1)), Vector[Colour](Colour.PleasantBlue), new Vector3(0,0,0), 3),
            new ATAMTile(Vector(new ATAMGlue("0", 1), new ATAMGlue("0", 1), new ATAMGlue("0", 1), new ATAMGlue("0", 1)), Vector[Colour](Colour.Orange), new Vector3(0,0,0), 4),
            new ATAMTile(Vector(new ATAMGlue("0", 1), new ATAMGlue("1", 1), new ATAMGlue("1", 1), new ATAMGlue("1", 1)), Vector[Colour](Colour.Orange), new Vector3(0,0,0), 5)
        )
        return new Simulation(startingTile, adderTiles)
    }
}
