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

    def canBind(otherGlue: Glue, glueState: GlueState, otherGlueState: GlueState): Int =
    {
        otherGlue match
        {
            case glue: SMTAMGlue =>
                //if(glue.isBound || this.isBound) return false
                if(!glue.label.equals(this.label)) return -1
                if(glue.strength != this.strength) return -1
                if(glueState != GlueState.Active || otherGlueState != GlueState.Active) return 0
                return 1
        }
        return 0
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
