package com.tw10g12.ASA.Controller

import com.tw10g12.ASA.Debug.Profiler
import com.tw10g12.ASA.Model.{Simulation, SimulationState}
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

class SimulationController(val simulation: Simulation)
{

    var future: Future = null
    var state = SimulationState.Stopped

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
        simulation.reset()
    }

    def doSimulation(): Unit =
    {
        while(!Thread.interrupted())
        {
            //Profiler.start()
            Profiler.profile("Begin profiling")
            simulation.tick()
            Profiler.profile("Tick complete")
            //Profiler.print()
        }
        println("Interrupted")
    }

    def getSimulationState(): SimulationState =
    {
        return simulation.state
    }
}
