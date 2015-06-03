package com.tw10g12.ASA.Model.JSON

import com.tw10g12.ASA.Model.ATAM.ATAMTile
import com.tw10g12.ASA.Model.SMTAM.SMTAMTile
import com.tw10g12.ASA.Model.{Glue, Tile}
import com.tw10g12.ASA.Util
import com.tw10g12.Draw.Engine.Colour
import com.tw10g12.Maths.Vector3
import org.json.JSONObject

/**
 * Created by Tom on 17/03/2015.
 */
object JSONTileFactory
{
    val tileFactories: Map[String, JSONTileFactory] = Map[String, JSONTileFactory]("ATAMTile" -> new JSONATAMTileFactory(), "SMTAMTile" -> new JSONSMTAMTileFactory(), "KTAMTile" -> new JSONATAMTileFactory())

    def createTile(serialized: JSONObject): Tile =
    {
        val tileClassID: String = serialized.getString("tileClassID")
        val colours: Vector[Colour] = Util.IOUtil.JSONArrayToArray[JSONObject](serialized.getJSONArray("colour")).map(col => Util.IOUtil.JSONToColour(col)).toVector
        val position: Vector3 = Util.IOUtil.JSONToVector3(serialized.getJSONObject("position"))
        val typeID: Int = serialized.getInt("typeID")

        return tileFactories(tileClassID).createTile(serialized, colours, position, typeID)
    }
}

trait JSONTileFactory
{
    def createTile(serialized: JSONObject, colours: Vector[Colour], position: Vector3, typeID: Int): Tile
}

class JSONATAMTileFactory extends JSONTileFactory
{
    override def createTile(serialized: JSONObject,  colours: Vector[Colour], position: Vector3, typeID: Int): Tile =
    {
        val glueArr = Util.IOUtil.JSONMixedArrayToArray(serialized.getJSONArray("glues"))
        val glues: Vector[Glue] = glueArr.map(serializedGlue => createGlue(serializedGlue)).toVector
        return new ATAMTile(glues, colours, position, typeID)
    }

    def createGlue(serialized: AnyRef): Glue =
    {
        if(serialized == null || !serialized.isInstanceOf[JSONObject]) return null
        return JSONGlueFactory.createGlue(serialized.asInstanceOf[JSONObject])
    }
}

class JSONSMTAMTileFactory extends JSONATAMTileFactory
{
    override def createTile(serialized: JSONObject,  colours: Vector[Colour], position: Vector3, typeID: Int): Tile =
    {
        return new SMTAMTile(super.createTile(serialized, colours, position, typeID).asInstanceOf[ATAMTile])
    }
}