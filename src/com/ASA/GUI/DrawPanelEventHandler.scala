package com.ASA.GUI

import java.awt.event._
import javax.media.opengl.glu.GLU
import javax.media.opengl.{GL, GL2, GLAutoDrawable, GLEventListener}

import com.ASA.Controller.SimulationController
import com.ASA.GUI.Draw.{RenderATAMTile, RenderSimulation, RenderMain}
import com.ASA.Model.ATAM.{ATAMGlue, ATAMTile}
import com.ASA.Model.{Tile, Simulation}
import com.jogamp.opengl.util.gl2.GLUT
import com.tw10g12.Draw.Engine.Camera.MouseMode
import com.tw10g12.Draw.Engine._
import com.tw10g12.Maths.{Vector2, Vector3, Matrix4}

/**
 * Created by Tom on 20/10/2014.
 */
class DrawPanelEventHandler(val simulationController: SimulationController) extends GLEventListener with MouseMotionListener with MouseListener with MouseWheelListener
{
    var drawTools: DrawTools = null
    val camera: Camera = new OrbitCamera(new Vector3(-500, 500, 0), 1000)
    var lastKnownMouse: Vector2 = null

    override def init(event: GLAutoDrawable): Unit =
    {

        val gl3 = event.getGL.getGL3
        val shaderLoader: ShaderLoader = new ShaderLoader("shaders/main")

        this.drawTools = new DrawTools(gl3, new GLU(), new GLUT(), shaderLoader, shaderLoader)
        drawTools.setupPerspectiveProjection(event.getWidth, event.getHeight)
        //drawTools.setupModelView(Matrix4.getIdentityMatrix)
        gl3.glEnable(GL.GL_DEPTH_TEST)
        gl3.glDepthFunc(GL.GL_LESS)
    }

    override def display(p1: GLAutoDrawable): Unit =
    {
        RenderMain.before(drawTools, camera)
        //RenderATAMTile.renderTile(startingTile.clone(new Vector3(-1, 0, 0), Vector[Int]()), drawTools)
        //otherTiles.map(tile => RenderATAMTile.renderTile(tile.clone(new Vector3(tile.typeID, 0, 0), Vector[Int]()), drawTools))
        RenderSimulation.render(simulationController.getSimulationState, camera.asInstanceOf[OrbitCamera], drawTools)
        RenderMain.after(drawTools)
    }

    override def reshape(autoDrawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int): Unit =
    {
        drawTools.setupPerspectiveProjection(width, height)
    }

    override def dispose(p1: GLAutoDrawable): Unit = {println("GLJPanel disposed")}

    def reset(): Unit =
    {
        camera.setCameraPos(new Vector3(0,0,0))
        camera.asInstanceOf[OrbitCamera].setDistance(100)
        camera.setRotX(0)
        camera.setRotY(0)
        camera.setRotZ(0)
    }

    override def mouseClicked(e: MouseEvent): Unit =
    {
        if(e.getButton == MouseEvent.BUTTON2)
        {
            reset()
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
