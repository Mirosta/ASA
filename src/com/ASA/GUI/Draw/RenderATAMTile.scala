package com.ASA.GUI.Draw

import com.ASA.Model.{Glue, Tile}
import com.ASA.Util
import com.tw10g12.Draw.Engine.{Colour, DrawTools}
import com.tw10g12.Maths.{Vector3, Vector2}

/**
 * Created by Tom on 27/10/2014.
 */
object RenderATAMTile
{
    val tileSideSize = 10
    val tileDepth = 1
    val tileSize = new Vector3(tileSideSize, tileSideSize, tileDepth)
    val glueSideSize = 1
    val textOffset = 1.7
    val scalingFactor = tileSideSize + glueSideSize*1

    def renderTile(tile: Tile, lod: Int, drawTools: DrawTools): Unit =
    {
        val position = getRenderPosition(tile.getPosition)
        if(lod == 1 && tile.getPosition.getX % 3 == 1 && tile.getPosition.getY % 3 == 1) return
        if(lod == 0 && tile.getPosition.getX % 2 == 1 && tile.getPosition.getY % 2 == 1) return

        if(!drawTools.isPointVisible(position, true, 0.5))
        {
            return
        }
        drawTools.drawCuboid(position.add(tileSize.multiply(new Vector3(-0.5, 0.5, -0.5))), tileSize, Array(tile.getColour))
        if(lod > 2) tile.glues.map(glue => renderGlue(glue, position, lod, drawTools))
    }

    def getRenderPosition(tilePosition: Vector3): Vector3 =
    {
        return tilePosition.multiply(new Vector3(-scalingFactor, scalingFactor, 1))
    }

    def renderGlue(glue: Glue, position: Vector3, lod: Int, drawTools: DrawTools): Unit =
    {
        if(glue == null) return
        val glueDirection = Util.orientationToVector(glue.orientation).multiply(new Vector3(-1, 1, 1))
        val newCenter = position.add(tileSize.multiply(0.5).add(new Vector3(glueSideSize, glueSideSize, tileDepth).multiply(0.5)).multiply(glueDirection))
        val textCenter = newCenter.subtract(glueDirection.multiply(textOffset))
        val glueWidth = 2 * glue.strength - 1
        val glueSize = glueDirection.multiply(glueSideSize).add(glueDirection.cross(new Vector3(0,0,1)).multiply(glueWidth))

        drawTools.drawCuboid(newCenter.add(glueSize.multiply(new Vector3(-0.5, 0.5, -0.5))), glueSize, Array(Colour.Black))
        if(lod > 4) drawTools.drawText(glue.label, textCenter, 1.5, Colour.Black, 2, new Vector3(0.5, 0.5, 0), 0, 0, 0)
    }

    def getDistanceSq(pos: Vector3, t: Tile): Double =
    {
        return pos.subtract(getRenderPosition(t.getPosition)).lengthSquared()
    }
}
