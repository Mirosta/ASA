package com.tw10g12.ASA.Model.JSON

import com.tw10g12.ASA.Model.ATAM.ATAMGlue
import com.tw10g12.ASA.Model.Glue
import org.json.JSONObject

/**
 * Created by Tom on 17/03/2015.
 */
object JSONGlueFactory
{
    val glueFactories: Map[String, JSONGlueFactory] = Map[String, JSONGlueFactory]("ATAMGlue" -> new ATAMJSONGlueFactory())

    def createGlue(serialized: JSONObject): Glue =
    {
        if(serialized == null) return null
        val glueClassID: String = serialized.getString("glueClassID")
        val label: String = serialized.getString("label")
        val strength: Int = serialized.getInt("strength")
        val orientation: Int = serialized.getInt("orientation")
        return glueFactories(glueClassID).createGlue(serialized, label, strength, orientation)
    }
}

trait JSONGlueFactory
{
    def createGlue(serialized: JSONObject, label: String, strength: Int, orientation: Int): Glue
}

class ATAMJSONGlueFactory extends JSONGlueFactory
{
    override def createGlue(serialized: JSONObject, label: String, strength: Int, orientation: Int): Glue =
    {
        return new ATAMGlue(label, strength, orientation, null, false)
    }
}
