package com.tw10g12.ASA.Model

/**
 * Created by Tom on 03/03/2015.
 */
class KTAMSimulation(startingTile: Tile, tileTypes: Vector[Tile], backwardConstant: Double, forwardConstant: Double) extends Simulation(startingTile, tileTypes)
{
    override def reset(): Unit =
    {
        state = new KTAMSimulationState(startingTile, tileTypes, backwardConstant, forwardConstant)
    }
}
