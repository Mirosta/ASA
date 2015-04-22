package com.tw10g12.ASA.Model

import com.tw10g12.ASA.Model.StateMachine.StateMachine

import scala.util.Random

/**
 * Created by Tom on 29/03/2015.
 */
class SMTAMSimulation(startingTile: Tile, tileTypes: Vector[Tile], tileStateMachines: Map[Tile, StateMachine], val checkConnected: Boolean, rnd: Random) extends Simulation(startingTile, tileTypes, rnd)
{
    def this(startingTile: Tile, tileTypes: Vector[Tile], tileStateMachines: Map[Tile, StateMachine], checkConnected: Boolean) = this(startingTile, tileTypes, tileStateMachines, checkConnected, new Random())

    override def reset(): Unit =
    {
        state = new SMTAMSimulationState(startingTile, tileTypes, tileStateMachines, checkConnected)
    }
}
