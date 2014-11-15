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

        drawTools.drawCuboid(position.add(tileSize.multiply(new Vector3(-0.5, 0.5, -0.5))), tileSize, Array(tile.getColour))
        if(lod > 2) tile.glues.map(glue => renderGlue(glue, position, lod, drawTools))
    }

    def instanceRender(tile: Tile, drawTools: DrawTools): Unit =
    {
        val position = getRenderPosition(tile.getPosition)
        if(drawTools.isPointVisible(position, true, 0.5))
        {
            drawTools.drawInstance(position)
        }
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
        val glueSize = glueDirection.multiply(glueSideSize).add(glueDirection.cross(new Vector3(0,0,1)).multiply(glueWidth)).add(new Vector3(0,0,tileDepth));

        drawTools.drawCuboid(newCenter.add(glueSize.multiply(new Vector3(-0.5, 0.5, -0.5))), glueSize, Array(Colour.Black))
        if(lod > 4) drawTools.drawText(glue.label, textCenter, 1.5, Colour.Black, 2, new Vector3(0.5, 0.5, 0), 0, 0, 0)
    }

    def getDistanceSq(pos: Vector3, t: Tile): Double =
    {
        return pos.subtract(getRenderPosition(t.getPosition)).lengthSquared()
    }

    def getLODIndex(lod: Int): Int =
    {
        if(lod > 4) return 2
        if(lod > 2) return 1
        return 0
    }

    def getLODFromIndex(lodIndex: Int): Int =
    {
        val lod: Vector[Int] = Vector[Int](0, 3, 5)
        return lod(lodIndex)
    }
}
