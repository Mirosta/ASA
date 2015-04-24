package com.tw10g12.ASA

import com.tw10g12.ASA.Model.ATAM.{ATAMGlue, ATAMTile}
import com.tw10g12.ASA.Model.SimulationState
import com.tw10g12.Draw.Engine.Colour
import com.tw10g12.Maths.Vector3
import com.tw10g12.Test.UnitSpec

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

        setTileAbove.adjacencies.size should be (2)
        setTileAbove.adjacencies.contains(new Vector3(0, 2, 0)) should be (true)
        setTileAbove.adjacencies(new Vector3(0, 2, 0)).size should be (1)
        setTileAbove.adjacencies(new Vector3(0, 2, 0)).exists(pair => pair._1 == 0) should be (true)

        setTileAbove.adjacencies.contains(new Vector3(0, -1, 0)) should be (true)
        setTileAbove.adjacencies(new Vector3(0, -1, 0)).size should be (1)
        setTileAbove.adjacencies(new Vector3(0, -1, 0)).exists(pair => pair._1 == 1) should be (true)

        setTileBelow.tiles.contains(new Vector3(0, -1, 0)) should be (true)
        setTileBelow.tiles(new Vector3(0, -1, 0)).typeID should be (1)

        setTileBelow.adjacencies.size should be (2)
        setTileBelow.adjacencies.contains(new Vector3(0, 2, 0)) should be (true)
        setTileBelow.adjacencies(new Vector3(0, 2, 0)).size should be (1)
        setTileBelow.adjacencies(new Vector3(0, 2, 0)).exists(pair => pair._1 == 0) should be (true)

        setTileBelow.adjacencies.contains(new Vector3(0, -1, 0)) should be (true)
        setTileBelow.adjacencies(new Vector3(0, -1, 0)).size should be (1)
        setTileBelow.adjacencies(new Vector3(0, -1, 0)).exists(pair => pair._1 == 1) should be (true)

        setBoth.tiles.contains(new Vector3(0, 1, 0)) should be (true)
        setBoth.tiles(new Vector3(0, 1, 0)).typeID should be (0)
        setBoth.tiles.contains(new Vector3(0, -1, 0)) should be (true)
        setBoth.tiles(new Vector3(0, -1, 0)).typeID should be (1)

        setTileAbove.tiles.contains(new Vector3(0, 1, 0)) should be (true)
        setTileAbove.tiles(new Vector3(0, 1, 0)).typeID should be (0)

        setTileAbove.adjacencies.size should be (1)
        setTileAbove.adjacencies.contains(new Vector3(0, 2, 0)) should be (true)
        setTileAbove.adjacencies(new Vector3(0, 2, 0)).size should be (1)
        setTileAbove.adjacencies(new Vector3(0, 2, 0)).exists(pair => pair._1 == 0) should be (true)
    }
}
