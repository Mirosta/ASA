package com.tw10g12.ASA.Model.SMTAM

import com.tw10g12.ASA.Model.ATAM.{ATAMGlue, ATAMTile}
import com.tw10g12.ASA.Model.StateMachine.GlueState
import com.tw10g12.ASA.Model.StateMachine.GlueState.GlueState
import com.tw10g12.ASA.Model.{Glue, Tile}
import com.tw10g12.ASA.Util
import com.tw10g12.Draw.Engine.Colour
import com.tw10g12.Maths.Vector3

/**
 * Created by Tom on 30/03/2015.
 */
class SMTAMTile(setupGlues: Vector[Glue], colours: Vector[Colour], position: Vector3, typeID: Int) extends ATAMTile(setupGlues, colours, position, typeID)
{
    def this(aTAMTile: ATAMTile) = this(aTAMTile.glues.map(glue => if(glue == null) null.asInstanceOf[SMTAMGlue] else new SMTAMGlue(glue.asInstanceOf[ATAMGlue])), Vector[Colour](aTAMTile.getColour), aTAMTile.getPosition, aTAMTile.typeID)

    //override def getStrength(otherTiles: Vector[(Tile, Int)]): Int = super.getStrength(otherTiles)

    def canBind(otherTiles: Vector[(Tile, Int)], glueStates: Map[Vector3, Map[Int, GlueState]], tileGlueStates: Map[Int, GlueState]): Boolean =
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
                    case otherTile: SMTAMTile =>
                        val reverseOrientation = Util.oppositeOrientation(orientation)
                        val otherGlue = otherTile.glues(reverseOrientation)
                        val ownGlue = this.glues(orientation)
                        val ownGlueState = if(tileGlueStates.contains(orientation)) tileGlueStates(orientation) else GlueState.Active
                        val otherGlueState = if(glueStates(otherTile.getPosition).contains(reverseOrientation)) glueStates(otherTile.getPosition)(reverseOrientation) else GlueState.Active

                        if (ownGlue != null && otherGlue != null)
                        {
                            val bindResult = ownGlue.asInstanceOf[SMTAMGlue].canBind(otherGlue, ownGlueState, otherGlueState);
                            if(bindResult == -1) return false
                            else if(bindResult == 1) validStrength += ownGlue.strength
                        }
                }
            }
        }
        return validStrength >= 2
    }

    override def clone(newPosition: Vector3, adjacentOrientations: Vector[Int]): Tile =
    {
        val newGlues = glues.map(glue => glueMapFun(glue, adjacentOrientations, true))
        return new SMTAMTile(newGlues, colours, newPosition, typeID)
    }

    override def clone(newTilePosition: Vector3, exists: Boolean): Tile =
    {
        val adjacentOrientations = Vector[Int](Util.vectorToOrientation(position.add(newTilePosition.multiply(-1))))
        val newGlues = glues.map(glue => glueMapFun(glue, adjacentOrientations, exists))
        return new SMTAMTile(newGlues, colours, position, typeID)
    }

    override def setColour(newColour: Colour): SMTAMTile =
    {
        return new SMTAMTile(glues, Vector[Colour](newColour), position, typeID)
    }

    override def setGlue(glue: Glue, glueIndex: Int): SMTAMTile =
    {
        var index = -1
        val newGlues = glues.map(g => { index += 1; if(index == glueIndex) glue else g } )
        return new SMTAMTile(newGlues, colours, position, typeID)
    }

    override def setTypeID(newTypeID: Int): SMTAMTile =
    {
        return new SMTAMTile(glues, colours, position, newTypeID)
    }

}
