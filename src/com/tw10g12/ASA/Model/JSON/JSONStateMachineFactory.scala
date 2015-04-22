package com.tw10g12.ASA.Model.JSON

import java.util.Random

import com.tw10g12.ASA.Model.StateMachine.GlueState.GlueState
import com.tw10g12.ASA.Model.StateMachine.{GlueState, StateMachine, StateNode}
import com.tw10g12.ASA.Util
import org.json.JSONObject

/**
 * Created by Tom on 19/04/2015.
 */
object JSONStateMachineFactory
{
    def createStateMachine(serialized: JSONObject): StateMachine =
    {
        val jsonNodes = Util.IOUtil.JSONArrayToArray[JSONObject](serialized.getJSONArray("stateNodes"))
        val nodes: List[StateNode] = jsonNodes.map(jsonState => JSONStateNodeFactory.createStateNode(jsonState)).toList
        JSONStateNodeFactory.updateTransitions(jsonNodes.toArray, nodes)

        val current: StateNode = if(!serialized.has("currentNode") || serialized.get("currentNode") == null) null else nodes(serialized.getInt("currentNode"))
        val glueStatesMap = serialized.getJSONObject("currentGlueStates")
        val currentGlueStates: Map[Int, GlueState] = glueStatesMap.keySet().toArray.map(orientation => (orientation.asInstanceOf[String].toInt -> GlueState.apply(glueStatesMap.getInt(orientation.asInstanceOf[String])))).toMap
        val newStateMachine = new StateMachine(current, currentGlueStates, new Random(), nodes)
        return newStateMachine
    }
}
