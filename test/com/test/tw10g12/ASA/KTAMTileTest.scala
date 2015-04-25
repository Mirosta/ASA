package com.test.tw10g12.ASA

import com.test.tw10g12.Test.UnitSpec
import com.tw10g12.ASA.Model.ATAM.{ATAMGlue, ATAMTile}
import com.tw10g12.ASA.Model.KTAMSimulationState
import com.tw10g12.Draw.Engine.Colour
import com.tw10g12.Maths.Vector3

/**
 * Created by Tom on 25/04/2015.
 */
class KTAMTileTest extends UnitSpec
{
    "A KTAM Simulation" should "allow a tile to be added" in
        {
            val seedTile = new ATAMTile(Vector(new ATAMGlue("N", 2), new ATAMGlue("E", 2), new ATAMGlue("S", 2), new ATAMGlue("W", 2)), Vector(Colour.Black), new Vector3(0,0,0), -1)
            val tileTypes = Vector(new ATAMTile(Vector(new ATAMGlue("N", 2), null, new ATAMGlue("N", 2)), Vector(Colour.Red), new Vector3(0,0,0), 0), new ATAMTile(Vector(new ATAMGlue("S", 2)), Vector(Colour.Black), new Vector3(), 1))
            val simulation = new KTAMSimulationState(seedTile, tileTypes, 1, 1)

            val setTileAbove = simulation.setTile(new Vector3(0, 1, 0), tileTypes(0)).asInstanceOf[KTAMSimulationState]
            val setTileBelow = simulation.setTile(new Vector3(0, -1, 0), tileTypes(1)).asInstanceOf[KTAMSimulationState]
            val setBoth = simulation.setTile(new Vector3(0, 1, 0), tileTypes(0)).setTile(new Vector3(0, -1, 0), tileTypes(1)).asInstanceOf[KTAMSimulationState]

            setTileAbove.tiles.contains(new Vector3(0, 1, 0)) should be (true)
            setTileAbove.tiles(new Vector3(0, 1, 0)).typeID should be (0)

            setTileAbove.adjacencies should matchAdjacencies(2, Map(new Vector3(0,2,0)->Set(0), new Vector3(0,-1,0) -> Set(1)))
            setTileAbove.removeTileProbabilities._2 should contain (new Vector3(0, 1, 0))

            setTileBelow.tiles.contains(new Vector3(0, -1, 0)) should be (true)
            setTileBelow.tiles(new Vector3(0, -1, 0)).typeID should be (1)

            setTileBelow.adjacencies should matchAdjacencies(1, Map(new Vector3(0,1,0)->Set(0)))
            setTileBelow.removeTileProbabilities._2 should contain (new Vector3(0, -1, 0))

            setBoth.tiles.contains(new Vector3(0, 1, 0)) should be (true)
            setBoth.tiles(new Vector3(0, 1, 0)).typeID should be (0)
            setBoth.tiles.contains(new Vector3(0, -1, 0)) should be (true)
            setBoth.tiles(new Vector3(0, -1, 0)).typeID should be (1)

            setBoth.adjacencies should matchAdjacencies(1, Map(new Vector3(0,2,0)->Set(0)))
            setBoth.removeTileProbabilities._2 should contain allOf (new Vector3(0, -1, 0), new Vector3(0, 1, 0))
        }

    "An KTAM Simulation" should "allow a tile to be removed" in
        {
            val seedTile = new ATAMTile(Vector(new ATAMGlue("N", 2), new ATAMGlue("E", 2), new ATAMGlue("S", 2), new ATAMGlue("W", 2)), Vector(Colour.Black), new Vector3(0,0,0), -1)
            val tileTypes = Vector(new ATAMTile(Vector(new ATAMGlue("N", 2), null, new ATAMGlue("N", 2)), Vector(Colour.Red), new Vector3(0,0,0), 0), new ATAMTile(Vector(new ATAMGlue("S", 2)), Vector(Colour.Black), new Vector3(), 1))

            val simulation = new KTAMSimulationState(seedTile, tileTypes, 1, 1).setTile(new Vector3(0, 1, 0), tileTypes(0)).setTile(new Vector3(0, -1, 0), tileTypes(1))

            val removeTileAbove = simulation.setTile(new Vector3(0, 1, 0), null).asInstanceOf[KTAMSimulationState]
            val removeTileBelow = simulation.setTile(new Vector3(0, -1, 0), null).asInstanceOf[KTAMSimulationState]
            val removeBoth = simulation.setTile(new Vector3(0, 1, 0), null).setTile(new Vector3(0, -1, 0), null).asInstanceOf[KTAMSimulationState]

            removeTileAbove.tiles.contains(new Vector3(0, 1, 0)) should be (false)
            removeTileAbove.adjacencies should matchAdjacencies(1, Map(new Vector3(0,1,0)->Set(0)))
            removeTileAbove.removeTileProbabilities._2 should contain only (new Vector3(0, -1, 0))

            removeTileBelow.tiles.contains(new Vector3(0, -1, 0)) should be (false)
            removeTileBelow.adjacencies should matchAdjacencies(1, Map(new Vector3(0,-1,0)->Set(1)))
            removeTileBelow.removeTileProbabilities._2 should contain only (new Vector3(0, 1, 0))

            removeBoth.tiles.contains(new Vector3(0, 1, 0)) should be (false)
            removeBoth.tiles.contains(new Vector3(0, -1, 0)) should be (false)
            removeBoth.adjacencies should matchAdjacencies(2, Map(new Vector3(0,1,0)->Set(0), new Vector3(0, -1, 0) -> Set(1)))
            removeBoth.removeTileProbabilities._2.isEmpty should be (true)
        }

    "An KTAM Simulation" should "allow a tile to be updated" in
        {
            val seedTile = new ATAMTile(Vector(new ATAMGlue("N", 2), new ATAMGlue("E", 2), new ATAMGlue("S", 2), new ATAMGlue("W", 2)), Vector(Colour.Black), new Vector3(0,0,0), -1)
            val tileTypes = Vector(new ATAMTile(Vector(new ATAMGlue("N", 2), null, new ATAMGlue("N", 2)), Vector(Colour.Red), new Vector3(0,0,0), 0), new ATAMTile(Vector(new ATAMGlue("S", 2)), Vector(Colour.Black), new Vector3(), 1), new ATAMTile(Vector(new ATAMGlue("S", 2), null, new ATAMGlue("S", 2)), Vector(Colour.Red), new Vector3(0,0,0), 2), new ATAMTile(Vector(null, null, new ATAMGlue("N", 2)), Vector(Colour.Black), new Vector3(), 3))

            val simulation: KTAMSimulationState = new KTAMSimulationState(seedTile, tileTypes, 1, 1).setTile(new Vector3(0, 1, 0), tileTypes(0)).setTile(new Vector3(0, -1, 0), tileTypes(1)).asInstanceOf[KTAMSimulationState]

            val terminateTileAbove = simulation.setTile(new Vector3(0, 1, 0), tileTypes(3)).asInstanceOf[KTAMSimulationState]
            val chainTileBelow = simulation.setTile(new Vector3(0, -1, 0), tileTypes(2)).asInstanceOf[KTAMSimulationState]
            val updateBoth = simulation.setTile(new Vector3(0, 1, 0), tileTypes(3)).setTile(new Vector3(0, -1, 0), tileTypes(2)).asInstanceOf[KTAMSimulationState]

            terminateTileAbove.tiles.contains(new Vector3(0, 1, 0)) should be (true)
            terminateTileAbove.tiles(new Vector3(0, 1, 0)).typeID should be (3)
            terminateTileAbove.adjacencies should matchAdjacencies(0, Map())
            terminateTileAbove.removeTileProbabilities should contain allOf(new Vector3(0, 1, 0), new Vector3(0, -1, 0))

            chainTileBelow.tiles.contains(new Vector3(0, -1, 0)) should be (true)
            chainTileBelow.tiles(new Vector3(0, -1, 0)).typeID should be (2)
            chainTileBelow.adjacencies should matchAdjacencies(2, Map(new Vector3(0,-2,0)->Set(1, 2)))
            chainTileBelow.removeTileProbabilities should contain allOf(new Vector3(0, 1, 0), new Vector3(0, -1, 0))

            updateBoth.tiles.contains(new Vector3(0, 1, 0)) should be (true)
            updateBoth.tiles(new Vector3(0, 1, 0)).typeID should be (3)
            updateBoth.tiles.contains(new Vector3(0, -1, 0)) should be (true)
            updateBoth.tiles(new Vector3(0, -1, 0)).typeID should be (2)
            updateBoth.adjacencies should matchAdjacencies(1, Map(new Vector3(0,-2,0)->Set(1, 2)))
            updateBoth.removeTileProbabilities should contain allOf(new Vector3(0, 1, 0), new Vector3(0, -1, 0))
        }
}
