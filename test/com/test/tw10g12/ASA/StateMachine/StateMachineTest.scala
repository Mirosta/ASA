package com.test.tw10g12.ASA.StateMachine

import com.test.tw10g12.Test.UnitSpec
import com.tw10g12.ASA.Model.StateMachine.{StateTransition, StateNode, StateMachine}
import com.tw10g12.Maths.Vector3

import scala.util.Random

/**
 * Created by Tom on 25/04/2015.
 */
class StateMachineTest extends UnitSpec
{
    "A State Machine" should "allow epsilon moves" in
    {
        val nodes: List[StateNode] = List(new StateNode(Map(), new Vector3(), "N1"), new StateNode(Map(), new Vector3(5, 0, 0), "N2") , new StateNode(Map(), new Vector3(10, 0, 0), "N3"))
        nodes(0).setTransitions(Map("" -> List(new StateTransition(nodes(0), nodes(1), 1.0, new Vector3(), new Vector3())), "S+" -> List(new StateTransition(nodes(0), nodes(2), 1.0, new Vector3(), new Vector3()))))
        val stateMachine = new StateMachine(nodes(0), Map(), new Random(), nodes)

        val epsilonMove = stateMachine.nextState("")

        epsilonMove._1.currentNode should be (nodes(1))
    }

    "A State Machine" should "allow moves for a non empty input" in
    {
        val nodes: List[StateNode] = List(new StateNode(Map(), new Vector3(), "N1"), new StateNode(Map(), new Vector3(5, 0, 0), "N2") , new StateNode(Map(), new Vector3(10, 0, 0), "N3"))
        nodes(0).setTransitions(Map("" -> List(new StateTransition(nodes(0), nodes(1), 1.0, new Vector3(), new Vector3())), "S+" -> List(new StateTransition(nodes(0), nodes(2), 1.0, new Vector3(), new Vector3()))))
        val stateMachine = new StateMachine(nodes(0), Map(), new Random(), nodes)

        val epsilonMove = stateMachine.nextState("S+")

        epsilonMove._1.currentNode should be (nodes(2))
    }
}
