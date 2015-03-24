package com.tw10g12.ASA.Model

import com.tw10g12.ASA.Debug.Profiler
import com.tw10g12.ASA.Util
import com.tw10g12.Maths.Vector3

import scala.collection.mutable
import scala.util.Random

/**
 * Created by Tom on 03/03/2015.
 */
class KTAMSimulationState(startingTiles: Map[Vector3, Tile], tileTypes: Vector[Tile], startingAdjacencies: Map[Vector3, List[(Int, Double)]], startingRemoveTileProbabilities: (Double, Map[Vector3, Double]), backwardConstant: Double, forwardConstant: Double, removeTick: Boolean) extends SimulationState(startingTiles, tileTypes, startingAdjacencies)
{
    val removeTileProbabilities: (Double, Map[Vector3, Double]) = if(startingRemoveTileProbabilities == null) calculateMaxChances(tiles.foldLeft(Map[Vector3, Double]())((curMap, tilePair) => curMap + calculateRemoveChances(tilePair._1, tiles))) else startingRemoveTileProbabilities

    def this(startingTile: Tile, tileTypes: Vector[Tile], backwardConstant: Double, forwardConstant: Double) = this(Map((startingTile.getPosition -> startingTile)), tileTypes, null, null, backwardConstant, forwardConstant, false)

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
        return Math.exp(-tile.getStrength(adjacentTiles) * backwardConstant)
    }

    override def filterTile(tile: Tile, adjacentTiles: Vector[(Tile, Int)]): Boolean =
    {
        return true
    }

    override def nextState(rnd: Random): SimulationState =
    {
        val weightedForwardChance = forwardConstant / (forwardConstant + backwardConstant)

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

        return new KTAMSimulationState(newTiles, tileTypes, newAdjacencies, calculateMaxChances(newRemoveTileProbabilities), backwardConstant, forwardConstant, !removeTick)
    }

    def nextRemoveState(rnd: Random): KTAMSimulationState =
    {
        Profiler.profile("Begin next state")

        val removeTilePos = getTileRemovePos(rnd, removeTileProbabilities).toSet
        val fullRemoveAdjacents: List[(Vector3, Vector[(Tile, Int)])] = removeTilePos.toList.map(pos => (pos, getFullAdjacents(pos, tiles)))
        val removeAdjacentTiles:Map[Vector3, Vector3] = fullRemoveAdjacents.flatMap(pair => pair._2.map(innerPair => innerPair._1.getPosition -> pair._1)).toMap
        val reduntantAdjacencies = removeTilePos.flatMap(pos => getEmptyAdjacents(tiles(pos), tiles)).toSet.filter(adjPos => !getFullAdjacents(adjPos, tiles).exists(tileOrientationPair => !removeTilePos.contains(tileOrientationPair._1.getPosition)))
        Profiler.profile("Got adjacent tiles")

        Profiler.profile("Cloned new tile")
        val newTuples: List[(Vector3, Tile)] = removeAdjacentTiles.toList.map(pair => getTileAt(pair._1, tiles).clone(pair._2, false)).map(tile => (tile.getPosition, tile))
        Profiler.profile("Got tiles to be updated")
        val newTiles = tiles -- removeTilePos ++ newTuples
        Profiler.profile("Cloned full tile set")
        val newAdjacentTiles = (removeTilePos).map(position => position -> calculateMonteCarloChances(position, newTiles)).filter(_._2.length > 0)
        val newAdjacencies = adjacencies -- reduntantAdjacencies ++ newAdjacentTiles
        val newRemoveTiles: Map[Vector3, Double] = newTiles.map(pair => calculateRemoveChances(pair._1, newTiles)).toMap
        val newRemoveTileProbabilities: Map[Vector3, Double] = removeTileProbabilities._2 -- removeTilePos ++ newRemoveTiles
        Profiler.profile("Cloned and updated adjacencies"   )

        return new KTAMSimulationState(newTiles, tileTypes, newAdjacencies, calculateMaxChances(newRemoveTileProbabilities), backwardConstant, forwardConstant, !removeTick)
    }

    def getTileRemovePos(rnd : Random, tileProbabilities: (Double, Map[Vector3, Double])): List[Vector3] =
    {
        if(tileProbabilities._2.size == 0) return List[Vector3]()
        val totalProb = tileProbabilities._2.foldLeft(0.0)((sum, pair) => sum + pair._2)
        if(totalProb <= 0) return List[Vector3]()
        var randNum = rnd.nextDouble() * totalProb
        val chosenTile = tileProbabilities._2.foldLeft(null.asInstanceOf[Vector3])((curTile, pair) =>
        {
            if(curTile != null) curTile
            else if(pair._2 > randNum)
            {
                pair._1
            }
            else
            {
                randNum -= pair._2
                curTile
            }
        })
        return List[Vector3](chosenTile)//tileProbabilities._2.filter(pair => pair._2 > rnd.nextDouble()).map(pair => pair ._1).toList
    }

    override def getEmptyAdjacents(tile: Tile, tiles: Map[Vector3, Tile]): List[Vector3] =
    {
        return (0 until 4).map(orientation => tile.getPosition.add(Util.orientationToVector(orientation))).filter(pos => !tiles.contains(pos)).toList
    }

    def getConnected(tilePos: Vector3, tiles: Map[Vector3, Tile]): Set[Vector3] =
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
                if(!visited.contains(newPos))
                {
                    visited.add(newPos)
                    edge.push(newPos)
                }
            } )
        }

        return visited.toSet
    }
}
