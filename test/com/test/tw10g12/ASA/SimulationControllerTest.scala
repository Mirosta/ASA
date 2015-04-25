package com.test.tw10g12.ASA

import com.test.tw10g12.Test.UnitSpec
import com.tw10g12.ASA.Controller.SimulationController
import com.tw10g12.ASA.Model.ATAM.{ATAMGlue, ATAMTile}
import com.tw10g12.ASA.Model.Simulation
import com.tw10g12.Draw.Engine.Colour
import com.tw10g12.Maths.Vector3

import scala.util.Random

/**
 * Created by Tom on 25/04/2015.
 */
class SimulationControllerTest extends UnitSpec
{
    "A SimulationController" should "be able to pause without loading the queued simulation" in
    {
        val seedTile = new ATAMTile(Vector(new ATAMGlue("N", 2), new ATAMGlue("E", 2), new ATAMGlue("S", 2), new ATAMGlue("W", 2)), Vector(Colour.Black), new Vector3(0,0,0), -1)
        val tileTypes = Vector(new ATAMTile(Vector(new ATAMGlue("N", 2), null, new ATAMGlue("N", 2)), Vector(Colour.Red), new Vector3(0,0,0), 0), new ATAMTile(Vector(new ATAMGlue("S", 2)), Vector(Colour.Black), new Vector3(), 1))

        val originalRnd = new Random()
        val newRnd = new Random()

        val startingSimulation = new Simulation(seedTile, tileTypes, originalRnd)
        val controller: SimulationController = new SimulationController(startingSimulation)

        controller.beginSimulation()
        controller.setSimulation(new Simulation(seedTile, tileTypes, newRnd))
        Thread.sleep(500)

        controller.pauseSimulation()
        val rndField = controller.simulation.getClass.getDeclaredField("rnd")
        rndField.setAccessible(true)
        rndField.get(controller.simulation) should be (originalRnd)

        controller.beginSimulation()
        Thread.sleep(500)

        rndField.get(controller.simulation) should be (originalRnd)

        controller.stopSimulation()
    }

    "A SimulationController" should "be able to stop and load the queued simulation" in
    {
        val seedTile = new ATAMTile(Vector(new ATAMGlue("N", 2), new ATAMGlue("E", 2), new ATAMGlue("S", 2), new ATAMGlue("W", 2)), Vector(Colour.Black), new Vector3(0,0,0), -1)
        val tileTypes = Vector(new ATAMTile(Vector(new ATAMGlue("N", 2), null, new ATAMGlue("N", 2)), Vector(Colour.Red), new Vector3(0,0,0), 0), new ATAMTile(Vector(new ATAMGlue("S", 2)), Vector(Colour.Black), new Vector3(), 1))

        val originalRnd = new Random()
        val newRnd = new Random()

        val startingSimulation = new Simulation(seedTile, tileTypes, originalRnd)
        val controller: SimulationController = new SimulationController(startingSimulation)

        controller.beginSimulation()
        controller.setSimulation(new Simulation(seedTile, tileTypes, newRnd))
        Thread.sleep(500)

        controller.stopSimulation()
        val rndField = controller.simulation.getClass.getDeclaredField("rnd")
        rndField.setAccessible(true)
        rndField.get(controller.simulation) should be (newRnd)

        controller.beginSimulation()
        Thread.sleep(500)

        rndField.get(controller.simulation) should be (newRnd)

        controller.stopSimulation()
    }
}
