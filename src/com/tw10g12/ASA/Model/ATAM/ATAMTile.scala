package com.tw10g12.ASA.Model.ATAM

import com.tw10g12.ASA.Model.{Glue, Tile}
import com.tw10g12.ASA.Util
import com.tw10g12.Draw.Engine.Colour
import com.tw10g12.Maths.Vector3
import org.json.{JSONArray, JSONObject}

/**
 * Created by Tom on 27/10/2014.
 */
class ATAMTile(setupGlues: Vector[Glue], colours: Vector[Colour], position: Vector3, typeID: Int) extends Tile(setupGlues, colours, position, typeID)
{

    //Serialization
    override def toJSON(editingObj: JSONObject): JSONObject =
    {
        val newEditingObj = super.toJSON(editingObj)
        newEditingObj.put("glues", new JSONArray(glues.map(glue => if(glue == null) null else glue.toJSON(new JSONObject())).toArray))
        newEditingObj.put("tileClassID", "ATAMTile")
        return newEditingObj
    }

    override def getStrength(otherTiles: Vector[(Tile, Int)]): Int =
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

                        if (ownGlue != null && otherGlue != null && !ownGlue.incorrectBind(otherGlue)) validStrength += ownGlue.strength
                }
            }
        }
        return validStrength
    }

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
        val newGlues = glues.map(glue => glueMapFun(glue, adjacentOrientations, true))
        return new ATAMTile(newGlues, colours, newPosition, typeID)
    }

    override def clone(newTilePosition: Vector3, exists: Boolean): Tile =
    {
        val adjacentOrientations = Vector[Int](Util.vectorToOrientation(position.add(newTilePosition.multiply(-1))))
        val newGlues = glues.map(glue => glueMapFun(glue, adjacentOrientations, exists))
        return new ATAMTile(newGlues, colours, position, typeID)
    }

    def glueMapFun(glue: Glue, adjacentOrientations: Vector[Int], exists: Boolean): Glue =
    {
        if(glue == null) return null
        val matchingOrientations = adjacentOrientations.filter(orientation => orientation == glue.orientation);
        if(matchingOrientations.length == 1) return glue.clone(exists)
        return glue
    }

    def setColour(newColour: Colour): ATAMTile =
    {
        return new ATAMTile(glues, Vector[Colour](newColour), position, typeID)
    }

    def setGlue(glue: Glue, glueIndex: Int): ATAMTile =
    {
        var index = -1
        val newGlues = glues.map(g => { index += 1; if(index == glueIndex) glue else g } )
        return new ATAMTile(newGlues, colours, position, typeID)
    }

    def setTypeID(newTypeID: Int): ATAMTile =
    {
        return new ATAMTile(glues, colours, position, newTypeID)
    }
}
