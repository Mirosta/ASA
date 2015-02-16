package com.ASA.Model

import com.ASA.Debug.Profiler

import scala.util.Random

/**
 * Created by Tom on 27/10/2014.
 */
class Simulation(startingTile: Tile, tileTypes: Vector[Tile])
{

    var state: SimulationState = new SimulationState(startingTile, tileTypes)
    val rnd = new Random()

    def tick(): Unit =
    {
        state = state.nextState(rnd)
        Profiler.profile("Created new state")
    }

    def reset(): Unit =
    {
        state = new SimulationState(startingTile, tileTypes)
    }

    def getTileTypes(): (Tile, Vector[Tile]) =
    {
        return (startingTile, tileTypes)
    }
}
