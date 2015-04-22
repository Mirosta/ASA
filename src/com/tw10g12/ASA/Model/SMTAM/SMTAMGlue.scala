package com.tw10g12.ASA.Model.SMTAM

import com.tw10g12.ASA.Model.ATAM.ATAMGlue
import com.tw10g12.ASA.Model.StateMachine.GlueState
import com.tw10g12.ASA.Model.StateMachine.GlueState.GlueState
import com.tw10g12.ASA.Model.{Glue, Tile}

/**
 * Created by Tom on 30/03/2015.
 */
class SMTAMGlue(label: String, strength: Int, orientation: Int, parent: SMTAMTile, isBound: Boolean) extends ATAMGlue(label, strength, orientation, parent, isBound)
{
    def this(aTAMGlue: ATAMGlue) = this(aTAMGlue.label, aTAMGlue.strength, aTAMGlue.orientation, null, aTAMGlue.isBound)

    def canBind(otherGlue: Glue, glueState: GlueState, otherGlueState: GlueState): Boolean =
    {
        otherGlue match
        {
            case glue: ATAMGlue =>
                if(glue.isBound || this.isBound) return false
                if(!glue.label.equals(this.label)) return false
                if(glue.strength != this.strength) return false
                if(glueState != GlueState.Active || otherGlueState != GlueState.Active) return false
                return true
        }
        return false
    }

    override def clone(newParent: Tile, newOrientation: Int): Glue =
    {
        new SMTAMGlue(label, strength, newOrientation, newParent.asInstanceOf[SMTAMTile], isBound)
    }

    override def clone(newIsBound: Boolean): Glue =
    {
        new SMTAMGlue(label, strength, orientation, parent, newIsBound)
    }
}
