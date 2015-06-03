package com.tw10g12.ASA.Model.StateMachine

import com.tw10g12.ASA.Util
import com.tw10g12.IO.{JSONSerializable}
import com.tw10g12.Maths.Vector3
import org.json.JSONObject

/**
 * Created by Tom on 25/03/2015.
 */
class StateTransition(val from: StateNode, val to:StateNode, val probability: Double, val fromDirection: Vector3, val toDirection: Vector3) extends JSONSerializable
{

    def setFrom(newFrom: StateNode): StateTransition =
    {
        return new StateTransition(newFrom, to, probability, fromDirection, toDirection)
    }

    def setTo(newTo: StateNode): StateTransition =
    {
        return new StateTransition(from, newTo, probability, fromDirection, toDirection)
    }

    def setProbability(probability: Double): StateTransition =
    {
        return new StateTransition(from, to, probability, fromDirection.multiply(1), toDirection.multiply(1))
    }


    override def toJSON(obj: JSONObject): JSONObject =
    {
        toJSON(obj, List())
    }

    def toJSON(obj: JSONObject, nodes: List[StateNode]): JSONObject =
    {
        val fromIndex = nodes.indexOf(from)
        val toIndex = nodes.indexOf(to)
        if(fromIndex < 0) throw new Exception("Couldn't find From Node")
        if(toIndex < 0) throw new Exception("Couldn't find To Node")

        obj.put("from", fromIndex)
        obj.put("to", toIndex)
        obj.put("probability", probability)
        obj.put("fromDirection", Util.IOUtil.vector3ToJSON(fromDirection))
        obj.put("toDirection", Util.IOUtil.vector3ToJSON(toDirection))
        return obj
    }
}
