package com.tw10g12.ASA.GUI.Draw

import com.tw10g12.ASA.GUI.Interaction.{BezierIntersectable, CirclePlaneIntersectable, Intersectable}
import com.tw10g12.ASA.Model.StateMachine.{StateMachine, StateNode, StateTransition}
import com.tw10g12.Draw.Engine.{Colour, DrawTools, OrbitCamera}
import com.tw10g12.Maths.{BezierCurve, Vector3}

/**
 * Created by Tom on 26/03/2015.
 */
object RenderStateMachine
{
    val nodeRadius = 1.0
    val controlPointRadius = 1.0

    def render(simulationState: StateMachine, camera: OrbitCamera, selected: List[Intersectable], drawTools: DrawTools): Unit =
    {
        simulationState.stateNodes.map(node => renderStateNode(node, node == simulationState.currentNode, selected.filter(sel => sel != null), drawTools))
    }

    def renderStateNode(node: StateNode, isActive: Boolean, selected: List[Intersectable], drawTools: DrawTools): Unit =
    {
        val isSelected = selected != null && selected.exists(sel => sel.getAttachedModelObject == node)
        val colour = if(isSelected) Colour.Blue else if(isActive) Colour.Green else new Colour(200, 200, 200)
        drawTools.drawCircle(node.position, new Vector3(0, 1, 0), new Vector3(1, 0, 0), nodeRadius, 3, colour)
        drawTools.drawText(node.label, node.position, 1, Colour.Black, 1, new Vector3(0.5, 0.5, 0), 0, 0, 0)

        val gluesInfoTop = node.position.add(new Vector3(0,-1,0).multiply(nodeRadius * 1.5))
        drawTools.drawText(node.gluesText, gluesInfoTop, 1, Colour.Black, 1, new Vector3(0.5, 1, 0), 0, 0, 0)

        if(node.transitions != null) node.transitions.map(pair => pair._2.map(transition => renderStateTransition(transition, pair._1, selected.exists(sel => transition == sel.getAttachedModelObject), drawTools)))
    }

    def renderStateTransition(stateTransition: StateTransition, label: String, isSelected: Boolean, drawTools: DrawTools): Unit =
    {
        val curve = getBezierCurve(stateTransition)

        val lineColour = if(isSelected) Colour.Orange else Colour.Black
        drawTools.drawBezierCurve(curve, 50, lineColour)

        val middle = curve.getPointAlongCurve(0.5)
        val direction = middle.subtract(curve.getPointAlongCurve(0.48)).normalise()
        val arrowLen = nodeRadius / 3
        val arrowStart = middle.subtract(direction.multiply(arrowLen * 0.5))
        val arrowEnd = middle.add(direction.multiply(arrowLen * 0.5))
        val up = direction.cross(new Vector3(0,0,-1))
        drawTools.drawArrow(arrowStart, arrowEnd, up, false, true, 1, arrowLen, lineColour)
        drawTools.drawText(label + " : " + stateTransition.probability, middle.add(new Vector3(0, 0.2, 0)), 1, lineColour, 1, new Vector3(0.5, 0, 0), 0, 0, 0)
    }

    def getStartOrEnd(nodePos: Vector3, direction: Vector3): Vector3 =
    {
        nodePos.add(direction.multiply(nodeRadius))
    }

    def getStartOrEndControl(startPos: Vector3, direction: Vector3): Vector3 =
    {
        startPos.add(direction.multiply(controlPointRadius))
    }

    def getBezierCurve(stateTransition: StateTransition): BezierCurve =
    {
        val startPos: Vector3 = getStartOrEnd(stateTransition.from.position, stateTransition.fromDirection)
        val endPos: Vector3 = getStartOrEnd(stateTransition.to.position, stateTransition.toDirection)

        val startControl: Vector3 = getStartOrEndControl(startPos, stateTransition.fromDirection)
        val endControl: Vector3 = getStartOrEndControl(endPos, stateTransition.toDirection)

        return new BezierCurve(startPos, startControl, endPos, endControl)
    }

    def getNodeIntersectables(stateMachine: StateMachine): List[Intersectable] =
    {
        stateMachine.stateNodes.flatMap(node => getStateNodeIntersectables(node))
    }

    def getStateNodeIntersectables(stateNode: StateNode): List[Intersectable] =
    {
        return List[Intersectable](new CirclePlaneIntersectable(stateNode.position, new Vector3(0, 1, 0), new Vector3(1, 0, 0), nodeRadius, stateNode))
    }

    def getTransitionsIntersectables(stateMachine: StateMachine): List[Intersectable] =
    {
        return stateMachine.stateNodes.flatMap(node => node.transitions.flatMap(pair => pair._2.flatMap(transition => getTransitionIntersectables(pair._1, transition))))
    }

    def getTransitionIntersectables(transitionOn: String, transition: StateTransition): List[Intersectable] =
    {
        return List[Intersectable](new BezierIntersectable(getBezierCurve(transition), 0.4, 20, (transitionOn, transition)))
    }

    def getIntersectables(stateMachine: StateMachine): List[Intersectable] =
    {
        if(stateMachine == null) return List()
        val nodeIntersectables: List[Intersectable] = getNodeIntersectables(stateMachine)
        val transitionIntersectables: List[Intersectable] = getTransitionsIntersectables(stateMachine)

        return nodeIntersectables ++ transitionIntersectables
    }


}
