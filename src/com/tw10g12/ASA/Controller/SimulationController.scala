package com.tw10g12.ASA.Controller

import com.tw10g12.ASA.Debug.Profiler
import com.tw10g12.ASA.Model.{Tile, Simulation, SimulationState}
import com.tw10g12.ASA.Util

import scala.actors.threadpool.Future

/**
 * Created by Tom on 03/11/2014.
 */

object SimulationState extends Enumeration
{
    type SimulationState = Value
    val Stopped, Running, Paused = Value
}

class SimulationController(var simulation: Simulation)
{
    var future: Future = null
    var state = SimulationState.Stopped
    var nextSimulation: Simulation = null
    var nextTiles: (Tile, Vector[Tile]) = null
    var simulationSpeed: Double = 1.0

    def beginSimulation(): Unit =
    {
        future = Util.threadPool.submit(Util.toRunnable(doSimulation))
        state = SimulationState.Running
    }

    def pauseSimulation(): Unit =
    {
        if(future != null) future.cancel(true)
        state = SimulationState.Paused
    }

    def stopSimulation(): Unit =
    {
        pauseSimulation()
        resetSimulation()
        state = SimulationState.Stopped
    }

    def resetSimulation(): Unit =
    {
        moveToNextSimulation()
        simulation.reset()
    }

    def doSimulation(): Unit =
    {
        var done = false
        while(!Thread.interrupted() && !done)
        {
            //Profiler.start()
            Profiler.profile("Begin profiling")
            simulation.tick()
            Profiler.profile("Tick complete")
            //Profiler.print()
            try
            {
                if(simulationSpeed < 1) Thread.sleep(Math.ceil((1.0 / simulationSpeed) * 1.0).asInstanceOf[Long])
            }
            catch
            {
                case ex: InterruptedException => done = true
            }
        }
        println("Interrupted")
    }

    def getSimulationState(): SimulationState =
    {
        return simulation.state
    }

    def setSimulation(nextSimulation: Simulation): Unit =
    {
        this.nextSimulation = nextSimulation
        moveToNextSimulation()
    }

    def moveToNextSimulation(): Unit =
    {
        if((future == null || future.isDone) && nextSimulation != null)
        {
            simulation = nextSimulation
            nextSimulation = null
        }
    }

    def getTileTypes(): (Tile, Vector[Tile]) =
    {
        if(nextTiles == null) return simulation.getTileTypes()
        else nextTiles
    }

    def setTileTypes(tileTypes: (Tile, Vector[Tile])): Unit =
    {
        nextTiles = tileTypes
    }

    def setSimulationSpeed(simulationSpeed: Double): Unit =
    {
        this.simulationSpeed = simulationSpeed
    }
}
