package com.tw10g12.ASA.GUI.Draw

import com.tw10g12.ASA.Model.StateMachine.{StateTransition, StateMachine, StateNode}
import com.tw10g12.Draw.Engine.{Colour, DrawTools, OrbitCamera}
import com.tw10g12.Maths.Vector3

/**
 * Created by Tom on 26/03/2015.
 */
object RenderStateMachine
{
    val nodeRadius = 1.0
    val controlPointRadius = 1.0

    def render(nodes: List[StateNode], simulationState: StateMachine, camera: OrbitCamera, drawTools: DrawTools): Unit =
    {
        nodes.map(node => renderStateNode(node, node == simulationState.currentNode, drawTools))
    }

    def renderStateNode(node: StateNode, isActive: Boolean, drawTools: DrawTools): Unit =
    {
        val colour = if(isActive) Colour.Green else new Colour(200, 200, 200)
        drawTools.drawCircle(node.position, new Vector3(0, 1, 0), new Vector3(1, 0, 0), nodeRadius, 3, colour)
        drawTools.drawText(node.label, node.position, 1, Colour.Black, 1, new Vector3(0.5, 0.5, 0), 0, 0, 0)
        node.transitions.map(pair => pair._2.map(transition => renderStateTransition(transition, pair._1, drawTools)))
    }

    def renderStateTransition(stateTransition: StateTransition, label: String, drawTools: DrawTools): Unit =
    {
        val startPos: Vector3 = stateTransition.from.position.add(stateTransition.fromDirection.multiply(nodeRadius))
        val endPos: Vector3 = stateTransition.to.position.add(stateTransition.toDirection.multiply(nodeRadius))

        val startControl: Vector3 = startPos.add(stateTransition.fromDirection.multiply(controlPointRadius))
        val endControl: Vector3 = endPos.add(stateTransition.toDirection.multiply(controlPointRadius))

        drawTools.drawBezierCurve(startPos, endPos, startControl, endControl, 50, Colour.Black)

        val middle = drawTools.calculateBezierPoint(0.5, startPos, endPos, startControl, endControl)
        drawTools.drawText(label + " : " + stateTransition.probability, middle.add(new Vector3(0, 0.2, 0)), 1, Colour.Black, 1, new Vector3(0.5, 0, 0), 0, 0, 0)
    }
}
