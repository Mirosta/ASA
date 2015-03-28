package com.tw10g12.ASA.Model.StateMachine

import com.tw10g12.ASA.Model.StateMachine.GlueState.GlueState

import scala.util.Random

/**
 * Created by Tom on 25/03/2015.
 */
class StateMachine(val currentNode: StateNode, currentGlueStates: Map[Int, GlueState], rnd: Random, val stateNodes: List[StateNode])
{

    def nextState(input: String): StateMachine =
    {
        val possibleTransitions: List[StateTransition] = currentNode.getTransitionsForInput(input)
        val nextTransition: StateTransition = pickPossibleTransition(possibleTransitions, rnd)
        if(nextTransition == null) return this

        val nextGlueStates: Map[Int, GlueState] = nextTransition.to.updateGlueStates(currentGlueStates)

        return new StateMachine(nextTransition.to, nextGlueStates, rnd, stateNodes)
    }

    def pickPossibleTransition(possibleTransitions: List[StateTransition], rnd: Random): StateTransition =
    {
        if(possibleTransitions.size <= 0) return null
        val probabilityTotal = possibleTransitions.foldLeft(0.0)((total, transition) => total + transition.probability)
        if(probabilityTotal == 0) return null
        var randNum = rnd.nextDouble() * probabilityTotal
        val possibleTransition = possibleTransitions.foldLeft(null.asInstanceOf[StateTransition])((current, potential) =>
        {
            if(current != null) current else if(potential.probability > randNum) potential else randNum -= potential.probability; current
        })
        return possibleTransition
    }

}
