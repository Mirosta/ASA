package com.tw10g12.ASA.Model.ATAM

import com.tw10g12.ASA.Model.{Glue, Tile}
import com.tw10g12.ASA.Util
import com.tw10g12.Draw.Engine.Colour
import com.tw10g12.Maths.Vector3

/**
 * Created by Tom on 27/10/2014.
 */
class ATAMTile(setupGlues: Vector[Glue], colours: Vector[Colour], position: Vector3, typeID: Int) extends Tile(setupGlues, colours, position, typeID)
{

    override def canBind(otherTiles: Vector[(Tile, Int)]): Boolean =
    {
        var validStrength = 0
        for(tileOrientationPair <- otherTiles)
        {
            val otherTile = tileOrientationPair._1
            if(otherTile != null)
            {
                val orientation = tileOrientationPair._2
                otherTile match
                {
                    case otherTile: ATAMTile =>
                        val reverseOrientation = Util.oppositeOrientation(orientation)
                        val otherGlue = otherTile.glues(reverseOrientation)
                        val ownGlue = this.glues(orientation)

                        if (ownGlue == null || otherGlue == null) return false
                        if (!ownGlue.canBind(otherGlue)) return false
                        validStrength += ownGlue.strength
                    case _ => return false
                }
            }
        }
        return validStrength >= 2
    }

    override def clone(newPosition: Vector3, adjacentOrientations: Vector[Int]): Tile =
    {
        val newGlues = glues.map(glue => glueMapFun(glue, adjacentOrientations))
        return new ATAMTile(newGlues, colours, newPosition, typeID)
    }

    override def clone(newTilePosition: Vector3): Tile =
    {
        val adjacentOrientations = Vector[Int](Util.vectorToOrientation(position.add(newTilePosition.multiply(-1))))
        val newGlues = glues.map(glue => glueMapFun(glue, adjacentOrientations))
        return new ATAMTile(newGlues, colours, position, typeID)
    }

    def glueMapFun(glue: Glue, adjacentOrientations: Vector[Int]): Glue =
    {
        if(glue == null) return null
        val matchingOrientations = adjacentOrientations.filter(orientation => orientation == glue.orientation);
        if(matchingOrientations.length == 1) return glue.clone(true)
        return glue
    }

    def setColour(newColour: Colour): ATAMTile =
    {
        return new ATAMTile(glues, Vector[Colour](newColour), position, typeID)
    }
}
