package com.tw10g12.ASA.GUI.DrawPanel

import java.awt.event._
import javax.media.opengl._
import javax.media.opengl.awt.GLJPanel

import com.tw10g12.ASA.GUI.Draw.RenderMain
import com.tw10g12.ASA.GUI.Interaction.Intersectable
import com.tw10g12.ASA.Util.JOGLUtil
import com.tw10g12.Draw.Engine.Camera.MouseMode
import com.tw10g12.Draw.Engine.{Camera, DrawTools}
import com.tw10g12.Maths._

/**
 * Created by Tom on 20/10/2014.
 */
abstract class DrawPanelEventHandler(panel: GLJPanel) extends GLEventListener with MouseMotionListener with MouseListener with MouseWheelListener
{
    var drawTools: DrawTools = null
    val camera: Camera = setupCamera()
    var lastKnownMouse: Vector2 = null

    var screenSize: Vector2 = new Vector2(0,0)
    var aspectRatio: Double = 0.0
    lazy val nearClipHeight: Double = drawTools.getNearClipHeight()

    var rayStart: Vector4 = null
    var rayDirection: Vector4 = null

    var mouseOver: Intersectable = null
    var selected: Intersectable = null

    var lastRay: Ray3 = null

    var antiAliasing: Boolean = false

    def setupCamera(): Camera

    override def init(drawable: GLAutoDrawable): Unit =
    {
        drawable.setGL(new DebugGL3(drawable.getGL().getGL3()))
        val gl3 = drawable.getGL.getGL3

        this.drawTools = JOGLUtil.getDrawTools(gl3)
        drawTools.setupPerspectiveProjection(drawable.getWidth, drawable.getHeight)
        //drawTools.setupModelView(Matrix4.getIdentityMatrix)
        gl3.glEnable(GL.GL_DEPTH_TEST)
        gl3.glDepthFunc(GL.GL_LESS)
        gl3.glEnable(GL.GL_BLEND)
        gl3.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA)
    }

    override def display(drawable: GLAutoDrawable): Unit =
    {
        drawTools.setGL3(drawable.getGL().getGL3())
        beforeRender(drawable)

        if(antiAliasing)
        {
            drawTools.getGL3.glEnable(GL.GL_LINE_SMOOTH)
            drawTools.getGL3.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST)
        }
        else
        {
            drawTools.getGL3.glDisable(GL.GL_LINE_SMOOTH)
        }
        RenderMain.before(drawTools, camera)
        render(drawable)
        //RenderATAMTile.renderTile(startingTile.clone(new Vector3(-1, 0, 0), Vector[Int]()), drawTools)
        //otherTiles.map(tile => RenderATAMTile.renderTile(tile.clone(new Vector3(tile.typeID, 0, 0), Vector[Int]()), drawTools))
        RenderMain.after(drawTools)
    }

    def beforeRender(drawable: GLAutoDrawable): Unit
    def render(drawable: GLAutoDrawable): Unit

    override def reshape(autoDrawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int): Unit =
    {
        drawTools.setupPerspectiveProjection(width, height)
    }

    override def dispose(p1: GLAutoDrawable): Unit = {println("GLJPanel disposed")}

    def reset(): Unit =
    {
        camera.reset()
    }

    override def mouseClicked(e: MouseEvent): Unit =
    {
        if(e.getButton == MouseEvent.BUTTON2)
        {
            reset()
        }
        if(e.getButton == MouseEvent.BUTTON1 && e.getClickCount == 2)
        {
            camera.doZoomAction(-10)
        }
        else if(e.getButton() == MouseEvent.BUTTON3 && e.getClickCount == 2)
        {
            camera.doZoomAction(10)
        }
    }

    override def mouseEntered(e: MouseEvent): Unit = {}
    override def mouseExited(e: MouseEvent): Unit = {}

    override def mousePressed(e: MouseEvent): Unit = {}

    override def mouseReleased(e: MouseEvent): Unit =
    {
        lastKnownMouse = null
    }

    override def mouseDragged(e: MouseEvent): Unit =
    {
        val currentMouse: Vector2 = new Vector2(e.getX, e.getY)

        if (lastKnownMouse != null)
        {

            val mouseDelta: Vector2 = currentMouse.subtract(lastKnownMouse)

            if ((e.getModifiersEx & InputEvent.BUTTON1_DOWN_MASK) == InputEvent.BUTTON1_DOWN_MASK)
            {
                camera.doMouseAction(mouseDelta, MouseMode.PAN)
            }
            else if ((e.getModifiersEx & InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK)
            {
                camera.doMouseAction(mouseDelta, MouseMode.ROTATE)
            }
        }

        lastKnownMouse = new Vector2(e.getX, e.getY)
    }

    override def mouseMoved(e: MouseEvent) : Unit =
    {
        updateMouseCoords(e)
    }

    def updateMouseCoords(e: MouseEvent): Unit =
    {
        if(drawTools != null)
        {
            val normalisedCoords: Vector2 = getNormalisedMouseCoords(new Vector2(e.getX, e.getY))
            val eyeRay2D: Vector2 = normalisedCoords.multiply(new Vector2(nearClipHeight / aspectRatio, nearClipHeight))
            val inverseMV: Matrix4 = camera.getInverseMatrix(1.0)
            val eyeRayDirection = new Vector4(eyeRay2D, -drawTools.getNearZ, 0)

            rayStart = inverseMV.multiply(new Vector4(0,0,0,1))
            rayDirection = inverseMV.multiply(eyeRayDirection)
            lastRay = new Ray3(rayStart.getXYZ, new Vector3(rayDirection.getX, rayDirection.getY, rayDirection.getZ))

            checkIntersectables()
        }
    }

    def checkIntersectables(): Unit =
    {

    }

    override def mouseWheelMoved(e: MouseWheelEvent) : Unit =
    {
        camera.doZoomAction(e.getPreciseWheelRotation)
    }

    def getRayAtZeroZ(): Vector3 =
    {
        val progressAtZeroZ = -lastRay.getRayStart.getZ / lastRay.getRayDir.getZ
        return lastRay.getPointAlongRay(progressAtZeroZ)
    }

    def getNormalisedMouseCoords(mouseCoords: Vector2): Vector2 =
    {
        return mouseCoords.multiply(new Vector2(1, -1)).add(screenSize.multiply(new Vector2(-0.5, 0.5))).divide(screenSize.multiply(new Vector2(0.5, 0.5)))
    }

    def onAntiAliasingEnabled(): Unit =
    {
        antiAliasing = true
    }

    def onAntiAliasingDisabled(): Unit =
    {
        antiAliasing = false
    }
}
