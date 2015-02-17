package com.tw10g12.ASA.GUI

import javax.swing.{JButton, JPanel}

import com.tw10g12.ASA.Controller.{SimulationPanelController, SimulationController}
import com.tw10g12.ASA.Util

/**
 * Created by Tom on 01/12/2014.
 */
class SimulationPanel(simulation: SimulationController) extends JPanel
{
    val controller = new SimulationPanelController(simulation, this)
    var startButton: JButton = null
    var stopButton: JButton = null

    def setup(): Unit =
    {
        startButton = new JButton("Start")
        stopButton = new JButton("Stop")
        stopButton.setEnabled(false)

        startButton.addActionListener(controller.startListener)
        stopButton.addActionListener(controller.stopButtonListener)

        Util.addToGridBag(startButton, this, 1, 1, 0.0, 1.0)
        Util.addToGridBag(stopButton, this, 2, 1, 0.0, 1.0)
    }
}
