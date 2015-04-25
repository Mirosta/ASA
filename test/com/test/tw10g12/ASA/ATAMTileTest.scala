package com.test.tw10g12.ASA

import com.test.tw10g12.Test.UnitSpec
import com.tw10g12.ASA.Model.ATAM.{ATAMGlue, ATAMTile}
import com.tw10g12.ASA.Model.SimulationState
import com.tw10g12.Draw.Engine.Colour
import com.tw10g12.Maths.Vector3

/**
 * Created by Tom on 24/04/2015.
 */
class ATAMTileTest extends UnitSpec
{
    "A Simulation" should "allow a tile to be added" in
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
}
