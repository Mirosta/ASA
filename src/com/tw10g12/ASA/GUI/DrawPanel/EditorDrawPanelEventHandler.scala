package com.tw10g12.ASA.GUI.DrawPanel

import java.awt.event.MouseEvent
import javax.media.opengl.GLAutoDrawable

import com.tw10g12.ASA.GUI.Draw.{RenderATAMTile, RenderSimulation}
import com.tw10g12.ASA.GUI.Interaction.Intersectable
import com.tw10g12.ASA.Model.ATAM.ATAMTile
import com.tw10g12.ASA.Model.Tile
import com.tw10g12.Draw.Engine.{Colour, Camera, OrbitCamera}
import com.tw10g12.Maths._

/**
 * Created by Tom on 07/02/2015.
 */
class EditorDrawPanelEventHandler(tileset: (Tile, List[Tile])) extends DrawPanelEventHandler
{
    var activeTile: Tile = null
    var screenSize: Vector2 = new Vector2(0,0)
    var aspectRatio: Double = 0.0
    lazy val nearClipHeight: Double = drawTools.getNearClipHeight()

    var rayStart: Vector4 = null
    var rayDirection: Vector4 = null
    var selected: Boolean = false

    override def setupCamera(): Camera =
    {
        return new OrbitCamera(new Vector3(0.0, 0.0, 0.0), 50.0)
    }


    override def init(drawable: GLAutoDrawable): Unit =
    {
        super.init(drawable)
        screenSize = new Vector2(drawable.getWidth, drawable.getHeight)
        updateAspectRatio()
    }


    override def reshape(autoDrawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int): Unit =
    {
        super.reshape(autoDrawable, x, y, width, height)
        screenSize = new Vector2(autoDrawable.getWidth, autoDrawable.getHeight)
        updateAspectRatio()
    }

    def updateAspectRatio(): Unit =
    {
        aspectRatio = screenSize.getY / screenSize.getX
    }

    override def render(drawable: GLAutoDrawable): Unit =
    {
        if(activeTile != null)
        {
            val tileDistance: Double = RenderSimulation.getTileDistance(camera.asInstanceOf[OrbitCamera], activeTile)
            val lod = RenderSimulation.getLOD(tileDistance)
            var tempTile: Tile = activeTile
            if(selected) tempTile = activeTile.asInstanceOf[ATAMTile].setColour(Colour.Orange)
            RenderATAMTile.renderTile(tempTile, lod, drawTools)
            RenderATAMTile.afterRender(tempTile, tempTile.getPosition, lod, false, null, drawTools)
        }
        if(rayStart != null && rayDirection != null)
        {
            val normalDir = new Vector3(rayDirection.getX, rayDirection.getY, rayDirection.getZ).normalise()
            //drawTools.drawCuboid(rayStart.getXYZ.add(normalDir.multiply(100)), new Vector3(10,10,10), Array(Colour.Black))
        }
    }

    def getNormalisedMouseCoords(mouseCoords: Vector2): Vector2 =
    {
        return mouseCoords.multiply(new Vector2(1, -1)).add(screenSize.multiply(new Vector2(-0.5, 0.5))).divide(screenSize.multiply(new Vector2(0.5, 0.5)))
    }

    override def mouseMoved(e: MouseEvent): Unit =
    {
        super.mouseMoved(e)
        val normalisedCoords: Vector2 = getNormalisedMouseCoords(new Vector2(e.getX, e.getY))
        val eyeRay2D: Vector2 = normalisedCoords.multiply(new Vector2(nearClipHeight / aspectRatio, nearClipHeight))
        val inverseMV: Matrix4 = camera.getInverseMatrix(1.0)
        val eyeRayDirection = new Vector4(eyeRay2D, -drawTools.getNearZ, 0)

        rayStart = inverseMV.multiply(new Vector4(0,0,0,1))
        rayDirection = inverseMV.multiply(eyeRayDirection)

        if(activeTile != null)
        {
            val tileDistance: Double = RenderSimulation.getTileDistance(camera.asInstanceOf[OrbitCamera], activeTile)
            val lod = RenderSimulation.getLOD(tileDistance)
            val intersectables = RenderATAMTile.getIntersectables(activeTile, lod)
            val ray: Ray3 = new Ray3(rayStart.getXYZ, new Vector3(rayDirection.getX, rayDirection.getY, rayDirection.getZ))
            val smallestDistance: Double = intersectables.foldLeft(Double.NaN)((smallestDistance: Double, inter: Intersectable) => {val distance: Double = inter.rayIntersects(ray); if(!distance.isNaN && smallestDistance.isNaN() || distance < smallestDistance) distance else smallestDistance})

            selected = !smallestDistance.isNaN()
        }
    }
}
