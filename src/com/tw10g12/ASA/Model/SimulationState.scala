package com.tw10g12.ASA.Model

import com.tw10g12.ASA.Debug.Profiler
import com.tw10g12.ASA.Util
import com.tw10g12.Maths.Vector3

import scala.util.Random

/**
 * Created by Tom on 27/10/2014.
 */
class SimulationState(startingTiles: Map[Vector3, Tile], tileTypes: Vector[Tile], startingAdjacencies: Map[Vector3, List[(Int, Double)]])
{
    def this(startingTile: Tile, tileTypes: Vector[Tile]) = this(Map((startingTile.getPosition -> startingTile)), tileTypes, null)

    val tiles: Map[Vector3, Tile] = startingTiles
    val adjacencies: Map[Vector3, List[(Int, Double)]] = if(startingAdjacencies == null) calculateAdjacencies() else startingAdjacencies

    def calculateEdgeTiles(): Map[Vector3, Tile] =
    {
        Profiler.profile("Starting edge tile calculation")
        val retVal = tiles.par.filter(keyVal => isEdgeTile(keyVal._2)).seq
        Profiler.profile("Finished edge tile calculation")
        return retVal
    }

    def isEdgeTile(currentTile: Tile): Boolean =
    {
        for(orientation <- 0 until 4)
        {
            if(currentTile.glues(orientation) != null && currentTile.glues(orientation).strength > 0 && !currentTile.glues(orientation).isBound)
            {
                return true
            }
        }
        return false
    }

    def calculateMonteCarloChances(position: Vector3, tiles: Map[Vector3, Tile]): List[(Int, Double)] =
    {
        if(position == null) return List[(Int, Double)]()
        val adjacentTiles: Vector[(Tile, Int)] = getFullAdjacents(position, tiles)
        val validTileTypes = tileTypes.filter(tile => filterTile(tile, adjacentTiles))
        return validTileTypes.map(tile => (tile.typeID, validTileTypes.length.asInstanceOf[Double])).toList
    }

    def filterTile(tile: Tile, adjacentTiles: Vector[(Tile, Int)]): Boolean =
    {
        return tile.canBind(adjacentTiles)
    }

    def getTileAt(position: Vector3, tiles: Map[Vector3, Tile]): Tile =
    {
        if(!tiles.contains(position)) return null
        else return tiles(position)
    }

    def calculateAdjacencies(): Map[Vector3,List[(Int, Double)]] =
    {
        Profiler.profile("Starting adjacency calculation")
        val edgeTiles: Map[Vector3, Tile] = calculateEdgeTiles()
        val adjacentTiles: Set[Vector3] = edgeTiles.par.flatMap(tile => getEmptyAdjacents(tile._2, tiles)).seq.toSet[Vector3]
        Profiler.profile("Completed flat map, calculating monte carlo chances for " + adjacentTiles.size + " tiles")
        val retVal = adjacentTiles.toList.par.map(position => position -> calculateMonteCarloChances(position, tiles)).filter(_._2.length > 0).seq.toMap
        Profiler.profile("Finished adjacency calculation")
        return retVal
    }

    def getEmptyAdjacents(tile: Tile, tiles: Map[Vector3, Tile]): List[Vector3] =
    {
        val filteredGlues = tile.glues.filter(glue => !(glue == null || glue.isBound))
        return filteredGlues.map(glue => tile.getPosition.add(Util.orientationToVector(glue.orientation))).toList
    }

    def getFullAdjacents(position: Vector3, tiles: Map[Vector3, Tile]): Vector[(Tile, Int)] =
    {
        if(position == null) return Vector[(Tile, Int)]()
        return (0 until 4).map(x => (getTileAt(position.add(Util.orientationToVector(x)), tiles), x)).filter(_._1 != null).toVector
    }

    def nextState(rnd: Random): SimulationState =
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
            return new SimulationState(this.tiles, this.tileTypes, this.adjacencies)
        }
        val tile = tileTypes(tileType)
        val newTile: Tile = tile.clone(adjacency._1, fullAdjacents.map(pair => pair._2))
        Profiler.profile("Cloned new tile")
        val newTuples: List[(Vector3, Tile)] = (newTile :: adjacentTiles.toList.map(pos => getTileAt(pos, tiles).clone(newTile.getPosition, true))).map(tile => (tile.getPosition, tile));
        Profiler.profile("Got tiles to be updated")
        val newTiles = tiles ++ newTuples
        Profiler.profile("Cloned full tile set")
        val newAdjacentTiles = getEmptyAdjacents(newTile, tiles).map(position => position -> calculateMonteCarloChances(position, newTiles)).filter(_._2.length > 0)
        val newAdjacencies = (adjacencies - newTile.getPosition) ++ newAdjacentTiles
        Profiler.profile("Cloned and updated adjacencies")
        return new SimulationState(newTiles, tileTypes, newAdjacencies)
    }

    def updateTileRows(row: Int, curList: List[List[Tile]], newTile: Tile, adjacentTiles: Set[Vector3]): List[List[Tile]] =
    {
        if(curList == Nil)
        {
            var rowNo:Int = row
            val retList:List[List[Tile]] = (row until newTile.getPosition.getY.asInstanceOf[Int]).map(_ => List[Tile]()).toList

            if(row <= newTile.getPosition.getY)
            {
                val blankRow:List[Tile] = (0 until newTile.getPosition.getX.asInstanceOf[Int]).map(x => null.asInstanceOf[Tile]).toList
                return retList ::: List[List[Tile]]((blankRow ::: List[Tile](newTile)))
            }
            else
            {
                return Nil
            }
        }
        else
        {
            return updateTileRow(row, 0, curList.head, newTile, adjacentTiles) :: updateTileRows(row + 1, curList.tail, newTile, adjacentTiles)
        }
    }

    def updateTileRow(row:Int, column: Int, curRow: List[Tile], newTile: Tile, adjacentTiles: Set[Vector3]): List[Tile] =
    {
        if(curRow == Nil)
        {
            if(row != newTile.getPosition.getY) Nil
            if(column <= newTile.getPosition.getX)
            {
                val blankRow: List[Tile] = (column until newTile.getPosition.getX.asInstanceOf[Int]).map(x => null.asInstanceOf[Tile]).toList
                return (blankRow ::: List[Tile](newTile))
            }
            return Nil
        }
        else
        {
            val pos = new Vector3(column, row, 0)
            val retTile:Tile = getRetTile(pos, curRow, newTile, adjacentTiles)
            return retTile :: updateTileRow(row, column + 1, curRow.tail, newTile, adjacentTiles)
        }
    }

    def getRetTile(pos: Vector3, curRow: List[Tile], newTile: Tile, adjacentTiles: Set[Vector3] ) : Tile =
    {
        if(pos.equals(newTile.getPosition)) return newTile //Is current pos the actual tile?
        else if(adjacentTiles.contains(pos)) return curRow.head.clone(newTile.getPosition, true) //Is the current pos an adjacent tile
        else return curRow.head //Otherwise no state change
    }

    def getTileType(rndNo : Double, tileTypes: List[(Int, Double)]): Int =
    {
        if(tileTypes.length == 1) return tileTypes.head._1
        val oneOver = 1.0 / tileTypes.head._2
        if(oneOver > rndNo) return tileTypes.head._1
        return getTileType(rndNo - oneOver, tileTypes.tail)
    }
}
