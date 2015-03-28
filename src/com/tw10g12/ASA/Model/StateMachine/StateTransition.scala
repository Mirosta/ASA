package com.tw10g12.ASA.Model.StateMachine

import com.tw10g12.Maths.Vector3

/**
 * Created by Tom on 25/03/2015.
 */
class StateTransition(val from: StateNode, val to:StateNode, val probability: Double, val fromDirection: Vector3, val toDirection: Vector3)
{

}
