package com.tw10g12.ASA.Model

import com.tw10g12.ASA.Debug.Profiler
import com.tw10g12.ASA.Model.StateMachine.StateMachine
import com.tw10g12.ASA.Util
import com.tw10g12.IO.JSONSerializable
import org.json.JSONObject

import scala.util.Random

/**
 * Created by Tom on 27/10/2014.
 */
class Simulation(startingTile: Tile, tileTypes: Vector[Tile], rnd: Random) extends JSONSerializable
{
    def this(startingTile: Tile, tileTypes: Vector[Tile]) = this(startingTile, tileTypes, new Random())

    var state: SimulationState = null

    reset()

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

    override def toJSON(obj: JSONObject): JSONObject =
    {
        val stateMachines: Map[Tile, StateMachine] = if(state.isInstanceOf[SMTAMSimulationState]) state.asInstanceOf[SMTAMSimulationState].tileStateMachines else Map()
        obj.put("rnd", Util.IOUtil.randomToJSON(rnd))
        obj.put("tileset", Util.IOUtil.tilesetToJSON((startingTile, tileTypes.toList), stateMachines))
        state.toJSON(obj)
        return obj
    }
}
