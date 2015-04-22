package com.tw10g12.ASA.Model.JSON

import com.tw10g12.ASA.Model.StateMachine.{StateTransition, StateNode}
import com.tw10g12.ASA.Util
import org.json.JSONObject

/**
 * Created by Tom on 19/04/2015.
 */
object JSONStateTransitionFactory
{
    def createStateTransition(serialized: JSONObject, stateNodes: List[StateNode]): StateTransition =
    {
        val from = stateNodes(serialized.getInt("from"))
        val to = stateNodes(serialized.getInt("to"))

        val newStateTransition = new StateTransition(from, to, serialized.getDouble("probability"), Util.IOUtil.JSONToVector3(serialized.getJSONObject("fromDirection")), Util.IOUtil.JSONToVector3(serialized.getJSONObject("toDirection")))
        return newStateTransition
    }
}
