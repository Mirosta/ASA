package com.tw10g12.ASA.GUI.Draw

import com.tw10g12.ASA.Model.{SimulationState, Tile}
import com.tw10g12.Draw.Engine.{DrawTools, OrbitCamera}
import com.tw10g12.Maths.Vector3

/**
 * Created by Tom on 27/10/2014.
 */
object RenderSimulation
{
    def render(simulationState: SimulationState, camera: OrbitCamera, drawTools: DrawTools): Unit =
    {
        val tileTypes: Map[Int,Map[Int, List[Tile]]] = foldTiles(simulationState, camera)
        drawTools.end()
        tileTypes.map(keyValue => renderTileLevels(keyValue._1, keyValue._2, drawTools))
        drawTools.start()
    }

    def renderTileLevels(tileType: Int, tileLevels: Map[Int, List[Tile]], drawTools: DrawTools): Unit =
    {
        if(tileLevels.size == 0) return
        tileLevels.map(keyValue => renderTileType(tileType, RenderATAMTile.getLODFromIndex(keyValue._1), keyValue._2, drawTools))
    }

    def renderTileType(tileType: Int, lod: Int, tiles: List[Tile], drawTools: DrawTools): Unit =
    {
        if(tiles.length == 0) return
        drawTools.start(true)
        RenderATAMTile.renderTile(tiles(0).clone(new Vector3(0,0,0), Vector()), lod, drawTools)
        tiles.map(tile => RenderATAMTile.instanceRender(tile, drawTools))
        tiles.map(tile => RenderATAMTile.afterRender(tile, tile.getPosition, lod, drawTools))
        drawTools.end()
    }

    def foldTiles(simulationState: SimulationState, camera: OrbitCamera): Map[Int, Map[Int, List[Tile]]] =
    {
        simulationState.tiles.foldLeft(Map[Int,Map[Int,List[Tile]]]())((currentMap, tile) => foldHelper(currentMap, tile._2, camera))
    }

    def foldHelper(currentMap: Map[Int, Map[Int, List[Tile]]], tile: Tile, camera: OrbitCamera): Map[Int, Map[Int, List[Tile]]] =
    {

        if (!currentMap.contains(tile.typeID)) return currentMap + (tile.typeID -> processSubMap(Map[Int, List[Tile]](), tile, camera))
        else return currentMap + (tile.typeID -> processSubMap(currentMap(tile.typeID), tile, camera))
    }

    def processSubMap(currentMap: Map[Int, List[Tile]], tile: Tile, camera: OrbitCamera): Map[Int, List[Tile]] =
    {
        val lodIndex: Int = RenderATAMTile.getLODIndex(getLOD(getTileDistance(camera, tile)))
        if (!currentMap.contains(lodIndex)) return currentMap + (lodIndex -> List[Tile](tile))
        else return currentMap + (lodIndex -> (tile :: currentMap(lodIndex)))
    }

    def getTileDistance(camera: OrbitCamera, tile: Tile): Double =
    {
        return camera.getActualCameraPos.subtract(RenderATAMTile.getRenderPosition(tile.getPosition)).lengthSquared()
    }

    def getLOD(distance: Double): Int =
    {
        val absDistance: Double = distance
        if(absDistance < 50*50) return 6
        if(absDistance < 100*100) return 5
        if(absDistance < 200*200) return 4
        if(absDistance < 300*300) return 3
        if(absDistance < 450*450) return 2
        if(absDistance < 600*600) return 1

        return 0
    }
}
