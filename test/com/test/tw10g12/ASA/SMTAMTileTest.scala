package com.test.tw10g12.ASA

import com.test.tw10g12.Test.UnitSpec
import com.tw10g12.ASA.Model.ATAM.{ATAMGlue, ATAMTile}
import com.tw10g12.ASA.Model.SimulationState
import com.tw10g12.Draw.Engine.Colour
import com.tw10g12.Maths.Vector3

/**
 * Created by Tom on 25/04/2015.
 */
class SMTAMTileTest extends UnitSpec
{
    "A SMTAM Simulation" should "allow a tile to be added" in
        {
            val seedTile = new ATAMTile(Vector(new ATAMGlue("N", 2), new ATAMGlue("E", 2), new ATAMGlue("S", 2), new ATAMGlue("W", 2)), Vector(Colour.Black), new Vector3(0,0,0), -1)
            val tileTypes = Vector(new ATAMTile(Vector(new ATAMGlue("N", 2), null, new ATAMGlue("N", 2)), Vector(Colour.Red), new Vector3(0,0,0), 0), new ATAMTile(Vector(new ATAMGlue("S", 2)), Vector(Colour.Black), new Vector3(), 1))
            val simulation = new SimulationState(seedTile, tileTypes)

            val setTileAbove = simulation.setTile(new Vector3(0, 1, 0), tileTypes(0))
            val setTileBelow = simulation.setTile(new Vector3(0, -1, 0), tileTypes(1))
            val setBoth = simulation.setTile(new Vector3(0, 1, 0), tileTypes(0)).setTile(new Vector3(0, -1, 0), tileTypes(1))

            setTileAbove.tiles.contains(new Vector3(0, 1, 0)) should be (true)
            setTileAbove.tiles(new Vector3(0, 1, 0)).typeID should be (0)

            setTileAbove.adjacencies should matchAdjacencies(2, Map(new Vector3(0,2,0)->Set(0), new Vector3(0,-1,0) -> Set(1)))

            setTileBelow.tiles.contains(new Vector3(0, -1, 0)) should be (true)
            setTileBelow.tiles(new Vector3(0, -1, 0)).typeID should be (1)

            setTileBelow.adjacencies should matchAdjacencies(1, Map(new Vector3(0,1,0)->Set(0)))

            setBoth.tiles.contains(new Vector3(0, 1, 0)) should be (true)
            setBoth.tiles(new Vector3(0, 1, 0)).typeID should be (0)
            setBoth.tiles.contains(new Vector3(0, -1, 0)) should be (true)
            setBoth.tiles(new Vector3(0, -1, 0)).typeID should be (1)

            setBoth.adjacencies should matchAdjacencies(1, Map(new Vector3(0,2,0)->Set(0)))
        }

    "A SMTAM Simulation" should "allow a tile to be removed" in
        {
            val seedTile = new ATAMTile(Vector(new ATAMGlue("N", 2), new ATAMGlue("E", 2), new ATAMGlue("S", 2), new ATAMGlue("W", 2)), Vector(Colour.Black), new Vector3(0,0,0), -1)
            val tileTypes = Vector(new ATAMTile(Vector(new ATAMGlue("N", 2), null, new ATAMGlue("N", 2)), Vector(Colour.Red), new Vector3(0,0,0), 0), new ATAMTile(Vector(new ATAMGlue("S", 2)), Vector(Colour.Black), new Vector3(), 1))

            val simulation = new SimulationState(seedTile, tileTypes).setTile(new Vector3(0, 1, 0), tileTypes(0)).setTile(new Vector3(0, -1, 0), tileTypes(1))

            val removeTileAbove = simulation.setTile(new Vector3(0, 1, 0), null)
            val removeTileBelow = simulation.setTile(new Vector3(0, -1, 0), null)
            val removeBoth = simulation.setTile(new Vector3(0, 1, 0), null).setTile(new Vector3(0, -1, 0), null)

            removeTileAbove.tiles.contains(new Vector3(0, 1, 0)) should be (false)
            removeTileAbove.adjacencies should matchAdjacencies(1, Map(new Vector3(0,1,0)->Set(0)))

            removeTileBelow.tiles.contains(new Vector3(0, -1, 0)) should be (false)
            removeTileBelow.adjacencies should matchAdjacencies(1, Map(new Vector3(0,-1,0)->Set(1)))

            removeBoth.tiles.contains(new Vector3(0, 1, 0)) should be (false)
            removeBoth.tiles.contains(new Vector3(0, -1, 0)) should be (false)

            removeBoth.adjacencies should matchAdjacencies(2, Map(new Vector3(0,1,0)->Set(0), new Vector3(0, -1, 0) -> Set(1)))
        }

    "A SMTAM Simulation" should "allow a tile to be updated" in
        {
            val seedTile = new ATAMTile(Vector(new ATAMGlue("N", 2), new ATAMGlue("E", 2), new ATAMGlue("S", 2), new ATAMGlue("W", 2)), Vector(Colour.Black), new Vector3(0,0,0), -1)
            val tileTypes = Vector(new ATAMTile(Vector(new ATAMGlue("N", 2), null, new ATAMGlue("N", 2)), Vector(Colour.Red), new Vector3(0,0,0), 0), new ATAMTile(Vector(new ATAMGlue("S", 2)), Vector(Colour.Black), new Vector3(), 1), new ATAMTile(Vector(new ATAMGlue("S", 2), null, new ATAMGlue("S", 2)), Vector(Colour.Red), new Vector3(0,0,0), 2), new ATAMTile(Vector(null, null, new ATAMGlue("N", 2)), Vector(Colour.Black), new Vector3(), 3))

            val simulation = new SimulationState(seedTile, tileTypes).setTile(new Vector3(0, 1, 0), tileTypes(0)).setTile(new Vector3(0, -1, 0), tileTypes(1))

            val terminateTileAbove = simulation.setTile(new Vector3(0, 1, 0), tileTypes(3))
            val chainTileBelow = simulation.setTile(new Vector3(0, -1, 0), tileTypes(2))
            val updateBoth = simulation.setTile(new Vector3(0, 1, 0), tileTypes(3)).setTile(new Vector3(0, -1, 0), tileTypes(2))

            terminateTileAbove.tiles.contains(new Vector3(0, 1, 0)) should be (true)
            terminateTileAbove.tiles(new Vector3(0, 1, 0)).typeID should be (3)
            terminateTileAbove.adjacencies should matchAdjacencies(0, Map())

            chainTileBelow.tiles.contains(new Vector3(0, -1, 0)) should be (true)
            chainTileBelow.tiles(new Vector3(0, -1, 0)).typeID should be (2)
            chainTileBelow.adjacencies should matchAdjacencies(2, Map(new Vector3(0,-2,0)->Set(1, 2)))

            updateBoth.tiles.contains(new Vector3(0, 1, 0)) should be (true)
            updateBoth.tiles(new Vector3(0, 1, 0)).typeID should be (3)
            updateBoth.tiles.contains(new Vector3(0, -1, 0)) should be (true)
            updateBoth.tiles(new Vector3(0, -1, 0)).typeID should be (2)
            updateBoth.adjacencies should matchAdjacencies(1, Map(new Vector3(0,-2,0)->Set(1, 2)))
        }
}
