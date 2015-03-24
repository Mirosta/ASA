package com.tw10g12.ASA.GUI.Draw

import com.tw10g12.ASA.GUI.Interaction.{AABBIntersectable, Intersectable}
import com.tw10g12.ASA.Model.{SimulationState, Glue, Tile}
import com.tw10g12.ASA.Util
import com.tw10g12.Draw.Engine.{Colour, DrawTools}
import com.tw10g12.Maths.Vector3

/**
 * Created by Tom on 27/10/2014.
 */
object RenderATAMTile
{

    val tileSideSize = 10
    val tileDepth = 1
    val glueSideSize = 1
    val textOffset = 1.7
    val scalingFactor = tileSideSize + glueSideSize*1

    val tileSize = new Vector3(tileSideSize, tileSideSize, tileDepth)
    val filledTileSize = new Vector3(scalingFactor, scalingFactor, tileDepth)

    def renderTile(tile: Tile, lod: Int, drawTools: DrawTools): Unit =
    {
        val position = getRenderPosition(tile.getPosition)
        val size = if (lod > 2) tileSize else filledTileSize

        drawTools.drawCuboid(position.add(size.multiply(new Vector3(-0.5, 0.5, -0.5))), size, Array(tile.getColour))
        if(lod > 2) tile.glues.map(glue => renderGlue(glue, position, lod, Colour.Black, 1, drawTools))
    }

    def getIntersectables(tile: Tile, lod: Int): List[Intersectable] =
    {
        val position = getRenderPosition(tile.getPosition)
        val size = if (lod > 2) tileSize else filledTileSize
        val mainIntersectable: Intersectable = new AABBIntersectable(position.add(size.multiply(new Vector3(-0.5, -0.5, -1.5))), size)
        return mainIntersectable :: tile.glues.flatMap(glue => getGlueIntersectables(glue, position, lod)).toList
    }

    def getGlueIntersectables(glue: Glue, position: Vector3, lod: Int): List[Intersectable] =
    {
        if(glue == null) return List[Intersectable]()

        val glueDirection = Util.orientationToVector(glue.orientation).multiply(new Vector3(-1, 1, 1))
        val newCenter = position.add(tileSize.multiply(0.5).add(new Vector3(glueSideSize, glueSideSize, tileDepth).multiply(0.5)).multiply(glueDirection))
        val glueWidth = 2 * glue.strength - 1
        val glueSize = glueDirection.multiply(glueSideSize).add(glueDirection.cross(new Vector3(0,0,1)).multiply(glueWidth)).add(new Vector3(0,0,tileDepth))

        return List[Intersectable](new AABBIntersectable(newCenter.add(glueSize.multiply(new Vector3(-0.5, -0.5, -1.5))), glueSize))
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

    def renderGlue(glue: Glue, position: Vector3, lod: Int, colour: Colour, sizeMultiplier: Double, drawTools: DrawTools): Unit =
    {
        if(glue == null || glue.strength == 0) return
        val glueDirection = Util.orientationToVector(glue.orientation).multiply(new Vector3(-1, 1, 1))
        val newCenter = position.add(tileSize.multiply(0.5).add(new Vector3(glueSideSize, glueSideSize, tileDepth).multiply(0.5)).multiply(glueDirection))
        val glueWidth = 2 * glue.strength - 1
        val glueSize = (glueDirection.multiply(glueSideSize).add(glueDirection.cross(new Vector3(0,0,1)).multiply(glueWidth)).add(new Vector3(0,0,tileDepth))).multiply(sizeMultiplier)

        drawTools.drawCuboid(newCenter.add(glueSize.multiply(new Vector3(-0.5, 0.5, -0.5))), glueSize, Array(colour))
    }

    def afterRender(tile: Tile, tilePosition: Vector3, lod: Int, checkIncorrectGlues: Boolean, simulationState: SimulationState, drawTools: DrawTools) : Unit =
    {
        if(lod > 4 || checkIncorrectGlues)
        {
            val renderPosition: Vector3 = getRenderPosition(tilePosition)
            tile.glues.map(glue =>
                {
                    if(glue != null)
                    {
                        val vecOrientation = Util.orientationToVector(glue.orientation)
                        val glueDirection = vecOrientation.multiply(new Vector3(-1, 1, 1))
                        if(checkIncorrectGlues)
                        {
                            val adjacentPosition = tilePosition.add(vecOrientation)
                            val adjacentTile = if(simulationState.tiles.contains(adjacentPosition)) simulationState.tiles(adjacentPosition) else null
                            val adjacentGlue = if(adjacentTile == null) null else adjacentTile.glues(Util.oppositeOrientation(glue.orientation))
                            val isIncorrect = adjacentTile != null && (adjacentGlue == null || glue.incorrectBind(adjacentGlue))
                            if(isIncorrect)
                            {
                                //drawTools.drawCuboid(renderPosition.add(new Vector3(0,0,tileDepth)), new Vector3(tileDepth, tileDepth, tileDepth), Array(Colour.Red))
                                renderGlue(glue, renderPosition.add(new Vector3(0,0,tileDepth)), lod, Colour.Red, 2, drawTools)
                            }
                        }
                        if(lod > 4)
                        {
                            val newCenter = renderPosition.add(tileSize.multiply(0.5).add(new Vector3(glueSideSize, glueSideSize, tileDepth).multiply(0.5)).multiply(glueDirection))
                            val textCenter = newCenter.subtract(glueDirection.multiply(textOffset))
                            drawTools.drawText(glue.label, textCenter, 1.5, Colour.Black, 2, new Vector3(0.5, 0.5, 0), 0, 0, 0)
                        }
                    }
                }
            )
        }
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
