package com.tw10g12.ASA.Model.StateMachine

import com.tw10g12.ASA.Model.StateMachine.GlueState.GlueState
import com.tw10g12.ASA.Util
import com.tw10g12.IO.JSONSerializable
import com.tw10g12.Maths.Vector3
import org.json.{JSONArray, JSONObject}

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

    def getDelta(from: GlueState, to: GlueState): Int =
    {
        if(from == Disabled) return 0
        if(to == Active) return 1
        if(to == Disabled) return -1

        return 0
    }
}

class StateNode(val actions: Map[Int, GlueState], val position: Vector3, val label: String) extends JSONSerializable
{

    lazy val gluesText = getGluesText()

    private def getGluesText(): String =
    {
        val glueStates = (0 to 3).map(orientation => (orientation, getGlueState(orientation))).filter(pair => pair._2 != null)
        return glueStates.foldLeft("", "")((outStrs, pair) => (outStrs._1 + outStrs._2 + Util.orientationToHeading(pair._1) + getGlueStateString(pair._2), ", "))._1
    }

    def getGlueStateString(glueState: GlueState): String =
    {
        glueState match
        {
            case GlueState.Active => return "+"
            case GlueState.Inert => return "="
            case GlueState.Disabled => return "-"
        }
        ""
    }

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

    def updateGlueStates(currentGlueStates: Map[Int, GlueState]): (Map[Int, GlueState], Set[Int]) =
    {
        val updatedStates = actions.foldLeft(Map[Int, GlueState](), Set[Int]())((curSeqs, pair) =>
        {
            val currentGlueState = if(currentGlueStates.contains(pair._1)) currentGlueStates(pair._1) else GlueState.Active
            (curSeqs._1 + (pair._1 -> GlueState.updateGlueState(currentGlueState, pair._2)), curSeqs._2 + GlueState.getDelta(currentGlueState, pair._2))
        })
        return ((currentGlueStates ++ updatedStates._1), updatedStates._2)
    }

    def setLabel(newLabel: String): StateNode =
    {
        val newNode = new StateNode(actions, position.multiply(1), newLabel)
        newNode.setTransitions(transitions.map(pair => (pair._1, pair._2.map(transition => transition.setFrom(newNode)))))
        return newNode
    }

    def updateTransitions(oldNode: StateNode, updatedNode: StateNode): StateNode =
    {
        transitions = if(transitions == null) null else transitions.map(pair => (pair._1, pair._2.map(transition => if(transition.to == oldNode) transition.setTo(updatedNode) else transition)))
        return this
    }

    def removeGlueState(orientation: Int): StateNode =
    {
        val newNode = new StateNode(actions - orientation, position, label)
        newNode.setTransitions(transitions)
        return newNode
    }

    def addGlueState(glue: Int, state: GlueState): StateNode =
    {
        val newNode = new StateNode(actions + (glue -> state), position, label)
        newNode.setTransitions(transitions)
        return newNode
    }

    override def toJSON(obj: JSONObject): JSONObject =
    {
        return toJSON(obj, List())
    }

    def toJSON(obj: JSONObject, stateNodes: List[StateNode]): JSONObject =
    {
        if(stateNodes.isEmpty) return obj
        val actionsMapObj = new JSONObject()
        actions.map(pair => actionsMapObj.put(pair._1.toString, pair._2.id))

        val transitionsMap = new JSONObject()
        transitions.map(pair => transitionsMap.put(pair._1, new JSONArray(pair._2.map(transition => transition.toJSON(new JSONObject(), stateNodes)).toArray)))

        obj.put("actions", actionsMapObj)
        obj.put("position", Util.IOUtil.vector3ToJSON(position))
        obj.put("label", label)
        obj.put("transitions", transitionsMap)

        return obj
    }
}
