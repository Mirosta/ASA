package com.test.tw10g12.ASA.JSON

import com.test.tw10g12
import com.test.tw10g12.Draw.matchColour
import com.test.tw10g12.Maths
import com.test.tw10g12.Maths.matchVector
import com.test.tw10g12.Test.UnitSpec
import com.tw10g12.ASA.Model.ATAM.{ATAMGlue, ATAMTile}
import com.tw10g12.ASA.Model.JSON._
import com.tw10g12.ASA.Model.SMTAM.{SMTAMGlue, SMTAMTile}
import com.tw10g12.ASA.Model.StateMachine.{GlueState, StateMachine, StateNode, StateTransition}
import com.tw10g12.ASA.Model._
import com.tw10g12.ASA.Util
import com.tw10g12.Draw.Engine.Colour
import com.tw10g12.Maths.Vector3
import org.json.JSONObject

import scala.util.Random

/**
 * Created by Tom on 25/04/2015.
 */
class JSONTest extends UnitSpec
{
    "A Glue" should "correctly convert to and from JSON" in
    {
        val atamGlue = new ATAMGlue("A", 1, 2, null, false)
        val smtamGlue = new SMTAMGlue("B", 2, 0, null, true)

        val outputATAMGlue = JSONGlueFactory.createGlue(atamGlue.toJSON(new JSONObject()))
        val outputSMTAMGlue = JSONGlueFactory.createGlue(smtamGlue.toJSON(new JSONObject()))

        atamGlue.label should be (outputATAMGlue.label)
        atamGlue.strength should be (outputATAMGlue.strength)
        atamGlue.orientation should be (outputATAMGlue.orientation)
        atamGlue.isBound should be (outputATAMGlue.isBound)
    }

    "A Tile" should "correctly convert to and from JSON" in
    {
        val atamTile = new ATAMTile(Vector(null, null, null, null), Vector(Colour.White), new Vector3(1,2,3), 12)
        val smtamTile = new SMTAMTile(Vector(null, null, null, null), Vector(new Colour(0.1f, 0.2f, 0.3f)), new Vector3(-3,-2,-1), -1)

        val outputATAMTile = JSONTileFactory.createTile(atamTile.toJSON(new JSONObject()))
        val outputSMTAMTile = JSONTileFactory.createTile(smtamTile.toJSON(new JSONObject()))

        atamTile.getColour should matchColour(outputATAMTile.getColour)
        atamTile.glues.map(glue => glue != null).size should be (outputATAMTile.glues.map(glue => glue != null).size)
        atamTile.glues.map(glue => glue == null).size should be (outputATAMTile.glues.map(glue => glue == null).size)
        atamTile.getPosition should be (outputATAMTile.getPosition)
        atamTile.typeID should be (outputATAMTile.typeID)

        smtamTile.getColour should matchColour(outputSMTAMTile.getColour)
        smtamTile.glues.map(glue => glue != null).size should be (outputSMTAMTile.glues.map(glue => glue != null).size)
        smtamTile.glues.map(glue => glue == null).size should be (outputSMTAMTile.glues.map(glue => glue == null).size)
        smtamTile.getPosition should be (outputSMTAMTile.getPosition)
        smtamTile.typeID should be (outputSMTAMTile.typeID)
    }

    "A State Transition" should "correctly convert to and from JSON" in
    {
        val nodes: List[StateNode] = List(new StateNode(Map(), new Vector3(1,2,3), "N1"), new StateNode(Map(), new Vector3(1,2,3), "N2"))

        val stateTransition: StateTransition = new StateTransition(nodes.head, nodes(1), 0.534, new Vector3(4.1,8.2,10.9), new Vector3(-1.001, -1.221, 0))
        val outputStateTransition = JSONStateTransitionFactory.createStateTransition(stateTransition.toJSON(new JSONObject(), nodes), nodes)

        outputStateTransition.from should be (stateTransition.from)
        outputStateTransition.to should be (stateTransition.to)
        outputStateTransition.fromDirection should be (stateTransition.fromDirection)
        outputStateTransition.toDirection should be (stateTransition.toDirection)
        tw10g12.Maths.inTolerance(outputStateTransition.probability, stateTransition.probability, 0.00001) should be (true)
    }

    "A State Node" should "correctly convert to and from JSON" in
    {
        val nodes: List[StateNode] = List(new StateNode(Map(0 -> GlueState.Active, 1 -> GlueState.Inert, 2 -> GlueState.Active, 3 -> GlueState.Disabled), new Vector3(-0.1, 2.32, 11), "Long Test"))
        nodes(0).setTransitions(Map("" -> List(new StateTransition(nodes(0), nodes(0), 0.1, new Vector3, new Vector3), new StateTransition(nodes(0), nodes(0), 0.1, new Vector3, new Vector3)), "S+" -> List(new StateTransition(nodes(0), nodes(0), 0.1, new Vector3, new Vector3))))
        val stateNode: StateNode = nodes(0)

        val jsonStateNodes = Array(stateNode.toJSON(new JSONObject(), nodes))
        val outputStateNode: StateNode = JSONStateNodeFactory.createStateNode(jsonStateNodes(0))
        JSONStateNodeFactory.updateTransitions(jsonStateNodes, List(outputStateNode))

        outputStateNode.position should be (stateNode.position)
        outputStateNode.label should be (stateNode.label)

        outputStateNode.actions.size should be(stateNode.actions.size)
        outputStateNode.actions.exists(pair => !stateNode.actions.contains(pair._1) || stateNode.actions(pair._1) != pair._2) should be (false)
        outputStateNode.transitions.size should be (stateNode.transitions.size)
        outputStateNode.transitions.exists(pair => !stateNode.transitions.contains(pair._1) || stateNode.transitions(pair._1).size != pair._2.size) should be (false)

    }

    "A State Machine" should "correctly convert to and from JSON" in
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

        val nextNum = new Random(1234).nextInt()
        val stateMachine = new StateMachine(nodes(0), Map(0 -> GlueState.Disabled, 1 -> GlueState.Inert, 2 -> GlueState.Active), new Random(1234), nodes)

        val outputStateMachine: StateMachine = JSONStateMachineFactory.createStateMachine(stateMachine.toJSON(new JSONObject()))

        outputStateMachine.currentNode should be (outputStateMachine.stateNodes(0))
        outputStateMachine.currentGlueStates.size should be(stateMachine.currentGlueStates.size)
        outputStateMachine.currentGlueStates.exists(pair => !stateMachine.currentGlueStates.contains(pair._1) || stateMachine.currentGlueStates(pair._1) != pair._2) should be (false)
        outputStateMachine.stateNodes.size should be (stateMachine.stateNodes.size)

        val rndField = outputStateMachine.getClass().getDeclaredField("rnd")
        rndField.setAccessible(true)

        val rnd: Random = rndField.get(outputStateMachine).asInstanceOf[Random]
        rnd.nextInt() should be (nextNum)
    }

    "A Tile Set" should "correctly convert to and from JSON" in
    {
        val seedTile = new ATAMTile(Vector(new ATAMGlue("N", 2), new ATAMGlue("E", 2), new ATAMGlue("S", 2), new ATAMGlue("W", 2)), Vector(Colour.Black), new Vector3(0,0,0), -1)
        val tileTypes = List(new ATAMTile(Vector(new ATAMGlue("N", 2), null, new ATAMGlue("N", 2)), Vector(Colour.Red), new Vector3(0,0,0), 0), new ATAMTile(Vector(new ATAMGlue("S", 2)), Vector(Colour.Black), new Vector3(), 1))

        val tileSet: (Tile, List[Tile]) = (seedTile, tileTypes)

        val nodes: List[StateNode] = List(new StateNode(Map(0 -> GlueState.Disabled, 1 -> GlueState.Inert, 0 -> GlueState.Disabled), new Vector3(), "N1"), new StateNode(Map(), new Vector3(5, 0, 0), "N2") , new StateNode(Map(0 -> GlueState.Disabled, 1 -> GlueState.Active, 2 -> GlueState.Disabled), new Vector3(10, 0, 0), "N3"))
        nodes(0).setTransitions(Map("" -> List(new StateTransition(nodes(0), nodes(1), 1.0, new Vector3(), new Vector3())), "S+" -> List(new StateTransition(nodes(0), nodes(2), 1.0, new Vector3(), new Vector3()))))
        val stateMachine = new StateMachine(nodes(0), Map(0 -> GlueState.Disabled, 1 -> GlueState.Inert, 0 -> GlueState.Disabled), new Random(), nodes)

        val stateMachines: Map[Tile, StateMachine] = Map(tileTypes(0) -> stateMachine)

        val outputTileSet = Util.IOUtil.JSONtoTileset(Util.IOUtil.tilesetToJSON(tileSet, stateMachines))

        outputTileSet._1.typeID should be (tileSet._1.typeID)
        outputTileSet._2.size should be (tileSet._2.size)
        (0 until outputTileSet._2.size).exists(index => outputTileSet._2(index).typeID != tileSet._2(index).typeID) should be (false)
        outputTileSet._3.size should be (stateMachines.size)
        outputTileSet._3.keySet.exists(key => !stateMachines.contains(key)) should be (false)
    }

    "A Vector3" should "correctly convert to and from JSON" in
    {
        val vec = new Vector3(1, -1.5, 0.1212)

        val outputVec = Util.IOUtil.JSONToVector3(Util.IOUtil.vector3ToJSON(vec))

        outputVec should matchVector(1, -1.5, 0.1212, 0.0000001)
    }

    "A Colour" should "correctly convert to and from JSON" in
    {
        val col = new Colour(0.1f, 0.2f, 0.3f, 0.5f)

        val outputCol = Util.IOUtil.JSONToColour(Util.IOUtil.colourToJSON(col))

        outputCol should matchColour(col)
    }

    "An ATAM Simulation" should "correctly convert to and from JSON" in
    {

        val seedTile = new ATAMTile(Vector(new ATAMGlue("N", 2), new ATAMGlue("E", 2), new ATAMGlue("S", 2), new ATAMGlue("W", 2)), Vector(Colour.Black), new Vector3(0,0,0), -1)
        val tileTypes = List(new ATAMTile(Vector(new ATAMGlue("N", 2), null, new ATAMGlue("N", 2)), Vector(Colour.Red), new Vector3(0,0,0), 0), new ATAMTile(Vector(new ATAMGlue("S", 2)), Vector(Colour.Black), new Vector3(), 1))

        val rand = new Random()
        val simulation: Simulation = new Simulation(seedTile, tileTypes.toVector, rand)
        (0 until 100).map(_ => simulation.tick())

        val outputSimulation = JSONSimulationFactory.createSimulation(simulation.toJSON(new JSONObject()))
        val rndField = outputSimulation.getClass().getDeclaredField("rnd")
        rndField.setAccessible(true)

        val newRnd = rndField.get(outputSimulation).asInstanceOf[Random]

        newRnd.nextInt() should be (rand.nextInt())
        outputSimulation.getTileTypes()._1.typeID should be (simulation.getTileTypes()._1.typeID)
        outputSimulation.getTileTypes()._2.size should be (simulation.getTileTypes()._2.size)
        (0 until outputSimulation.getTileTypes()._2.size).exists(index => outputSimulation.getTileTypes()._2(index).typeID != simulation.getTileTypes()._2(index).typeID) should be (false)

        outputSimulation.state.tiles.size should be (simulation.state.tiles.size)
        outputSimulation.state.tiles.exists(pair => !simulation.state.tiles.contains(pair._1) || simulation.state.tiles(pair._1).typeID != pair._2.typeID) should be (false)
        outputSimulation.state.adjacencies.size should be (simulation.state.adjacencies.size)
        outputSimulation.state.adjacencies.exists(pair => !simulation.state.adjacencies.contains(pair._1) || simulation.state.adjacencies(pair._1).size != pair._2.size) should be (false)
        outputSimulation.state.stats.metrics.size should be (simulation.state.stats.metrics.size)
        outputSimulation.state.stats.metrics.exists(pair => !tw10g12.Maths.inTolerance(simulation.state.stats.getMetric(pair._1), pair._2, 0.000001)) should be (false)
    }

    "A KTAM Simulation" should "correctly convert to and from JSON" in
    {

        val seedTile = new ATAMTile(Vector(new ATAMGlue("N", 2), new ATAMGlue("E", 2), new ATAMGlue("S", 2), new ATAMGlue("W", 2)), Vector(Colour.Black), new Vector3(0,0,0), -1)
        val tileTypes = List(new ATAMTile(Vector(new ATAMGlue("N", 2), null, new ATAMGlue("N", 2)), Vector(Colour.Red), new Vector3(0,0,0), 0), new ATAMTile(Vector(new ATAMGlue("S", 2)), Vector(Colour.Black), new Vector3(), 1))

        val rand = new Random()
        val simulation: KTAMSimulation = new KTAMSimulation(seedTile, tileTypes.toVector, 1, 1, rand)
        (0 until 100).map(_ => simulation.tick())

        val outputSimulation = JSONSimulationFactory.createSimulation(simulation.toJSON(new JSONObject()))
        val rndField = outputSimulation.getClass().getDeclaredField("rnd")
        rndField.setAccessible(true)

        val newRnd = rndField.get(outputSimulation).asInstanceOf[Random]

        newRnd.nextInt() should be (rand.nextInt())

        outputSimulation.isInstanceOf[KTAMSimulation] should be (true)

        val outputKTAMSimulation = outputSimulation.asInstanceOf[KTAMSimulation]
        outputSimulation.getTileTypes()._1.typeID should be (simulation.getTileTypes()._1.typeID)
        outputSimulation.getTileTypes()._2.size should be (simulation.getTileTypes()._2.size)
        (0 until outputSimulation.getTileTypes()._2.size).exists(index => outputSimulation.getTileTypes()._2(index).typeID != simulation.getTileTypes()._2(index).typeID) should be (false)

        outputSimulation.state.tiles.size should be (simulation.state.tiles.size)
        outputSimulation.state.tiles.exists(pair => !simulation.state.tiles.contains(pair._1) || simulation.state.tiles(pair._1).typeID != pair._2.typeID) should be (false)
        outputSimulation.state.adjacencies.size should be (simulation.state.adjacencies.size)
        outputSimulation.state.adjacencies.exists(pair => !simulation.state.adjacencies.contains(pair._1) || simulation.state.adjacencies(pair._1).size != pair._2.size) should be (false)
        outputSimulation.state.stats.metrics.size should be (simulation.state.stats.metrics.size)
        outputSimulation.state.stats.metrics.exists(pair => !tw10g12.Maths.inTolerance(simulation.state.stats.getMetric(pair._1), pair._2, 0.000001)) should be (false)

        Maths.inTolerance(outputKTAMSimulation.backwardConstant, simulation.backwardConstant, 0.00001) should be (true)
        Maths.inTolerance(outputKTAMSimulation.forwardConstant, simulation.forwardConstant, 0.00001) should be (true)

        val ktamState = simulation.state.asInstanceOf[KTAMSimulationState]
        val outputKtamState: KTAMSimulationState = outputKTAMSimulation.state.asInstanceOf[KTAMSimulationState]

        outputKtamState.removeTileProbabilities._2.size should be (ktamState.removeTileProbabilities._2.size)
        outputKtamState.removeTileProbabilities._2.exists(pair => !ktamState.removeTileProbabilities._2.contains(pair._1) || Maths.inTolerance(ktamState.removeTileProbabilities._2(pair._1), pair._2, 0.0001)) should be (false)
    }

    "A SMTAM Simulation" should "correctly convert to and from JSON" in
    {

        val seedTile = new ATAMTile(Vector(new ATAMGlue("N", 2), new ATAMGlue("E", 2), new ATAMGlue("S", 2), new ATAMGlue("W", 2)), Vector(Colour.Black), new Vector3(0,0,0), -1)
        val tileTypes = List(new ATAMTile(Vector(new ATAMGlue("N", 2), null, new ATAMGlue("N", 2)), Vector(Colour.Red), new Vector3(0,0,0), 0), new ATAMTile(Vector(new ATAMGlue("S", 2)), Vector(Colour.Black), new Vector3(), 1))

        val nodes: List[StateNode] = List(new StateNode(Map(0 -> GlueState.Disabled, 1 -> GlueState.Inert, 0 -> GlueState.Disabled), new Vector3(), "N1"), new StateNode(Map(), new Vector3(5, 0, 0), "N2") , new StateNode(Map(0 -> GlueState.Disabled, 1 -> GlueState.Active, 2 -> GlueState.Disabled), new Vector3(10, 0, 0), "N3"))
        nodes(0).setTransitions(Map("" -> List(new StateTransition(nodes(0), nodes(1), 1.0, new Vector3(), new Vector3())), "S+" -> List(new StateTransition(nodes(0), nodes(2), 1.0, new Vector3(), new Vector3()))))
        nodes.map(node => if(node.transitions == null) node.setTransitions(Map()))
        val stateMachine = new StateMachine(nodes(0), Map(0 -> GlueState.Disabled, 1 -> GlueState.Inert, 0 -> GlueState.Disabled), new Random(), nodes)


        val stateMachines: Map[Tile, StateMachine] = Map(tileTypes(0) -> stateMachine)

        val rand = new Random()
        val simulation: SMTAMSimulation = new SMTAMSimulation(seedTile, tileTypes.toVector, stateMachines, false, rand)
        (0 until 100).map(_ => simulation.tick())

        val outputSimulation = JSONSimulationFactory.createSimulation(simulation.toJSON(new JSONObject()))
        val rndField = outputSimulation.getClass().getDeclaredField("rnd")
        rndField.setAccessible(true)

        val newRnd = rndField.get(outputSimulation).asInstanceOf[Random]

        newRnd.nextInt() should be (rand.nextInt())
        outputSimulation.getTileTypes()._1.typeID should be (simulation.getTileTypes()._1.typeID)
        outputSimulation.getTileTypes()._2.size should be (simulation.getTileTypes()._2.size)
        (0 until outputSimulation.getTileTypes()._2.size).exists(index => outputSimulation.getTileTypes()._2(index).typeID != simulation.getTileTypes()._2(index).typeID) should be (false)

        outputSimulation.state.tiles.size should be (simulation.state.tiles.size)
        outputSimulation.state.tiles.exists(pair => !simulation.state.tiles.contains(pair._1) || simulation.state.tiles(pair._1).typeID != pair._2.typeID) should be (false)
        outputSimulation.state.adjacencies.size should be (simulation.state.adjacencies.size)
        outputSimulation.state.adjacencies.exists(pair => !simulation.state.adjacencies.contains(pair._1) || simulation.state.adjacencies(pair._1).size != pair._2.size) should be (false)
        outputSimulation.state.stats.metrics.size should be (simulation.state.stats.metrics.size)
        outputSimulation.state.stats.metrics.exists(pair => !tw10g12.Maths.inTolerance(simulation.state.stats.getMetric(pair._1), pair._2, 0.000001)) should be (false)

        outputSimulation.isInstanceOf[SMTAMSimulation] should be (true)

        val outputSMTAMSimulation = outputSimulation.asInstanceOf[SMTAMSimulation]
        val smtamState = simulation.state.asInstanceOf[SMTAMSimulationState]
        val outputSmtamState: SMTAMSimulationState = outputSMTAMSimulation.state.asInstanceOf[SMTAMSimulationState]

        outputSMTAMSimulation.checkConnected should be (simulation.checkConnected)

        val tileStateMachinesField = outputSMTAMSimulation.getClass().getDeclaredField("tileStateMachines")
        tileStateMachinesField.setAccessible(true)

        val outputTileStateMachines = tileStateMachinesField.get(outputSMTAMSimulation).asInstanceOf[Map[Tile, StateMachine]]
        outputTileStateMachines.size should be (stateMachines.size)
        outputTileStateMachines.exists(pair => !stateMachines.contains(pair._1)) should be (false)

        outputSmtamState.stateMachineStates.size should be (smtamState.stateMachineStates.size)
        outputSmtamState.stateMachineStates.keySet should contain only (smtamState.stateMachineStates.keySet)
    }
}
