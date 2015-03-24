package com.tw10g12.ASA.GUI.DrawPanel

import java.awt.event._
import javax.media.opengl.{DebugGL3, GL, GLAutoDrawable, GLEventListener}

import com.tw10g12.ASA.GUI.Draw.RenderMain
import com.tw10g12.ASA.Util.JOGLUtil
import com.tw10g12.Draw.Engine.Camera.MouseMode
import com.tw10g12.Draw.Engine.{Camera, DrawTools}
import com.tw10g12.Maths.Vector2

/**
 * Created by Tom on 20/10/2014.
 */
abstract class DrawPanelEventHandler extends GLEventListener with MouseMotionListener with MouseListener with MouseWheelListener
{
    var drawTools: DrawTools = null
    val camera: Camera = setupCamera()
    var lastKnownMouse: Vector2 = null

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
    }

    override def display(drawable: GLAutoDrawable): Unit =
    {
        drawTools.setGL3(drawable.getGL().getGL3())
        RenderMain.before(drawTools, camera)
        render(drawable)
        //RenderATAMTile.renderTile(startingTile.clone(new Vector3(-1, 0, 0), Vector[Int]()), drawTools)
        //otherTiles.map(tile => RenderATAMTile.renderTile(tile.clone(new Vector3(tile.typeID, 0, 0), Vector[Int]()), drawTools))
        RenderMain.after(drawTools)
    }

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

    override def mouseMoved(e: MouseEvent) : Unit = {}

    override def mouseWheelMoved(e: MouseWheelEvent) : Unit =
    {
        camera.doZoomAction(e.getPreciseWheelRotation)
    }
}
