package com.ASA.Controller

import java.util.Date

import com.ASA.Debug.Profiler
import com.ASA.Model.{SimulationState, Simulation}
import com.ASA.Util

import scala.actors.threadpool.Future

/**
 * Created by Tom on 03/11/2014.
 */
class SimulationController(val simulation: Simulation)
{
    var future: Future = null

    def beginSimulation(): Unit =
    {

        future = Util.threadPool.submit(Util.toRunnable(doSimulation))
    }

    def endSimulation(): Unit =
    {
        if(future != null) future.cancel(true)
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
