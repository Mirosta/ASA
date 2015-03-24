package com.tw10g12.ASA.Model

import com.tw10g12.IO.JSONSerializable
import org.json.JSONObject

/**
 * Created by Tom on 27/10/2014.
 */
abstract class Glue(val label: String, val strength: Int, val orientation: Int, val parent: Tile, val isBound: Boolean) extends JSONSerializable
{
    //Serialization
    override def toJSON(editingObj: JSONObject): JSONObject =
    {
        editingObj.put("label", label)
        editingObj.put("strength", strength)
        editingObj.put("orientation", orientation)

        return editingObj
    }

    def clone(newParent: Tile, newOrientation: Int): Glue
    def clone(newIsBound: Boolean): Glue
    def canBind(otherGlue: Glue): Boolean
    def incorrectBind(otherGlue: Glue): Boolean
}
