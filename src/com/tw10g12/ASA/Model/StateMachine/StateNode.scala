package com.tw10g12.ASA.Model.StateMachine

import com.tw10g12.ASA.Model.StateMachine.GlueState.GlueState
import com.tw10g12.Maths.Vector3

/**
 * Created by Tom on 25/03/2015.
 */
object GlueState extends Enumeration
{
    type GlueState = Value
    val Disabled, Inert, Active = Value

    def updateGlueState(from: GlueState, to: GlueState): GlueState =
    {
        if(from == Disabled) return Disabled
        if(to == Inert) return from
        return to
    }
}

class StateNode(actions: Map[Int, GlueState], val position: Vector3, val label: String)
{

    def getTransitionsForInput(input: String): List[StateTransition] =
    {
        if(!transitions.contains(input)) return List[StateTransition]()
        return transitions(input)
    }

    var transitions: Map[String, List[StateTransition]] = null

    def setTransitions(transitions: Map[String, List[StateTransition]]): Unit =
    {
        this.transitions = transitions
    }

    def getGlueState(orientation: Int): GlueState =
    {
        if(!actions.contains(orientation)) return null
        else return actions(orientation)
    }

    def updateGlueStates(currentGlueStates: Map[Int, GlueState]): Map[Int, GlueState] =
    {
        val updatedStates = actions.map(pair => (pair._1, GlueState.updateGlueState(currentGlueStates(pair._1), pair._2)))
        return currentGlueStates ++ updatedStates
    }
}
