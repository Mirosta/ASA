package com.tw10g12.ASA.Model

import com.tw10g12.ASA.Debug.Profiler
import com.tw10g12.ASA.Model.ATAM.ATAMTile
import com.tw10g12.ASA.Model.SMTAM.SMTAMTile
import com.tw10g12.ASA.Model.StateMachine.GlueState.GlueState
import com.tw10g12.ASA.Model.StateMachine.{GlueState, StateMachine}
import com.tw10g12.ASA.Util
import com.tw10g12.Maths.Vector3
import org.json.{JSONArray, JSONObject}

import scala.collection.mutable
import scala.util.Random

/**
 * Created by Tom on 29/03/2015.
 */
class SMTAMSimulationState(startingTiles: Map[Vector3, Tile], tileTypes: Vector[Tile], startingAdjacencies: Map[Vector3, List[(Int, Double)]], val stateMachineStates: Map[Vector3, StateMachine], val tileStateMachines: Map[Tile, StateMachine], val checkConnected: Boolean, stats: SimulationStatistics) extends SimulationState(startingTiles, tileTypes, startingAdjacencies, stats)
{
    def this(startingTile: Tile, tileTypes: Vector[Tile], tileStateMachines: Map[Tile, StateMachine], checkConnected: Boolean) = this(Map((startingTile.getPosition -> startingTile)), tileTypes, null, if(tileStateMachines.contains(startingTile)) Map(startingTile.getPosition -> tileStateMachines(startingTile)) else Map[Vector3, StateMachine](), tileStateMachines, checkConnected, new SimulationStatistics())

    //Calculate new tile, update adjacent tiles, update all state machines, update bindings, remove necessary tiles

    override def nextState(rnd: Random): SimulationState =
    {
        val updateResult = nextAddState(rnd)
        return updateResult._1.nextRemoveState(rnd, updateResult._2)
    }

    def isNodeRemoveable(pos: Vector3): Boolean =
    {
        if(!stateMachineStates.contains(pos)) return false
        if((0 until 4).exists(index => !stateMachineStates(pos).currentGlueStates.contains(index))) return false
        if(stateMachineStates(pos).currentGlueStates.exists(gluePair => !gluePair._2.eq(GlueState.Disabled))) return false

        return true
    }


    //Calculate removed tiles,
    def nextRemoveState(rnd: Random, updates: Map[Int, Set[Vector3]]): SMTAMSimulationState =
    {
        if(!updates.contains(-1)) return this
        val connected: Set[Vector3] = if(this.checkConnected) getConnected(tiles, stateMachineStates) else tiles.filter(pair => !isNodeRemoveable(pair._1)).keySet
        val removeTilePos: Set[Vector3] = tiles.filter(pair => !connected.contains(pair._1)).map(pair => pair._1).toSet

        return removeTiles(removeTilePos)
    }

    def removeTiles(removeTilePos: Set[Vector3]) : SMTAMSimulationState =
    {
        try
        {
            val fullRemoveAdjacents: List[(Vector3, Vector[(Tile, Int)])] = removeTilePos.toList.map(pos => (pos, getFullAdjacents(pos, tiles)))
            val removeAdjacentTiles: Map[Vector3, Vector3] = fullRemoveAdjacents.flatMap(pair => pair._2.map(innerPair => innerPair._1.getPosition -> pair._1)).toMap

            val newTuples: List[(Vector3, Tile)] = removeAdjacentTiles.filter(pair => !removeTilePos.contains(pair._2)).toList.map(pair => getTileAt(pair._1, tiles).clone(pair._2, false)).map(tile => (tile.getPosition, tile))

            val newTiles = tiles -- removeTilePos ++ newTuples
            val modifiedSides: Set[(Tile, Int)] = fullRemoveAdjacents.filter(pair => pair._2.exists(innerPair => !removeTilePos.contains(innerPair._1.getPosition))).flatMap(pair => pair._2.map(innerPair => (getTileAt(pair._1, newTiles), innerPair._2))).filter(pair => pair._1 != null).toSet

            val newStateMachineStates = updateStateMachines(stateMachineStates, modifiedSides.toVector, null, null, false)
            val reduntantAdjacencies = removeTilePos.flatMap(pos => getEmptyAdjacents(tiles(pos), newTiles, newStateMachineStates._1)).toSet.filter(adjPos => !getFullAdjacents(adjPos, newTiles).exists(tileOrientationPair => !removeTilePos.contains(tileOrientationPair._1.getPosition)))

            val sMRemoveUpdates: Set[Vector3] = if (newStateMachineStates._2.contains(-1)) newStateMachineStates._2(-1).flatMap(pos => getEmptyAdjacents(newTiles(pos), newTiles, newStateMachineStates._1)).toSet else Set[Vector3]()
            val sMAddUpdates = if (newStateMachineStates._2.contains(1)) newStateMachineStates._2(1).flatMap(pos => getEmptyAdjacents(newTiles(pos), newTiles, newStateMachineStates._1)).toSet else Set[Vector3]()

            val newAdjacentTiles = (sMRemoveUpdates ++ sMAddUpdates ++ removeTilePos).map(position => position -> calculateMonteCarloChances(position, newTiles, newStateMachineStates._1)).filter(pair => pair._2.length > 0 && getFullAdjacents(pair._1, newTiles).size > 0)
            val newAdjacencies = adjacencies -- reduntantAdjacencies -- newAdjacentTiles.filter(pair => pair._2.isEmpty).map(pair => pair._1) ++ newAdjacentTiles.filter(pair => !pair._2.isEmpty)

            val newStats = stats.clone()
            updateStats((removeTilePos ++ newTuples.map(pair => pair._1)).map(pos => getTileAt(pos, tiles)).toList, newTuples.map(pair => pair._2), tiles, newTiles, newStats)

            return new SMTAMSimulationState(newTiles, tileTypes, newAdjacencies, newStateMachineStates._1, tileStateMachines, checkConnected, newStats)
        }
        catch
        {
            case e: Exception =>
            {
                println(e.getMessage)
                e.printStackTrace()
                return this
            }
        }

    }

    def nextAddState(rnd: Random): (SMTAMSimulationState, Map[Int, Set[Vector3]]) =
    {
        Profiler.profile("Begin next state")
        val nextTile = if(adjacencies.size == 0) -1 else rnd.nextInt(adjacencies.size)
        Profiler.profile("Picked next tile")
        val adjacency = if(adjacencies.size == 0) (null, List()) else adjacencies.toList(nextTile)
        val tileTypeIDs = if(adjacencies.size == 0) List() else adjacency._2
        val rndNo = rnd.nextDouble()

        val tileType = if(adjacencies.size == 0) -1 else getTileType(rndNo, tileTypeIDs)
        Profiler.profile("Got tile type")
        val fullAdjacents: Vector[(Tile, Int)] = if(adjacencies.size == 0) Vector() else getFullAdjacents(adjacency._1, tiles)
        val adjacentTiles:Set[Vector3] = if(adjacencies.size == 0) Set() else fullAdjacents.map(pair => pair._1.getPosition).toSet

        Profiler.profile("Got adjacent tiles")
        //println("Tile Type: " + tileType)
        if(tileType == -1)
        {
            val a = 12

            val newStateMachineStates = updateStateMachines(stateMachineStates, fullAdjacents, null, null, true)
            Profiler.profile("Cloned new tile")
            Profiler.profile("Got tiles to be updated")
            val newTiles = tiles
            Profiler.profile("Cloned full tile set")
            val sMRemoveUpdates: Set[Vector3] = if(newStateMachineStates._2.contains(-1)) newStateMachineStates._2(-1).flatMap(pos => getEmptyAdjacents(newTiles(pos), newTiles, newStateMachineStates._1)).toSet else Set[Vector3]()
            val sMAddUpdates = if(newStateMachineStates._2.contains(1)) newStateMachineStates._2(1).flatMap(pos => getEmptyAdjacents(newTiles(pos), newTiles, newStateMachineStates._1)).toSet else Set[Vector3]()
            val newAdjacentTiles = (sMRemoveUpdates ++ sMAddUpdates).map(position => position -> calculateMonteCarloChances(position, newTiles)).filter(_._2.length > 0)
            val newAdjacencies = adjacencies -- newAdjacentTiles.filter(pair => pair._2.isEmpty).map(pair => pair._1) ++ newAdjacentTiles.filter(pair => !pair._2.isEmpty)
            Profiler.profile("Cloned and updated adjacencies")

            val newStats = stats.clone()
            updateStats(List(), List(), tiles, newTiles, newStats)

            return (new SMTAMSimulationState(newTiles, this.tileTypes, newAdjacencies, newStateMachineStates._1, this.tileStateMachines, this.checkConnected, newStats), newStateMachineStates._2)
        }
        val tile = tileTypes(tileType)
        val newTile: Tile = tile.clone(adjacency._1, fullAdjacents.map(pair => pair._2))
        val stateMachine = if(tileStateMachines.contains(tile)) tileStateMachines(tile) else null
        val newStateMachineStates = updateStateMachines(stateMachineStates, fullAdjacents, stateMachine, newTile, true)
        Profiler.profile("Cloned new tile")
        val newTuples: List[(Vector3, Tile)] = (newTile :: adjacentTiles.toList.map(pos => getTileAt(pos, tiles).clone(newTile.getPosition, true))).map(tile => (tile.getPosition, tile))
        Profiler.profile("Got tiles to be updated")
        val newTiles = tiles ++ newTuples
        Profiler.profile("Cloned full tile set")
        val sMRemoveUpdates: Set[Vector3] = if(newStateMachineStates._2.contains(-1)) newStateMachineStates._2(-1).flatMap(pos => getEmptyAdjacents(newTiles(pos), newTiles, newStateMachineStates._1)).toSet else Set[Vector3]()
        val sMAddUpdates = if(newStateMachineStates._2.contains(1)) newStateMachineStates._2(1).flatMap(pos => getEmptyAdjacents(newTiles(pos), newTiles, newStateMachineStates._1)).toSet else Set[Vector3]()
        val newAdjacentTiles = (sMRemoveUpdates ++ sMAddUpdates ++ getEmptyAdjacents(newTile, newTiles, newStateMachineStates._1)).map(position => position -> calculateMonteCarloChances(position, newTiles, newStateMachineStates._1)).filter(_._2.length > 0)
        val newAdjacencies = (adjacencies - newTile.getPosition) -- newAdjacentTiles.filter(pair => pair._2.isEmpty).map(pair => pair._1) ++ newAdjacentTiles.filter(pair => !pair._2.isEmpty)
        Profiler.profile("Cloned and updated adjacencies")

        val newStats = stats.clone()
        updateStats(adjacentTiles.map(pos => getTileAt(pos, tiles)).toList, newTuples.map(pair => pair._2), tiles, newTiles, newStats)

        return (new SMTAMSimulationState(newTiles, tileTypes, newAdjacencies, newStateMachineStates._1, this.tileStateMachines, this.checkConnected, newStats), newStateMachineStates._2)
    }

    def updateStateMachines(stateMachineStates: Map[Vector3, StateMachine], adjacents: Vector[(Tile, Int)], newStateMachine: StateMachine, newTile: Tile, adding: Boolean): (Map[Vector3, StateMachine], Map[Int, Set[Vector3]]) =
    {
        val opString = if(adding) "+" else "-"
        val updates: List[String] = adjacents.map(pair => Util.orientationToHeading(pair._2) + opString).toList
        val updateResult: (StateMachine, Set[Int]) = if(newStateMachine != null) updates.foldLeft((newStateMachine, Set[Int]()))((state, input) =>
            updateStep(state._1, state._2, input)
        ) else null

        val startingMaps: (Map[Vector3, StateMachine],Map[Int, Set[Vector3]]) = if(newStateMachine != null) (Map[Vector3, StateMachine](newTile.getPosition -> updateResult._1), updateResult._2.map(update => (update -> Set[Vector3](newTile.getPosition))).toMap) else (Map(), Map())
        val result = stateMachineStates.foldLeft(startingMaps)( (curMaps, statePair) =>
        {
            val updateResult = updateStateMachine(statePair, adjacents, opString)
            val newUpdateList: Map[Int, Set[Vector3]] = updateResult._2.foldLeft(Map[Int, Set[Vector3]]())((set, update) =>
            {
                if(!set.contains(update))
                {
                    if(!curMaps._2.contains(update)) set + (update -> Set[Vector3](statePair._1)) else set + (update -> (curMaps._2(update) + statePair._1))
                }
                else set + (update -> set(update).+(statePair._1))
            })
            (curMaps._1.+(statePair._1 -> updateResult._1), curMaps._2 ++ newUpdateList)
        })

        return result
    }

    def updateStep(currentState: StateMachine, currentUpdates: Set[Int], input: String): (StateMachine, Set[Int])=
    {
        val nextState = currentState.nextState(input)
        return (nextState._1, currentUpdates ++ nextState._2)
    }

    def updateStateMachine(statePair: (Vector3, StateMachine), adjacents: Vector[(Tile, Int)], opString: String): (StateMachine, Set[Int]) =
    {
        val updates: List[String] = adjacents.filter(pair => pair._1.getPosition.equals(statePair._1)).map(pair =>
        {
            val oppositeOrientation = Util.oppositeOrientation(pair._2)
            Util.orientationToHeading(oppositeOrientation) + opString
        }).toList
        if(updates.isEmpty) return statePair._2.nextState("")
        else
        {
            return updates.foldLeft((statePair._2, Set[Int]()))((state, input) =>
            {
                return updateStep(state._1, state._2, input)
            })
        }
    }

    override def calculateAdjacencies(): Map[Vector3,List[(Int, Double)]] =
    {
        Profiler.profile("Starting adjacency calculation")
        val edgeTiles: Map[Vector3, Tile] = calculateEdgeTiles()
        val adjacentTiles: Set[Vector3] = edgeTiles.par.flatMap(tile => getEmptyAdjacents(tile._2, tiles, stateMachineStates)).seq.toSet[Vector3]
        Profiler.profile("Completed flat map, calculating monte carlo chances for " + adjacentTiles.size + " tiles")
        val retVal = adjacentTiles.toList.par.map(position => position -> calculateMonteCarloChances(position, tiles)).filter(_._2.length > 0).seq.toMap
        Profiler.profile("Finished adjacency calculation")
        return retVal
    }

    override def calculateEdgeTiles(): Map[Vector3, Tile] =
    {
        Profiler.profile("Starting edge tile calculation")
        val retVal = tiles.par.filter(keyVal => isEdgeTile(keyVal._2, if(stateMachineStates.contains(keyVal._1)) stateMachineStates(keyVal._1).currentGlueStates else Map[Int, GlueState]())).seq
        Profiler.profile("Finished edge tile calculation")
        return retVal
    }

    def isEdgeTile(currentTile: Tile, currentState: Map[Int, GlueState]): Boolean =
    {
        for(orientation <- 0 until 4)
        {
            if(currentTile.glues(orientation) != null && currentTile.glues(orientation).strength > 0 && !currentTile.glues(orientation).isBound && (!currentState.contains(orientation) || currentState(orientation) == GlueState.Active))
            {
                return true
            }
        }
        return false
    }


    override def calculateMonteCarloChances(position: Vector3, tiles: Map[Vector3, Tile]): List[(Int, Double)] = calculateMonteCarloChances(position, tiles, stateMachineStates)

    def calculateMonteCarloChances(position: Vector3, tiles: Map[Vector3, Tile], stateMachineStates: Map[Vector3, StateMachine]): List[(Int, Double)] =
    {
        if(position == null) return List[(Int, Double)]()
        val adjacentTiles: Vector[(Tile, Int)] = getFullAdjacents(position, tiles)
        val validTileTypes = tileTypes.filter(tile => filterTile(tile, adjacentTiles, stateMachineStates))
        return validTileTypes.map(tile => (tile.typeID, validTileTypes.length.asInstanceOf[Double])).toList
    }

    def filterTile(tile: Tile, adjacentTiles: Vector[(Tile, Int)], stateMachineStates: Map[Vector3, StateMachine]): Boolean =
    {
        val adjacentGlueStates = (adjacentTiles).map(pair => (pair._1.getPosition -> (if(stateMachineStates.contains(pair._1.getPosition)) stateMachineStates(pair._1.getPosition).currentGlueStates else Map[Int, GlueState]()))).toMap
        val tileGlueStates: Map[Int, GlueState] = if(tileStateMachines.contains(tile)) tileStateMachines(tile).currentGlueStates else Map()
        return tile.asInstanceOf[SMTAMTile].canBind(adjacentTiles, adjacentGlueStates, tileGlueStates)
    }

    def getEmptyAdjacents(tile: Tile, tiles: Map[Vector3, Tile], stateMachineStates: Map[Vector3, StateMachine]): List[Vector3] =
    {
        val filteredGlues = tile.glues.filter(glue => !(glue == null) && !tiles.contains(tile.getPosition.add(Util.orientationToVector(glue.orientation))) && (!stateMachineStates.contains(tile.getPosition) || !stateMachineStates(tile.getPosition).currentGlueStates.contains(glue.orientation) || stateMachineStates(tile.getPosition).currentGlueStates(glue.orientation) == GlueState.Active))
        return filteredGlues.map(glue => tile.getPosition.add(Util.orientationToVector(glue.orientation))).toList
    }

    def getConnected(tiles: Map[Vector3, Tile], stateMachineStates: Map[Vector3, StateMachine]): Set[Vector3] =
    {
        val visited: mutable.Set[Vector3] = new mutable.HashSet[Vector3]()
        val edge: mutable.Stack[Vector3] = new mutable.Stack[Vector3]()

        edge.push(new Vector3(0,0,0))
        visited.add(new Vector3(0,0,0))

        while(edge.size > 0)
        {
            val curPos = edge.pop()
            (0 to 4).map(orientation =>
            {
                val newPos = curPos.add(Util.orientationToVector(orientation))

                if(!visited.contains(newPos) && tiles.contains(newPos) && tiles(curPos).glues(orientation) != null && tiles(curPos).glues(orientation).strength > 0 && (!stateMachineStates.contains(curPos) || !stateMachineStates(curPos).currentGlueStates.contains(orientation) || stateMachineStates(curPos).currentGlueStates(orientation) == GlueState.Active))
                {
                    visited.add(newPos)
                    edge.push(newPos)
                }
            } )
        }

        return visited.toSet
    }

    def getGlueStrength(orientation: Int, tile: Tile, stateMachine: StateMachine): Int =
    {
        if(tile.glues(orientation) == null) return 0
        if(stateMachine.currentGlueStates.contains(orientation) && stateMachine.currentGlueStates(orientation) != GlueState.Active) return 0
        return tile.glues(orientation).strength
    }

    def isRedundantAdjacency(fullAdjacents: Vector[(Tile, Int)], stateMachineStates: Map[Vector3, StateMachine]): Boolean =
    {
        fullAdjacents.map(pair => getGlueStrength(Util.oppositeOrientation(pair._2), pair._1, stateMachineStates(pair._1.getPosition))).sum > 2
    }


    override def getIncorrectBindings(tile: Tile, tiles: Map[Vector3, Tile]): Set[(Vector3, Vector3)] = Set()


    override def updateStats(oldTiles: List[Tile], newTiles: List[Tile], oldSimTiles: Map[Vector3, Tile], newSimTiles: Map[Vector3, Tile], stats: SimulationStatistics): Unit =
    {
        super.updateStats(oldTiles, newTiles, oldSimTiles, newSimTiles, stats)
        stats.updateMetric("stateMachines", stateMachineStates.size)
        stats.updateMetric("disabledGlues", stateMachineStates.map(pair => pair._2.currentGlueStates.filter(pair => pair._2 == GlueState.Disabled).size).sum)
        stats.updateMetric("inertGlues", stateMachineStates.map(pair => pair._2.currentGlueStates.filter(pair => pair._2 == GlueState.Inert).size).sum)
    }

    override def setTile(tilePos: Vector3, tile: Tile): SimulationState = setTile(if(tile == null) null else if(tile.isInstanceOf[ATAMTile] && !tile.isInstanceOf[SMTAMTile]) new SMTAMTile(tile.asInstanceOf[ATAMTile]) else tile.asInstanceOf[SMTAMTile], tilePos)

    def setTile(tile: SMTAMTile, tilePos: Vector3): SimulationState =
    {
        if(tile == null && tiles.contains(tilePos))
        {
            val removeTilePos = Set(tilePos)

            return removeTiles(removeTilePos)
        }
        else if(tile != null)
        {
            val fullAdjacents: Vector[(Tile, Int)] = getFullAdjacents(tilePos, tiles)
            val adjacentTiles:Set[Vector3] = fullAdjacents.map(pair => pair._1.getPosition).toSet

            val newTile = tile.clone(tilePos, fullAdjacents.map(pair => pair._2))
            val stateMachine = if(tileStateMachines.contains(tile)) tileStateMachines(tile) else null
            val newStateMachineStates = updateStateMachines(stateMachineStates, fullAdjacents, stateMachine, newTile, true)
            val newTuples: List[(Vector3, Tile)] = (newTile :: adjacentTiles.toList.map(pos => getTileAt(pos, tiles).clone(newTile.getPosition, true))).map(tile => (tile.getPosition, tile))
            val newTiles = tiles ++ newTuples

            val redundantAdjacents: List[Vector3] = if(tiles.contains(tilePos))
            {
                val emptyAdj: List[Vector3] = (0 until 4).map(orientation => tilePos.add(Util.orientationToVector(orientation))).filter(pos => !tiles.contains(pos)).toList;
                emptyAdj.filter(adj => isRedundantAdjacency(getFullAdjacents(adj, newTiles), newStateMachineStates._1))
            } else List(tilePos)

            val sMRemoveUpdates: Set[Vector3] = if(newStateMachineStates._2.contains(-1)) newStateMachineStates._2(-1).flatMap(pos => getEmptyAdjacents(newTiles(pos), newTiles, newStateMachineStates._1)).toSet else Set[Vector3]()
            val sMAddUpdates = if(newStateMachineStates._2.contains(1)) newStateMachineStates._2(1).flatMap(pos => getEmptyAdjacents(newTiles(pos), newTiles, newStateMachineStates._1)).toSet else Set[Vector3]()
            val newAdjacentTiles = (sMRemoveUpdates ++ sMAddUpdates ++ getEmptyAdjacents(newTile, tiles, newStateMachineStates._1)).map(position => position -> calculateMonteCarloChances(position, newTiles)).filter(_._2.length > 0)
            val newAdjacencies = (adjacencies - newTile.getPosition) -- redundantAdjacents -- newAdjacentTiles.filter(pair => pair._2.isEmpty).map(pair => pair._1) ++ newAdjacentTiles.filter(pair => !pair._2.isEmpty)
            Profiler.profile("Cloned and updated adjacencies")

            val newStats = stats.clone()
            updateStats(adjacentTiles.map(pos => getTileAt(pos, tiles)).toList, newTuples.map(pair => pair._2), tiles, newTiles, newStats)

            return new SMTAMSimulationState(newTiles, tileTypes, newAdjacencies, newStateMachineStates._1, tileStateMachines, checkConnected, newStats)
        }

        return this
    }

    override def getSimulationClassID(): String = "SMTAMSimulation"

    override def toJSON(obj: JSONObject): JSONObject =
    {
        super.toJSON(obj)
        //stateMachineStates: Map[Vector3, StateMachine], checkConnected
        obj.put("stateMachineStates", new JSONArray())
        stateMachineStates.map(pair => obj.append("stateMachineStates", createJSONStateMachinePair(pair)))
        obj.put("checkConnected", checkConnected)

        return obj
    }

    def createJSONStateMachinePair(pair: (Vector3, StateMachine)): JSONObject =
    {
        val obj = new JSONObject()
        obj.put("key", Util.IOUtil.vector3ToJSON(pair._1))
        obj.put("value", pair._2.toJSON(new JSONObject()))
        return obj
    }
}
