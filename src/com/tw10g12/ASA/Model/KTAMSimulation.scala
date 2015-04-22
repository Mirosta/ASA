package com.tw10g12.ASA.Model

import scala.util.Random

/**
 * Created by Tom on 03/03/2015.
 */
class KTAMSimulation(startingTile: Tile, tileTypes: Vector[Tile], val backwardConstant: Double, val forwardConstant: Double, rnd: Random) extends Simulation(startingTile, tileTypes, rnd)
{
    def this(startingTile: Tile, tileTypes: Vector[Tile], backwardConstant: Double, forwardConstant: Double) = this(startingTile, tileTypes, backwardConstant, forwardConstant, new Random())

    override def reset(): Unit =
    {
        state = new KTAMSimulationState(startingTile, tileTypes, backwardConstant, forwardConstant)
    }
}
