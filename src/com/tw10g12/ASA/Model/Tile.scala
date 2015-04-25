package com.tw10g12.ASA.Model

import com.tw10g12.ASA.Util
import com.tw10g12.Draw.Engine.Colour
import com.tw10g12.IO.JSONSerializable
import com.tw10g12.Maths.Vector3
import org.json.{JSONArray, JSONObject}
/**
 * Created by Tom on 27/10/2014.
 */
abstract class Tile(setupGlues: Vector[Glue], colour: Vector[Colour], position: Vector3, val typeID: Int) extends JSONSerializable
{

    //Serialization
    override def toJSON(editingObj: JSONObject): JSONObject =
    {
        editingObj.put("colour", new JSONArray(colour.map(col => Util.IOUtil.colourToJSON(col)).toArray))
        editingObj.put("position", Util.IOUtil.vector3ToJSON(position))
        editingObj.put("typeID", typeID)

        return editingObj
    }

    val glues = procGlues(setupGlues)

    private def procGlues(setupGlues: Vector[Glue]) : Vector[Glue] =
    {
        var orientation = -1
        def nullGlues: Vector[Glue] = (0 until 4-setupGlues.size).map(index => null).toVector
        def proc(glue: Glue) = if(glue == null) { orientation+= 1; null } else { orientation+= 1; glue.clone(this, orientation) }
        return (setupGlues ++ nullGlues).map(glue => proc(glue))
    }

    def getNorthGlue: Glue = glues(0)
    def getEastGlue: Glue = glues(1)
    def getSouthGlue: Glue = glues(2)
    def getWestGlue: Glue = glues(3)
    def getColour: Colour = colour(0)
    def getPosition: Vector3 = position

    def getStrength(otherTiles: Vector[(Tile, Int)]): Int
    def canBind(otherTiles: Vector[(Tile, Int)]): Boolean
    def clone(newPosition: Vector3, adjacentOrientations: Vector[Int]): Tile
    def clone(newTilePosition: Vector3, exists: Boolean): Tile
}
