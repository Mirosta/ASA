package com.tw10g12.ASA.GUI.DrawPanel

import java.awt.event.MouseEvent
import java.util.concurrent.atomic.AtomicBoolean
import javax.media.opengl.GLAutoDrawable
import javax.media.opengl.awt.GLJPanel

import com.tw10g12.ASA.Controller.{SimulationController, SimulationStateEnum}
import com.tw10g12.ASA.GUI.Draw.{RenderATAMTile, RenderSimulation, RenderStateMachine}
import com.tw10g12.ASA.GUI.Interaction.Intersectable
import com.tw10g12.ASA.Launcher
import com.tw10g12.ASA.Model.{SMTAMSimulationState, SimulationState, Tile}
import com.tw10g12.Draw.Engine.{Camera, Colour, OrbitCamera}
import com.tw10g12.Maths._

/**
 * Created by Tom on 20/10/2014.
 */
class SimulationDrawPanelEventHandler(val simulationController: SimulationController, panel: GLJPanel) extends DrawPanelEventHandler(panel)
{
    var activeTile: Tile = null
    var mouseGridPos: Vector3 = null

    var debugShowAdjacencies: AtomicBoolean = new AtomicBoolean(false)

    def setShowAdjacencies(showAdjacencies: Boolean): Unit =
    {
        debugShowAdjacencies.set(showAdjacencies)
    }

    override def setupCamera(): Camera =
    {
        return new OrbitCamera(new Vector3(-1450, 1400, 0.0), 2700)
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


    override def beforeRender(drawable: GLAutoDrawable): Unit = {}

    override def render(drawable: GLAutoDrawable): Unit =
    {
        val simulationState: SimulationState = simulationController.getSimulationState
        RenderSimulation.render(simulationState, camera.asInstanceOf[OrbitCamera], debugShowAdjacencies.get(), drawTools)
        if(simulationController.state != SimulationStateEnum.Running && lastRay != null)
        {
            val mouseGridRadius = 4
            val mouseGridRadiusSq = mouseGridRadius * mouseGridRadius

            (-mouseGridRadius to mouseGridRadius).map(y =>
                {
                    (-mouseGridRadius to mouseGridRadius).map(x =>
                    {
                        val relativeGridPos = new Vector3(x, y, 0)
                        val absoluteGridPos = mouseGridPos.add(relativeGridPos)
                        if(relativeGridPos.lengthSquared() <= mouseGridRadiusSq)
                        {
                            val invDistance = (relativeGridPos.lengthSquared / (mouseGridRadiusSq * 1.2)).asInstanceOf[Float]
                            val depth = if(simulationState.tiles.contains(absoluteGridPos)) -0.3 else 0
                            RenderATAMTile.renderTileOutline(absoluteGridPos.subtract(new Vector3(0,0,depth + invDistance*0.1)), new Colour(invDistance, invDistance, invDistance), drawTools)
                        }
                    })
                })
        }
        if(simulationState.isInstanceOf[SMTAMSimulationState])
        {
            val sMTAMSimulationState: SMTAMSimulationState = simulationState.asInstanceOf[SMTAMSimulationState]
            if(activeTile != null && sMTAMSimulationState.stateMachineStates.contains(activeTile.getPosition) && RenderSimulation.getLOD(RenderSimulation.getTileDistance(camera.asInstanceOf[OrbitCamera], activeTile)) > 4)
            {
                drawTools.end()
                drawTools.start()
                val stateMachine = sMTAMSimulationState.stateMachineStates(activeTile.getPosition)
                val minPos = if(stateMachine.currentNode == null) new Vector3() else stateMachine.stateNodes.foldLeft(stateMachine.currentNode.position)((min, node) => min.min(node.position))
                val maxPos = if(stateMachine.currentNode == null) new Vector3() else stateMachine.stateNodes.foldLeft(stateMachine.currentNode.position)((max, node) => max.max(node.position))
                val sizePos = maxPos.subtract(minPos)
                val midPos = minPos.add(maxPos).multiply(0.5)

                val stateMachineCamera: OrbitCamera = new OrbitCamera(midPos, Math.max(sizePos.getX, sizePos.getY))
                drawTools.setModelView(stateMachineCamera.getMatrix(1))
                RenderStateMachine.render(stateMachine, stateMachineCamera, List(), drawTools)
                drawTools.end()
                drawTools.start()
            }
        }
    }

    override def mouseClicked(e: MouseEvent): Unit =
    {
        super.mouseClicked(e)
        updateMouseCoords(e)

        if(simulationController.state != SimulationStateEnum.Running)
        {
            if(e.getButton == MouseEvent.BUTTON1 && e.getClickCount == 1)
            {
                Launcher.simulationWindow.simulationPanel.controller.onGridClicked(mouseGridPos)
            }
        }
    }

    lazy val tileSpacing = RenderATAMTile.getRenderPosition(new Vector3(1,1,1))

    override def checkIntersectables(): Unit =
    {
        var smallest: Double = Double.MaxValue
        activeTile = null

        mouseGridPos = getRayAtZeroZ().subtract(tileSpacing.multiply(-0.5)).divide(tileSpacing).floor()

        if (simulationController.getSimulationState().isInstanceOf[SMTAMSimulationState])
        {
            simulationController.getSimulationState().tiles.map(pair =>
            {
                val currentTile = pair._2
                val tileDistance: Double = RenderSimulation.getTileDistance(camera.asInstanceOf[OrbitCamera], currentTile)
                val lod = RenderSimulation.getLOD(tileDistance)
                if (lod > 4)
                {
                    val intersectables = RenderATAMTile.getIntersectables(currentTile, lod)
                    val ray: Ray3 = new Ray3(rayStart.getXYZ, new Vector3(rayDirection.getX, rayDirection.getY, rayDirection.getZ))
                    val smallestDistance: Double = intersectables.foldLeft(Double.NaN)((smallestDistance: Double, inter: Intersectable) =>
                    {
                        val distance: Double = inter.rayIntersects(ray);
                        if (!distance.isNaN && smallestDistance.isNaN() || distance < smallestDistance) distance else smallestDistance
                    })

                    if (!smallestDistance.isNaN && smallestDistance < smallest)
                    {
                        activeTile = pair._2
                    }
                }
            })
        }
    }

}
