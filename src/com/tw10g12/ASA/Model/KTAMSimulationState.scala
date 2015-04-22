package com.tw10g12.ASA.Model

import com.tw10g12.ASA.Debug.Profiler
import com.tw10g12.ASA.Util
import com.tw10g12.Maths.Vector3
import org.json.JSONObject

import scala.collection.mutable
import scala.util.Random

/**
 * Created by Tom on 03/03/2015.
 */
class KTAMSimulationState(startingTiles: Map[Vector3, Tile], tileTypes: Vector[Tile], startingAdjacencies: Map[Vector3, List[(Int, Double)]], startingRemoveTileProbabilities: (Double, Map[Vector3, Double]), val backwardConstant: Double, val forwardConstant: Double, removeTick: Boolean, stats: SimulationStatistics) extends SimulationState(startingTiles, tileTypes, startingAdjacencies, stats)
{
    val removeTileProbabilities: (Double, Map[Vector3, Double]) = if(startingRemoveTileProbabilities == null) calculateMaxChances(tiles.foldLeft(Map[Vector3, Double]())((curMap, tilePair) => curMap + calculateRemoveChances(tilePair._1, tiles))) else startingRemoveTileProbabilities

    def this(startingTile: Tile, tileTypes: Vector[Tile], backwardConstant: Double, forwardConstant: Double) = this(Map((startingTile.getPosition -> startingTile)), tileTypes, null, null, backwardConstant, forwardConstant, false, new SimulationStatistics())

    def calculateMaxChances(chances: Map[Vector3, Double]): (Double, Map[Vector3, Double]) =
    {
        return (chances.foldLeft(0.0)((max, pair) => if(pair._2 > max) pair._2 else max), chances)
    }

    def calculateRemoveChances(position: Vector3, tiles: Map[Vector3, Tile]): (Vector3, Double) =
    {
        return (position -> calculateRemoveChance(tiles(position), getFullAdjacents(position, tiles)))
    }

    def calculateRemoveChance(tile: Tile, adjacentTiles: Vector[(Tile, Int)]): Double =
    {
        if(tile.typeID == -1) return 0
        return Math.pow(backwardConstant, -tile.getStrength(adjacentTiles))
    }

    override def filterTile(tile: Tile, adjacentTiles: Vector[(Tile, Int)]): Boolean =
    {
        return true
    }

    override def nextState(rnd: Random): SimulationState =
    {
        val weightedForwardChance = 1.0 / forwardConstant

        if(rnd.nextDouble() > weightedForwardChance) return nextRemoveState(rnd)
        else return nextAddState(rnd)
    }

    def nextAddState(rnd: Random): KTAMSimulationState =
    {
        Profiler.profile("Begin next state")
        val nextTile = rnd.nextInt(adjacencies.size)
        Profiler.profile("Picked next tile")
        val adjacency = adjacencies.toList(nextTile)
        val tileTypeIDs = adjacency._2
        val rndNo = rnd.nextDouble()

        val tileType = getTileType(rndNo, tileTypeIDs)
        Profiler.profile("Got tile type")
        val fullAdjacents: Vector[(Tile, Int)] = getFullAdjacents(adjacency._1, tiles)
        val adjacentTiles:Set[Vector3] = fullAdjacents.map(pair => pair._1.getPosition).toSet
        Profiler.profile("Got adjacent tiles")
        //println("Tile Type: " + tileType)
        if(tileType == -1)
        {
            val a = 12
            return null
        }
        if(tileType == 0 && adjacency._1.getY == 1 && adjacency._1.getX == 0 && adjacency._1.getZ == 0)
        {
            val a = 1
        }
        val tile = tileTypes(tileType)
        val newTile: Tile = tile.clone(adjacency._1, fullAdjacents.map(pair => pair._2))
        Profiler.profile("Cloned new tile")
        val newTuples: List[(Vector3, Tile)] = (newTile :: adjacentTiles.toList.map(pos => getTileAt(pos, tiles).clone(newTile.getPosition, true))).map(tile => (tile.getPosition, tile))
        Profiler.profile("Got tiles to be updated")
        val newTiles = tiles ++ newTuples
        Profiler.profile("Cloned full tile set")
        val newAdjacentTiles = getEmptyAdjacents(newTile, tiles).map(position => position -> calculateMonteCarloChances(position, newTiles)).filter(_._2.length > 0)
        val newAdjacencies = (adjacencies - newTile.getPosition) ++ newAdjacentTiles
        val newRemoveTiles: Map[Vector3, Double] = newTiles.map(pair => calculateRemoveChances(pair._1, newTiles)).toMap
        val newRemoveTileProbabilities: Map[Vector3, Double] = removeTileProbabilities._2 ++ newRemoveTiles
        Profiler.profile("Cloned and updated adjacencies")

        val newStats = stats.clone()
        updateStats(adjacentTiles.map(pos => getTileAt(pos, tiles)).toList, newTuples.map(pair => pair._2), tiles, newTiles, newStats)

        return new KTAMSimulationState(newTiles, tileTypes, newAdjacencies, calculateMaxChances(newRemoveTileProbabilities), backwardConstant, forwardConstant, !removeTick, newStats)
    }

    def nextRemoveState(rnd: Random): KTAMSimulationState =
    {
        Profiler.profile("Begin next state")
        val connected = getConnected(tiles)
        val removeTilePos = (getTileRemovePos(rnd, removeTileProbabilities) ++ tiles.filter(pair => !connected.contains(pair._1)).map(pair => pair._1)).toSet
        return removeTiles(removeTilePos)
    }

    def removeTiles(removeTilePos: Set[Vector3]): KTAMSimulationState =
    {
        val fullRemoveAdjacents: List[(Vector3, Vector[(Tile, Int)])] = removeTilePos.toList.map(pos => (pos, getFullAdjacents(pos, tiles)))
        val removeAdjacentTiles:Map[Vector3, Vector3] = fullRemoveAdjacents.flatMap(pair => pair._2.map(innerPair => innerPair._1.getPosition -> pair._1)).toMap
        Profiler.profile("Got adjacent tiles")

        Profiler.profile("Cloned new tile")
        val newTuples: List[(Vector3, Tile)] = removeAdjacentTiles.filter(pair => !removeTilePos.contains(pair._2)).toList.map(pair => getTileAt(pair._1, tiles).clone(pair._2, false)).map(tile => (tile.getPosition, tile))
        Profiler.profile("Got tiles to be updated")
        val newTiles = tiles -- removeTilePos ++ newTuples
        val reduntantAdjacencies = removeTilePos.flatMap(pos => getEmptyAdjacents(tiles(pos), newTiles)).toSet.filter(adjPos => !getFullAdjacents(adjPos, newTiles).exists(tileOrientationPair => !removeTilePos.contains(tileOrientationPair._1.getPosition)))
        Profiler.profile("Cloned full tile set")
        val newAdjacentTiles = (removeTilePos).map(position => position -> calculateMonteCarloChances(position, newTiles)).filter(pair => pair._2.length > 0 && getFullAdjacents(pair._1, newTiles).size > 0)
        val newAdjacencies = adjacencies -- reduntantAdjacencies ++ newAdjacentTiles
        val newRemoveTiles: Map[Vector3, Double] = newTiles.map(pair => calculateRemoveChances(pair._1, newTiles)).toMap
        val newRemoveTileProbabilities: Map[Vector3, Double] = removeTileProbabilities._2 -- removeTilePos ++ newRemoveTiles
        Profiler.profile("Cloned and updated adjacencies"   )

        val newStats = stats.clone()
        updateStats((removeTilePos ++ newTuples.map(pair => pair._1)).map(pos => getTileAt(pos, tiles)).toList, newTuples.map(pair => pair._2), tiles, newTiles, newStats)

        return new KTAMSimulationState(newTiles, tileTypes, newAdjacencies, calculateMaxChances(newRemoveTileProbabilities), backwardConstant, forwardConstant, !removeTick, newStats)
    }

    def getTileRemovePos(rnd : Random, tileProbabilities: (Double, Map[Vector3, Double])): List[Vector3] =
    {
        if(tileProbabilities._2.size == 0) return List[Vector3]()
        val chosenTile = tileProbabilities._2.foldLeft(null.asInstanceOf[Vector3])((curTile, pair) =>
        {
            if(curTile != null) curTile
            else if(pair._2 >= rnd.nextDouble())
            {
                pair._1
            }
            else
            {
                curTile
            }
        })
        if(chosenTile == null)
        {
            var a = 1
            return List[Vector3]()
        }
        return tileProbabilities._2.filter(pair => rnd.nextDouble() < pair._2).map(pair => pair._1).toList //tileProbabilities._2.filter(pair => pair._2 > rnd.nextDouble()).map(pair => pair ._1).toList
    }

    override def getEmptyAdjacents(tile: Tile, tiles: Map[Vector3, Tile]): List[Vector3] =
    {
        return getAllEmptyAdjacents(tile, tiles)
    }

    def getConnected(tiles: Map[Vector3, Tile]): Set[Vector3] =
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
                if(!visited.contains(newPos) && tiles.contains(newPos))
                {
                    visited.add(newPos)
                    edge.push(newPos)
                }
            } )
        }

        return visited.toSet
    }


    override def setTile(tilePos: Vector3, tile: Tile): SimulationState =
    {
        if(tile == null && tiles.contains(tilePos))
        {
            return removeTiles(Set(tilePos))
        }
        else if(tile != null)
        {
            val fullAdjacents: Vector[(Tile, Int)] = getFullAdjacents(tilePos, tiles)
            val adjacentTiles:Set[Vector3] = fullAdjacents.map(pair => pair._1.getPosition).toSet

            val newTile = tile.clone(tilePos, fullAdjacents.map(pair => pair._2))
            val newTuples: List[(Vector3, Tile)] = (newTile :: adjacentTiles.toList.map(pos => getTileAt(pos, tiles).clone(newTile.getPosition, true))).map(tile => (tile.getPosition, tile));
            val newTiles = tiles ++ newTuples

            val redundantAdjacents: List[Vector3] = if(tiles.contains(tilePos))
            {
                val emptyAdj: List[Vector3] = (0 until 4).map(orientation => tilePos.add(Util.orientationToVector(orientation))).filter(pos => !tiles.contains(pos)).toList;
                emptyAdj.filter(adj => isRedundantAdjacency(getFullAdjacents(adj, newTiles)))
            } else List(tilePos)

            val newAdjacentTiles = getEmptyAdjacents(newTile, tiles).map(position => position -> calculateMonteCarloChances(position, newTiles)).filter(_._2.length > 0)
            val newAdjacencies = (adjacencies - tilePos) -- redundantAdjacents ++ newAdjacentTiles

            val newRemoveTiles: Map[Vector3, Double] = newTiles.map(pair => calculateRemoveChances(pair._1, newTiles)).toMap
            val newRemoveTileProbabilities: Map[Vector3, Double] = removeTileProbabilities._2 ++ newRemoveTiles

            val newStats = stats.clone()
            updateStats(adjacentTiles.map(pos => getTileAt(pos, tiles)).toList, newTuples.map(pair => pair._2), tiles, newTiles, newStats)

            return new KTAMSimulationState(newTiles, tileTypes, newAdjacencies, calculateMaxChances(newRemoveTileProbabilities), backwardConstant, forwardConstant, removeTick, newStats)
        }

        return this
    }

    override def isRedundantAdjacency(fullAdjacents: Vector[(Tile, Int)]): Boolean =
    {
        return fullAdjacents.isEmpty
    }

    override def getSimulationClassID(): String = "KTAMSimulation"

    override def toJSON(obj: JSONObject): JSONObject =
    {
        super.toJSON(obj)
        //startingRemoveTileProbabilities: (Double, Map[Vector3, Double]), backwardConstant: Double, forwardConstant: Double, removeTick
        val removeTileObj = new JSONObject()
        removeTileObj.put("total", removeTileProbabilities._1)
        removeTileProbabilities._2.map(pair => removeTileObj.append("map", createJSONRemovePair(pair)))

        obj.put("removeTileProbabilities", removeTileObj)
        obj.put("backwardConstant", backwardConstant)
        obj.put("forwardConstant", forwardConstant)
        obj.put("removeTick", removeTick)
    }

    def createJSONRemovePair(pair: (Vector3, Double)): JSONObject =
    {
        val obj = new JSONObject()
        obj.put("key", Util.IOUtil.vector3ToJSON(pair._1))
        obj.put("value", pair._2)
        return obj
    }
}
