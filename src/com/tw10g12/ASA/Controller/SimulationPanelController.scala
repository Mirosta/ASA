package com.tw10g12.ASA.Controller

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.JButton

import com.tw10g12.ASA.GUI.SimulationPanel

/**
 * Created by Tom on 01/12/2014.
 */
class SimulationPanelController(simulation: SimulationController, simulationPanel: SimulationPanel)
{
    val startListener = new ActionListener()
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            e.getSource match
            {
                case button: JButton =>
                {
                    if(simulation.state == SimulationState.Paused || simulation.state == SimulationState.Stopped)
                    {
                        simulation.beginSimulation()
                        button.setText("Pause")
                        simulationPanel.stopButton.setEnabled(true)
                    }
                    else
                    {
                        simulation.pauseSimulation()
                        button.setText("Resume")
                    }
                }
            }
        }
    }
    val stopButtonListener = new ActionListener()
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            e.getSource match
            {
                case button: JButton =>
                {
                    if(simulation.state == SimulationState.Paused || simulation.state == SimulationState.Running)
                    {
                        simulation.stopSimulation()
                        button.setEnabled(false)
                        simulationPanel.startButton.setText("Start")
                    }
                }
            }

        }
    }
}
