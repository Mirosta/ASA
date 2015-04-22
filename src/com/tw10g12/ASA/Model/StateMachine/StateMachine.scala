package com.tw10g12.ASA.Model.StateMachine

import com.tw10g12.ASA.Model.StateMachine.GlueState.GlueState
import com.tw10g12.IO.JSONSerializable
import org.json.{JSONArray, JSONObject}

import scala.util.Random

/**
 * Created by Tom on 25/03/2015.
 */
class StateMachine(val currentNode: StateNode, val currentGlueStates: Map[Int, GlueState], rnd: Random, val stateNodes: List[StateNode]) extends JSONSerializable
{

    def nextState(input: String): (StateMachine, Set[Int]) =
    {
        val possibleTransitions: List[StateTransition] = currentNode.getTransitionsForInput(input)
        val nextTransition: StateTransition = pickPossibleTransition(possibleTransitions, rnd)
        if(nextTransition == null) return (this, Set[Int](0))

        val nextGlueStatesAndActions: (Map[Int, GlueState], Set[Int]) = nextTransition.to.updateGlueStates(currentGlueStates)

        return (new StateMachine(nextTransition.to, nextGlueStatesAndActions._1, rnd, stateNodes), nextGlueStatesAndActions._2)
    }

    def pickPossibleTransition(possibleTransitions: List[StateTransition], rnd: Random): StateTransition =
    {
        if(possibleTransitions.size <= 0) return null
        val probabilityTotal = possibleTransitions.foldLeft(0.0)((total, transition) => total + transition.probability)
        if(probabilityTotal == 0) return null
        var randNum = rnd.nextDouble() * probabilityTotal
        val possibleTransition = possibleTransitions.foldLeft(null.asInstanceOf[StateTransition])((current, potential) =>
        {
            if(current != null) current
            else if(potential.probability > randNum) return potential
            else randNum -= potential.probability; current
        })
        return possibleTransition
    }

    def setCurrentNode(node: StateNode): StateMachine =
    {
        return new StateMachine(node, node.actions, rnd, stateNodes)
    }

    def addStateNode(newNode: StateNode): StateMachine =
    {
        val updatedList = newNode :: stateNodes
        return new StateMachine(if(updatedList.size == 1) newNode else currentNode, currentGlueStates, rnd, updatedList)
    }

    def updateStateNode(oldNode: StateNode, updatedNode: StateNode): StateMachine =
    {
        val newCurrentNode = if(oldNode == currentNode) updatedNode else currentNode.updateTransitions(oldNode, updatedNode)
        val removedList = stateNodes.filter(node => node != oldNode && node != currentNode)
        val updatedList = removedList.map(node => node.updateTransitions(oldNode, updatedNode)) ++ Set(newCurrentNode, updatedNode)
        return new StateMachine(newCurrentNode, currentGlueStates, rnd, updatedList)
    }

    def removeState(oldNode: StateNode): StateMachine =
    {
        val newStateNodes = stateNodes.filter(node => node != oldNode)
        val newCurrentNode = if(oldNode == currentNode) (if(newStateNodes.isEmpty) null else newStateNodes.head) else currentNode
        newStateNodes.map(node => node.transitions = node.transitions.map(pair => (pair._1, pair._2.filter(transition => transition.to != oldNode))).filter(pair => !pair._2.isEmpty))

        return new StateMachine(newCurrentNode, currentGlueStates, rnd, newStateNodes)
    }

    override def toJSON(obj: JSONObject): JSONObject =
    {
        val currentNodeIndex = if(currentNode == null) null else stateNodes.indexOf(currentNode)
        val currentGlueStatesObj = new JSONObject()
        currentGlueStates.map(pair => currentGlueStatesObj.put(pair._1.toString, pair._2.id))
        obj.put("currentNode", currentNodeIndex)
        obj.put("currentGlueStates", currentGlueStatesObj)
        obj.put("stateNodes", new JSONArray(stateNodes.map(stateNode => stateNode.toJSON(new JSONObject(), stateNodes)).toArray))

        return obj
    }
}
