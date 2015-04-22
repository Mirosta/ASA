package com.tw10g12.ASA.Model.JSON

import com.tw10g12.ASA.Model.ATAM.ATAMTile
import com.tw10g12.ASA.Model.SMTAM.SMTAMTile
import com.tw10g12.ASA.Model.StateMachine.StateMachine
import com.tw10g12.ASA.Model._
import com.tw10g12.ASA.Util
import com.tw10g12.Maths.Vector3
import org.json.JSONObject

import scala.util.Random

/**
 * Created by Tom on 19/04/2015.
 */
object JSONSimulationFactory
{
    val simulationFactories: Map[String, JSONSimulationFactory] = Map[String, JSONSimulationFactory]("Simulation" -> new JSONSimulationFactoryImp(), "KTAMSimulation" -> new JSONKTAMSimulationFactory(), "SMTAMSimulation" -> new JSONSMTAMSimulationFactory())

    def createSimulation(serialized: JSONObject): Simulation =
    {
        val simulationClassID: String = serialized.getString("simulationClassID")
        val tileTypes = Util.IOUtil.JSONtoTileset(serialized.getJSONObject("tileset"))
        val tilesArr = Util.IOUtil.JSONArrayToArray[JSONObject](serialized.getJSONArray("tiles"))
        val tiles = tilesArr.map(serialized => JSONToTilePair(serialized, tileTypes)).toMap
        val adjacenciesArray = Util.IOUtil.JSONArrayToArray[JSONObject](serialized.getJSONArray("adjacencies"))
        val adjacencies: Map[Vector3, List[(Int, Double)]] = adjacenciesArray.map(serializedPair => JSONToAdjacencyPair(serializedPair)).toMap

        val stats = if(serialized.has("stats")) JSONSimulationStatisticsFactory.createSimulationStatistics(serialized.getJSONObject("stats")) else new SimulationStatistics()

        val simulationState = simulationFactories(simulationClassID).createSimulationState(serialized, tiles, tileTypes._2, adjacencies, tileTypes._3, stats)
        val simulation = simulationFactories(simulationClassID).createSimulation(tileTypes._1,tileTypes._2.toVector, Util.IOUtil.JSONtoRandom(serialized.getJSONObject("rnd")), simulationState)

        return simulation
    }

    def JSONToTilePair(serialized: JSONObject, tileTypes: (Tile, List[Tile], Map[Tile, StateMachine])): (Vector3, Tile) =
    {
        val pos = Util.IOUtil.JSONToVector3(serialized.getJSONObject("key"))
        val tileTypeID = serialized.getInt("value")
        val tile = if(tileTypeID == -1) tileTypes._1.clone(pos, Vector()) else tileTypes._2(tileTypeID).clone(pos, Vector())
        return (pos, tile)
    }

    def JSONToAdjacencyPair(serialized: JSONObject): (Vector3, List[(Int, Double)]) =
    {
        val valueArray = Util.IOUtil.JSONArrayToArray[JSONObject](serialized.getJSONArray("value"))
        val value: List[(Int, Double)] = valueArray.map(serializedPair => (serializedPair.getInt("tileType"), serializedPair.getDouble("probability"))).toList
        return (Util.IOUtil.JSONToVector3(serialized.getJSONObject("key")), value)
    }
}

trait JSONSimulationFactory
{
    def createSimulation(startingTile: Tile, tileTypes: Vector[Tile], rnd: Random, state: SimulationState): Simulation

    def createSimulationState (serialized: JSONObject, tiles: Map[Vector3, Tile], tileTypes: List[Tile], adjacencies: Map[Vector3, List[(Int, Double)]], tileStateMachines: Map[Tile, StateMachine], stats: SimulationStatistics): SimulationState
}
class JSONSimulationFactoryImp extends JSONSimulationFactory
{
    def createSimulationState(serialized: JSONObject, tiles: Map[Vector3, Tile], tileTypes: List[Tile], adjacencies: Map[Vector3, List[(Int, Double)]], tileStateMachines: Map[Tile, StateMachine], stats: SimulationStatistics): SimulationState =
    {
        return new SimulationState(tiles, tileTypes.toVector, adjacencies, stats)
    }

    override def createSimulation(startingTile: Tile, tileTypes: Vector[Tile], rnd: Random, state: SimulationState): Simulation =
    {
        val newSim = new Simulation(startingTile, tileTypes, rnd)
        newSim.state = state
        return newSim
    }
}

class JSONKTAMSimulationFactory extends JSONSimulationFactory
{
    override def createSimulationState(serialized: JSONObject, tiles: Map[Vector3, Tile], tileTypes: List[Tile], adjacencies: Map[Vector3, List[(Int, Double)]], tileStateMachines: Map[Tile, StateMachine], stats: SimulationStatistics): SimulationState =
    {
        val removeTileObj = serialized.getJSONObject("removeTileProbabilities")
        val removeTileArray = Util.IOUtil.JSONArrayToArray[JSONObject](removeTileObj.getJSONArray("map"))
        val removeTileProbabilities: (Double, Map[Vector3, Double]) = (removeTileObj.getDouble("total"), removeTileArray.map(serializedPair => JSONToRemovePair(serializedPair)).toMap)
        val backwardConstant = serialized.getDouble("backwardConstant")
        val forwardConstant = serialized.getDouble("forwardConstant")
        val removeTick = serialized.getBoolean("removeTick")

        return new KTAMSimulationState(tiles, tileTypes.toVector, adjacencies, removeTileProbabilities, backwardConstant, forwardConstant, removeTick, stats)
    }

    def JSONToRemovePair(serialized: JSONObject): (Vector3, Double) =
    {
        return (Util.IOUtil.JSONToVector3(serialized.getJSONObject("key")), serialized.getDouble("value"))
    }

    override def createSimulation(startingTile: Tile, tileTypes: Vector[Tile], rnd: Random, state: SimulationState): Simulation =
    {
        val ktamState = state.asInstanceOf[KTAMSimulationState]
        val newSim = new KTAMSimulation(startingTile, tileTypes, ktamState.backwardConstant, ktamState.forwardConstant, rnd)
        newSim.state = state
        return newSim
    }
}

class JSONSMTAMSimulationFactory extends JSONSimulationFactory
{
    override def createSimulationState(serialized: JSONObject, tiles: Map[Vector3, Tile], tileTypes: List[Tile], adjacencies: Map[Vector3, List[(Int, Double)]], tileStateMachines: Map[Tile, StateMachine], stats: SimulationStatistics): SimulationState =
    {
        val stateMachineStateArray = if(serialized.has("stateMachineStates")) Util.IOUtil.JSONArrayToArray[JSONObject](serialized.getJSONArray("stateMachineStates")) else null
        val stateMachineStates: Map[Vector3, StateMachine] = if(stateMachineStateArray == null) Map() else stateMachineStateArray.map(serializedPair => JSONToStatePair(serializedPair)).toMap
        val checkConnected = serialized.getBoolean("checkConnected")
        val smTileTypes = tileTypes.map(tile => if(tile.isInstanceOf[ATAMTile] && !tile.isInstanceOf[SMTAMTile]) new SMTAMTile(tile.asInstanceOf[ATAMTile]) else tile)
        val smTiles = tiles.map(pair => if(pair._2.isInstanceOf[ATAMTile] && !pair._2.isInstanceOf[SMTAMTile]) (pair._1, new SMTAMTile(pair._2.asInstanceOf[ATAMTile])) else pair)
        return new SMTAMSimulationState(smTiles, smTileTypes.toVector, adjacencies, stateMachineStates, tileStateMachines, checkConnected, stats)
    }

    def JSONToStatePair(serialized: JSONObject): (Vector3, StateMachine) =
    {
        val stateMachine: StateMachine = JSONStateMachineFactory.createStateMachine(serialized.getJSONObject("value"))
        (Util.IOUtil.JSONToVector3(serialized.getJSONObject("key")), stateMachine)
    }

    override def createSimulation(startingTile: Tile, tileTypes: Vector[Tile], rnd: Random, state: SimulationState): Simulation =
    {
        val smtamState = state.asInstanceOf[SMTAMSimulationState]
        val newSim = new SMTAMSimulation(startingTile, tileTypes, smtamState.tileStateMachines, smtamState.checkConnected, rnd)
        newSim.state = state
        return newSim
    }
}
