package com.test.tw10g12.ASA.StateMachine

import com.test.tw10g12.Test.UnitSpec
import com.tw10g12.ASA.Model.StateMachine.{GlueState, StateTransition, StateNode, StateMachine}
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

        val nextState = stateMachine.nextState("S+")

        nextState._1.currentNode should be (nodes(2))
    }

    "A State Machine" should "correctly notify when a glue is disabled" in
    {
        val nodes: List[StateNode] = List(new StateNode(Map(), new Vector3(), "N1"), new StateNode(Map(), new Vector3(5, 0, 0), "N2") , new StateNode(Map(0 -> GlueState.Disabled, 1 -> GlueState.Active, 2 -> GlueState.Disabled), new Vector3(10, 0, 0), "N3"))
        nodes(0).setTransitions(Map("" -> List(new StateTransition(nodes(0), nodes(1), 1.0, new Vector3(), new Vector3())), "S+" -> List(new StateTransition(nodes(0), nodes(2), 1.0, new Vector3(), new Vector3()))))
        val stateMachine = new StateMachine(nodes(0), Map(), new Random(), nodes)

        val nextState = stateMachine.nextState("S+")

        nextState._1.currentNode should be (nodes(2))
        nextState._2 should contain only (0, 2)
    }

    "A State Machine" should "correctly notify when a glue is activated" in
    {
        val nodes: List[StateNode] = List(new StateNode(Map(0 -> GlueState.Disabled, 1 -> GlueState.Inert, 0 -> GlueState.Disabled), new Vector3(), "N1"), new StateNode(Map(), new Vector3(5, 0, 0), "N2") , new StateNode(Map(0 -> GlueState.Disabled, 1 -> GlueState.Active, 2 -> GlueState.Disabled), new Vector3(10, 0, 0), "N3"))
        nodes(0).setTransitions(Map("" -> List(new StateTransition(nodes(0), nodes(1), 1.0, new Vector3(), new Vector3())), "S+" -> List(new StateTransition(nodes(0), nodes(2), 1.0, new Vector3(), new Vector3()))))
        val stateMachine = new StateMachine(nodes(0), Map(0 -> GlueState.Disabled, 1 -> GlueState.Inert, 0 -> GlueState.Disabled), new Random(), nodes)

        val nextState = stateMachine.nextState("S+")

        nextState._1.currentNode should be (nodes(2))
        nextState._2 should contain only (1)
    }

    "A State Machine" should "correctly update the current glue state" in
    {
        val nodes: List[StateNode] = List(
            new StateNode(Map(0 -> GlueState.Disabled, 1 -> GlueState.Inert, 2 -> GlueState.Active), new Vector3(), "N1"),
            new StateNode(Map(1 -> GlueState.Active), new Vector3(5, 0, 0), "Active to Inert") ,
            new StateNode(Map(1 -> GlueState.Disabled), new Vector3(5, 0, 0), "Disable to Inert") ,
            new StateNode(Map(0 -> GlueState.Active), new Vector3(5, 0, 0), "Actived to Disabled") ,
            new StateNode(Map(0 -> GlueState.Inert), new Vector3(5, 0, 0), "Inert to Disabled") ,
            new StateNode(Map(2 -> GlueState.Inert), new Vector3(5, 0, 0), "Inert to Active") ,
            new StateNode(Map(2 -> GlueState.Disabled), new Vector3(5, 0, 0), "Disable to Active")
            )
        nodes(0).setTransitions(
            Map("A" -> List(new StateTransition(nodes(0), nodes(1), 1.0, new Vector3(), new Vector3())),
                "B" -> List(new StateTransition(nodes(0), nodes(2), 1.0, new Vector3(), new Vector3())),
                "C" -> List(new StateTransition(nodes(0), nodes(3), 1.0, new Vector3(), new Vector3())),
                "D" -> List(new StateTransition(nodes(0), nodes(4), 1.0, new Vector3(), new Vector3())),
                "E" -> List(new StateTransition(nodes(0), nodes(5), 1.0, new Vector3(), new Vector3())),
                "F" -> List(new StateTransition(nodes(0), nodes(6), 1.0, new Vector3(), new Vector3()))
            ))
        val stateMachine = new StateMachine(nodes(0), Map(0 -> GlueState.Disabled, 1 -> GlueState.Inert, 0 -> GlueState.Disabled), new Random(), nodes)

        val inputs = List("A", "B", "C", "D", "E", "F")
        val outputs = List(1 -> GlueState.Active,
                            1 -> GlueState.Disabled,
                            0 -> GlueState.Disabled,
                            0 -> GlueState.Disabled,
                            2 -> GlueState.Active,
                            2 -> GlueState.Disabled)
        val nextStates = inputs.map(input => stateMachine.nextState(input))

        (0 until nextStates.size).map(index =>
        {
            val nextState = nextStates(index)

            nextState._1.currentNode should be (nodes(index + 1))
            nextState._1.currentGlueStates should contain (outputs(index))
        })
    }
}
