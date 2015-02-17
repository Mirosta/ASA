package com.tw10g12.ASA.Model

import com.tw10g12.Draw.Engine.Colour
import com.tw10g12.Maths.Vector3
/**
 * Created by Tom on 27/10/2014.
 */
abstract class Tile(setupGlues: Vector[Glue], colour: Vector[Colour], position: Vector3, val typeID:Int)
{
    val glues = procGlues(setupGlues)

    private def procGlues(setupGlues: Vector[Glue]) : Vector[Glue] =
    {
        var orientation = -1
        def proc(glue: Glue) = if(glue == null) { orientation+= 1; null } else { orientation+= 1; glue.clone(this, orientation) }
        return setupGlues.map(glue => proc(glue))
    }

    def getNorthGlue: Glue = glues(0)
    def getEastGlue: Glue = glues(1)
    def getSouthGlue: Glue = glues(2)
    def getWestGlue: Glue = glues(3)
    def getColour: Colour = colour(0)
    def getPosition: Vector3 = position

    def canBind(otherTiles: Vector[(Tile, Int)]): Boolean
    def clone(newPosition: Vector3, adjacentOrientations: Vector[Int]): Tile
    def clone(newTilePosition: Vector3): Tile
}
