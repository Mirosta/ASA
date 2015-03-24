package com.tw10g12.ASA.Model.ATAM

import com.tw10g12.ASA.Model.{Tile, Glue}
import org.json.JSONObject

/**
 * Created by Tom on 27/10/2014.
 */
class ATAMGlue(label: String, strength: Int, orientation: Int, parent: ATAMTile, isBound: Boolean) extends Glue(label, strength, orientation, parent, isBound)
{
    def this(label: String, strength: Int) = this(label, strength, -1, null, false)

    //Serialization
    override def toJSON(editingObj: JSONObject): JSONObject =
    {
        val newEditingObj = super.toJSON(editingObj)
        newEditingObj.put("glueClassID", "ATAMGlue")
        return newEditingObj
    }

    override def clone(newParent: Tile, newOrientation: Int): Glue =
    {
        new ATAMGlue(label, strength, newOrientation, newParent.asInstanceOf[ATAMTile], isBound)
    }

    override def clone(newIsBound: Boolean): Glue =
    {
        new ATAMGlue(label, strength, orientation, parent, newIsBound)
    }

    override def canBind(otherGlue: Glue): Boolean =
    {
        otherGlue match
        {
            case glue: ATAMGlue =>
                if(glue.isBound || this.isBound) return false
                if(!glue.label.equals(this.label)) return false
                if(glue.strength != this.strength) return false
                return true
        }
        return false
    }

    override def incorrectBind(otherGlue: Glue): Boolean =
    {
        otherGlue match
        {
            case glue: ATAMGlue =>
                if(!glue.label.equals(this.label)) return true
                if(glue.strength != this.strength) return true
                return false
        }
        return true
    }
}