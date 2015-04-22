package com.tw10g12.ASA.GUI.DrawPanel

import java.awt.event.{KeyEvent, KeyListener, MouseEvent}
import javax.media.opengl.GLAutoDrawable
import javax.media.opengl.awt.GLJPanel

import com.tw10g12.ASA.GUI.Draw.{RenderATAMTile, RenderSimulation, RenderStateMachine}
import com.tw10g12.ASA.GUI.DrawPanel.EditorState.EditorState
import com.tw10g12.ASA.GUI.Interaction.{BezierIntersectable, Intersectable}
import com.tw10g12.ASA.Launcher
import com.tw10g12.ASA.Model.ATAM.ATAMTile
import com.tw10g12.ASA.Model.StateMachine.{StateNode, StateTransition}
import com.tw10g12.ASA.Model.Tile
import com.tw10g12.Draw.Engine.{Camera, Colour, OrbitCamera}
import com.tw10g12.Maths._

/**
 * Created by Tom on 07/02/2015.
 */

object EditorState extends Enumeration
{
    type EditorState = Value
    val AddState, AddTransition, SetStarting, Delete, Default = Value
}

class EditorDrawPanelEventHandler(tileset: (Tile, List[Tile]), val panel: GLJPanel) extends DrawPanelEventHandler(panel) with KeyListener
{

    var activeTile: Tile = null

    var editingState: EditorState = EditorState.Default
    var connectedNode: StateNode = null
    var fromConnectedDirection: Vector3 = null
    var toConnectedDirection: Vector3 = null

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


    override def beforeRender(drawable: GLAutoDrawable): Unit =
    {
        if(Launcher.editorWindow.editingStateMachine)
        {
            camera.setRotX(0)
            camera.setRotY(0)
            camera.setRotZ(0)
        }
    }

    override def render(drawable: GLAutoDrawable): Unit =
    {
        if(activeTile != null)
        {
            if(!Launcher.editorWindow.editingStateMachine)
            {
                val tileDistance: Double = RenderSimulation.getTileDistance(camera.asInstanceOf[OrbitCamera], activeTile)
                val lod = RenderSimulation.getLOD(tileDistance)
                var tempTile: Tile = activeTile
                if(mouseOver != null && mouseOver.getAttachedModelObject == activeTile) tempTile = activeTile.asInstanceOf[ATAMTile].setColour(Colour.Orange)
                RenderATAMTile.renderTile(tempTile, lod, drawTools)
                RenderATAMTile.afterRender(tempTile, tempTile.getPosition, lod, false, null, drawTools)
            }
            else if(Launcher.editorWindow.stateMachines.contains(activeTile) && Launcher.editorWindow.stateMachines(activeTile) != null)
            {
                RenderStateMachine.render(Launcher.editorWindow.stateMachines(activeTile), camera.asInstanceOf[OrbitCamera], List(mouseOver, selected), drawTools)
                if(mouseOver.isInstanceOf[BezierIntersectable])
                {
                    val selectedDetails = mouseOver.asInstanceOf[BezierIntersectable].getIntersectDistances(lastRay)
                    selectedDetails.map(detail => drawTools.drawCircle(detail._3, new Vector3(0, 1, 0), new Vector3(1, 0, 0), 0.1, 3, new Colour(detail._1.asInstanceOf[Float] / 0.4f, 0, 0)))
                }
                if(editingState == EditorState.AddState)
                {
                    //P0z + cDz = 0 => c = -P0z / Dz

                    RenderStateMachine.renderStateNode(new StateNode(Map(), getRayAtZeroZ, "N" + (Launcher.editorWindow.stateMachines(activeTile).stateNodes.size + 1)), false, null, drawTools)
                }
                else if(editingState == EditorState.AddTransition)
                {
                    var start: Vector3 = null
                    var end: Vector3 = null
                    var startControl: Vector3 = null
                    var endControl: Vector3 = null

                    val pointOver = getRayAtZeroZ

                    if(connectedNode != null)
                    {
                        start = RenderStateMachine.getStartOrEnd(connectedNode.position, fromConnectedDirection)
                        startControl = RenderStateMachine.getStartOrEndControl(start, fromConnectedDirection)
                    }

                    if(mouseOver != null && mouseOver.getAttachedModelObject.isInstanceOf[StateNode])
                    {
                        val selectedNode: StateNode = mouseOver.getAttachedModelObject.asInstanceOf[StateNode]
                        val direction = pointOver.subtract(selectedNode.position).normalise()

                        if(connectedNode == null)
                        {
                            startControl = selectedNode.position
                            start = RenderStateMachine.getStartOrEnd(selectedNode.position, direction)
                            end = RenderStateMachine.getStartOrEndControl(start, direction)
                            endControl = RenderStateMachine.getStartOrEndControl(end, direction)
                        }
                        else
                        {
                            end = RenderStateMachine.getStartOrEnd(selectedNode.position, direction)
                            endControl = RenderStateMachine.getStartOrEndControl(end, direction)
                        }
                    }
                    else if(start != null)
                    {
                        end = pointOver
                        endControl = end.subtract(startControl).normalise().add(end)
                    }
                    if(start != null && startControl != null && end != null && endControl != null)
                    {
                        val curve = new BezierCurve(start, startControl, end, endControl)
                        val middle = curve.getPointAlongCurve(0.5)
                        val direction = middle.subtract(curve.getPointAlongCurve(0.48)).normalise()
                        val arrowLen = RenderStateMachine.nodeRadius / 3
                        val arrowStart = middle.subtract(direction.multiply(arrowLen * 0.5))
                        val arrowEnd = middle.add(direction.multiply(arrowLen * 0.5))
                        val up = direction.cross(new Vector3(0,0,-1))
                        drawTools.drawArrow(arrowStart, arrowEnd, up, false, true, 1, arrowLen, Colour.Black)

                        drawTools.drawBezierCurve(curve, 50, Colour.Black)
                    }
                }
                /*RenderStateMachine.getIntersectables(Launcher.editorWindow.stateMachines(activeTile)).map( inter => inter match
                    {
                        case box: AABBIntersectable => drawTools.drawCuboid(box.start.add(box.size.multiply(new Vector3(0, 0, 0))), box.size, Array(Colour.Red))
                        case _ =>
                    })*/
            }
        }
        if(rayStart != null && rayDirection != null)
        {
            val normalDir = new Vector3(rayDirection.getX, rayDirection.getY, rayDirection.getZ).normalise()
            //drawTools.drawCuboid(rayStart.getXYZ.add(normalDir.multiply(100)), new Vector3(10,10,10), Array(Colour.Black))
        }
    }

    override def checkIntersectables(): Unit =
    {
        if(activeTile != null)
        {
            val intersectables: List[Intersectable] =
                if(!Launcher.editorWindow.editingStateMachine)
                {
                    val tileDistance: Double = RenderSimulation.getTileDistance(camera.asInstanceOf[OrbitCamera], activeTile)
                    val lod = RenderSimulation.getLOD(tileDistance)
                    RenderATAMTile.getIntersectables(activeTile, lod)
                }
                else if(Launcher.editorWindow.stateMachines.contains(activeTile) && Launcher.editorWindow.stateMachines(activeTile) != null)
                {
                    RenderStateMachine.getIntersectables(Launcher.editorWindow.stateMachines(activeTile))
                }
                else List()

            val smallestDistanceIntersectable: (Double, Intersectable) =
                if(editingState ==  EditorState.Default  || editingState == EditorState.AddTransition || editingState == EditorState.SetStarting)
                {
                    (if(editingState == EditorState.AddTransition) intersectables.filter(inter => inter.getAttachedModelObject.isInstanceOf[StateNode]) else intersectables).foldLeft((Double.NaN, null.asInstanceOf[Intersectable]))((smallestDistance: (Double, Intersectable), inter: Intersectable) =>
                    {
                        val distance: Double = inter.rayIntersects(lastRay);
                        if(!distance.isNaN && smallestDistance._1.isNaN() || distance < smallestDistance._1)
                            (distance, inter)
                        else smallestDistance
                    })
                }
                else (Double.NaN, null)
            if(!smallestDistanceIntersectable._1.isNaN) mouseOver = smallestDistanceIntersectable._2
            else mouseOver = null
        }
    }

    override def mouseClicked(e: MouseEvent): Unit =
    {
        super.mouseClicked(e)
        if(e.getButton() == MouseEvent.BUTTON3 && e.getClickCount == 1)
        {
            Launcher.editorWindow.editorController.updateEditingState(EditorState.Default)
        }
        else if(e.getButton == MouseEvent.BUTTON1 && e.getClickCount == 1)
        {
            if(editingState == EditorState.Default)
            {
                selected = mouseOver
                Launcher.editorWindow.editorController.onStateMachinePartSelected(selected)
            }
            if(mouseOver != null && mouseOver.getAttachedModelObject.isInstanceOf[StateNode])
            {
                val selectedNode: StateNode = mouseOver.getAttachedModelObject.asInstanceOf[StateNode]
                val pointClicked = getRayAtZeroZ
                val direction = pointClicked.subtract(selectedNode.position).normalise()
                if(editingState == EditorState.SetStarting)
                {
                    Launcher.editorWindow.editorController.onStateSetAsStarting(selectedNode)
                }
                if(connectedNode == null)
                {
                    connectedNode = selectedNode
                    fromConnectedDirection = direction
                }
                else if(toConnectedDirection == null)
                {
                    toConnectedDirection = direction
                    if(editingState == EditorState.AddTransition)
                    {
                        Launcher.editorWindow.editorController.onTransitionAdded(connectedNode, fromConnectedDirection, selectedNode, toConnectedDirection)
                    }
                }
                else resetNodeConnection()
            }
            else if(mouseOver == null && editingState == EditorState.AddState)
            {
                Launcher.editorWindow.editorController.onStateAdded(getRayAtZeroZ())
            }
        }
    }

    def onEditingStateChanged(editorState: EditorState): Unit =
    {
        resetNodeConnection()
    }

    def resetNodeConnection(): Unit =
    {
        connectedNode = null
        fromConnectedDirection = null
        toConnectedDirection = null
    }

    override def keyTyped(e: KeyEvent): Unit = {}

    override def keyPressed(e: KeyEvent): Unit = {}

    override def keyReleased(e: KeyEvent): Unit =
    {
        if(e.getKeyCode == KeyEvent.VK_DELETE)
        {
            if(selected != null)
            {
                if(selected.getAttachedModelObject.isInstanceOf[StateNode])
                {
                    Launcher.editorWindow.editorController.onStateRemoved(selected.getAttachedModelObject.asInstanceOf[StateNode])
                    Launcher.editorWindow.editorController.onStateMachinePartSelected(null)
                    selected = null
                }
                if(selected.getAttachedModelObject.isInstanceOf[(String, StateTransition)])
                {
                    Launcher.editorWindow.editorController.onTransitionRemoved(selected.getAttachedModelObject.asInstanceOf[(String, StateTransition)])
                    Launcher.editorWindow.editorController.onStateMachinePartSelected(null)
                    selected = null
                }
            }
        }
    }
}
