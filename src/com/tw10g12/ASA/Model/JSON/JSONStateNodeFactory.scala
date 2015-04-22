package com.tw10g12.ASA.Model.JSON

import com.tw10g12.ASA.Model.StateMachine.GlueState.GlueState
import com.tw10g12.ASA.Model.StateMachine.GlueState.GlueState
import com.tw10g12.ASA.Model.StateMachine.{StateTransition, GlueState, StateNode}
import com.tw10g12.ASA.Util
import org.json.{JSONArray, JSONObject}

/**
 * Created by Tom on 19/04/2015.
 */
object JSONStateNodeFactory
{
    def createStateNode(serialized: JSONObject): StateNode =
    {
        val actionMap = serialized.getJSONObject("actions")
        val actions: Map[Int, GlueState] = actionMap.keySet().toArray.map(orientation => (orientation.asInstanceOf[String].toInt -> GlueState.apply(actionMap.getInt(orientation.asInstanceOf[String])))).toMap
        val newNode = new StateNode(actions, Util.IOUtil.JSONToVector3(serialized.getJSONObject("position")), serialized.getString("label"))
        return newNode
    }

    def updateTransitions(jsonNodes: Array[JSONObject], stateNodes: List[StateNode]): Unit =
    {
        (0 until jsonNodes.size).map(index => updateNodeTransitions(jsonNodes(index), stateNodes(index), stateNodes))
    }

    def updateNodeTransitions(jsonNode: JSONObject, stateNode: StateNode, stateNodes: List[StateNode]): Unit =
    {
        val transitionMap = jsonNode.getJSONObject("transitions")
        val transitions: Map[String, List[StateTransition]] = transitionMap.keySet().toArray.map(transitionOn => (transitionOn.asInstanceOf[String] -> Util.IOUtil.JSONArrayToArray[JSONObject](transitionMap.getJSONArray(transitionOn.asInstanceOf[String])).map(jsonTransition => JSONStateTransitionFactory.createStateTransition(jsonTransition, stateNodes)).toList)).toMap
        stateNode.setTransitions(transitions)
    }
}
