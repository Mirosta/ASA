package com.tw10g12.ASA.Controller

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.JButton
import javax.swing.event.{ChangeEvent, ChangeListener, DocumentEvent, DocumentListener}

import com.tw10g12.ASA.GUI.SimulationPanel
import com.tw10g12.ASA.Model.{KTAMSimulation, Simulation, Tile}

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
                    if (simulation.state == SimulationState.Paused || simulation.state == SimulationState.Stopped)
                    {
                        val index = simulationPanel.simulationMode.getSelectedIndex
                        if (index > -1)
                        {
                            val item: String = simulationPanel.simulationMode.getItemAt(index)
                            simulation.setSimulation(getSimulation(item, simulation.getTileTypes()))
                        }
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
                    if (simulation.state == SimulationState.Paused || simulation.state == SimulationState.Running)
                    {
                        simulation.stopSimulation()
                        button.setEnabled(false)
                        simulationPanel.startButton.setText("Start")
                    }
                }
            }

        }
    }

    val modeSelectListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            val index = simulationPanel.simulationMode.getSelectedIndex
            if (index > -1)
            {
                val item: String = simulationPanel.simulationMode.getItemAt(index)
                simulationPanel.simulationModeOptions.show(simulationPanel.simulationModeOptionsPanel, item)
                simulationPanel.simulationModeOptionsPanel.repaint()
            }

        }
    }

    def getSimulation(simulationType: String, tileTypes: (Tile, Vector[Tile])): Simulation =
    {
        val functions: Map[String, Function[(Tile, Vector[Tile]), Simulation]] = Map[String, Function[(Tile, Vector[Tile]), Simulation]] ("ATAM" -> createATAM, "KTAM" -> createKTAM)
        return functions(simulationType)(tileTypes)
    }

    def createATAM(tileTypes: (Tile, Vector[Tile])): Simulation =
    {
        return new Simulation(tileTypes._1, tileTypes._2)
    }

    def createKTAM(tileTypes: (Tile, Vector[Tile])): Simulation =
    {
        val reverseConstant = if(simulationPanel.reverseConstantInput.isValid()) 1.0 / simulationPanel.reverseConstantInput.getText.toDouble else 0.0
        val forwardConstant = if(simulationPanel.forwardConstantInput.isValid()) 1.0 / simulationPanel.forwardConstantInput.getText.toDouble else 0.0
        return new KTAMSimulation(tileTypes._1, tileTypes._2, reverseConstant, forwardConstant)
    }

    val KTAMConstantsChanged: DocumentListener = new DocumentListener
    {
        override def insertUpdate(e: DocumentEvent): Unit =
        {
            onKTAMConstantsChanged()
        }

        override def changedUpdate(e: DocumentEvent): Unit =
        {
            onKTAMConstantsChanged()
        }

        override def removeUpdate(e: DocumentEvent): Unit =
        {
            onKTAMConstantsChanged()
        }
    }

    def onKTAMConstantsChanged(): Unit =
    {

    }

    var speedChangedListener: ChangeListener = new ChangeListener
    {
        override def stateChanged(e: ChangeEvent): Unit =
        {
            simulation.setSimulationSpeed(simulationPanel.simulationSpeedSlider.getValue / 100.0)
        }
    }


}
