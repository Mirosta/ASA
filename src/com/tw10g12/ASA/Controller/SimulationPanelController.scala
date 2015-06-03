package com.tw10g12.ASA.Controller

import java.awt.event.{ActionEvent, ActionListener}
import java.awt.image.BufferedImage
import java.io.{File, FileWriter, IOException}
import java.nio.charset.Charset
import java.nio.file.{Files, Paths}
import javax.swing._
import javax.swing.border.TitledBorder
import javax.swing.event.{ChangeEvent, ChangeListener, DocumentEvent, DocumentListener}

import com.tw10g12.ASA.GUI.SimulationPanel
import com.tw10g12.ASA.Launcher
import com.tw10g12.ASA.Model.ATAM.ATAMTile
import com.tw10g12.ASA.Model.JSON.JSONSimulationFactory
import com.tw10g12.ASA.Model.SMTAM.SMTAMTile
import com.tw10g12.ASA.Model.StateMachine.StateMachine
import com.tw10g12.ASA.Model.{KTAMSimulation, SMTAMSimulation, Simulation, Tile}
import com.tw10g12.ASA.Util.IOUtil.SimulationFileFilter
import com.tw10g12.Maths.Vector3
import org.json.JSONObject

/**
 * Created by Tom on 01/12/2014.
 */
class SimulationPanelController(simulation: SimulationController, simulationPanel: SimulationPanel)
{

    var imageMap: scala.collection.mutable.Map[Tile, BufferedImage] = null
    var tileIndexes: Vector[Tile] = null
    var tileTypeIndexes: Map[Int, Int] = null

    var selectedTile: Vector3 = null
    var populating: Boolean = false

    val startListener = new ActionListener()
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            e.getSource match
            {
                case button: JButton =>
                {
                    if (simulation.state == SimulationStateEnum.Paused || simulation.state == SimulationStateEnum.Stopped)
                    {
                        val index = simulationPanel.simulationMode.getSelectedIndex
                        if (index > -1 && simulation.state == SimulationStateEnum.Stopped && simulation.needsChange())
                        {
                            val item: String = simulationPanel.simulationMode.getItemAt(index)
                            simulation.moveToNextSimulation()
                        }
                        simulation.beginSimulation()
                        onStart()
                    }
                    else
                    {
                        simulation.pauseSimulation()
                        onPause()
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
                    if (simulation.state == SimulationStateEnum.Paused || simulation.state == SimulationStateEnum.Running)
                    {
                        simulation.stopSimulation()
                        onStop()
                    }
                }
            }

        }
    }

    val showHideButtonListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            val visible = Launcher.simulationWindow.statsPanel.isVisible

            if(visible) simulationPanel.showHideButton.setText("Show Statistics")
            else simulationPanel.showHideButton.setText("Hide Statistics")

            Launcher.simulationWindow.statsPanel.setVisible(!visible)
        }
    }

    val modeSelectListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            val index = simulationPanel.simulationMode.getSelectedIndex
            val item: String = simulationPanel.simulationMode.getItemAt(index)
            simulationPanel.simulationModeOptions.show(simulationPanel.simulationModeOptionsPanel, item)
            simulationPanel.simulationModeOptionsPanel.repaint()

            reloadSimulation()
        }
    }

    def reloadSimulation(): Unit =
    {
        if(populating) return
        val index = simulationPanel.simulationMode.getSelectedIndex
        if (index > -1)
        {
            val item: String = simulationPanel.simulationMode.getItemAt(index)
            simulation.setSimulation(getSimulation(item, simulation.getTileTypes()))
            if(simulation.state == SimulationStateEnum.Stopped) simulation.moveToNextSimulation()
        }
    }

    def onPause(): Unit =
    {
        simulationPanel.startButton.setText("Resume")
        simulationPanel.stopButton.setEnabled(true)
        simulationPanel.saveButton.setEnabled(true)
        simulationPanel.loadButton.setEnabled(true)
    }

    def onStop(): Unit =
    {
        simulationPanel.startButton.setText("Start")
        simulationPanel.stopButton.setEnabled(false)
        simulationPanel.saveButton.setEnabled(true)
        simulationPanel.loadButton.setEnabled(true)
    }

    def onStart(): Unit =
    {
        simulationPanel.startButton.setText("Pause")
        simulationPanel.stopButton.setEnabled(true)
        simulationPanel.saveButton.setEnabled(false)
        simulationPanel.loadButton.setEnabled(false)
    }

    val saveSimulationListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            if(simulation.state != SimulationStateEnum.Running)
            {
                saveSimulation()
            }
        }
    }

    val loadSimulationListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            if(simulation.state != SimulationStateEnum.Running)
            {
                loadSimulation()
            }
        }
    }

    val changeTileListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            if(simulationPanel.changeTileTypeCombo.getSelectedIndex > -1)
            {
                simulation.pauseSimulation()
                onPause()
                val newTile = tileIndexes(simulationPanel.changeTileTypeCombo.getSelectedIndex)
                simulation.simulation.state = simulation.simulation.state.setTile(selectedTile, newTile)
            }
        }
    }

    val removeTileListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            if(simulation.getSimulationState().tiles.contains(selectedTile))
            {
                simulation.pauseSimulation()
                onPause()
                simulation.simulation.state = simulation.simulation.state.setTile(selectedTile, null)
            }
        }
    }

    def saveSimulation(): Unit =
    {
        val fileChooser: JFileChooser = new JFileChooser(System.getProperty("user.home"))
        val filter = new SimulationFileFilter

        fileChooser.addChoosableFileFilter(filter)
        fileChooser.setFileFilter(filter)

        val returnVal = fileChooser.showSaveDialog(simulationPanel)

        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            var outputFile = fileChooser.getSelectedFile
            if(!outputFile.getName.contains(".") && fileChooser.getFileFilter == filter)
            {
                outputFile = new File(outputFile.getParentFile, outputFile.getName + filter.getExtension())
            }
            val writer = new FileWriter(outputFile)
            try
            {
                val jsonObject: JSONObject = simulation.saveSimulation()
                writer.write(jsonObject.toString)
            }
            catch
                {
                    case ex: IOException =>
                    {
                        JOptionPane.showMessageDialog(simulationPanel, "Sorry this file could not be written to")
                        Console.err.println("Couldn't write to file " + outputFile.getAbsolutePath)
                        Console.err.println(ex.getLocalizedMessage)
                        ex.printStackTrace()
                    }
                    case ex: Exception =>
                    {
                        JOptionPane.showMessageDialog(simulationPanel, "Unexpected error while writing to file")
                        Console.err.println("Unexepected error while writing to file " + outputFile.getAbsolutePath)
                        Console.err.println(ex.getLocalizedMessage)
                        ex.printStackTrace()
                    }
                }
            finally
            {
                writer.close()
            }
        }
    }

    def loadSimulation(): Unit =
    {
        val fileChooser: JFileChooser = new JFileChooser(System.getProperty("user.home"))
        val filter = new SimulationFileFilter

        fileChooser.addChoosableFileFilter(filter)
        fileChooser.setFileFilter(filter)

        val returnVal = fileChooser.showOpenDialog(simulationPanel)

        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            var inputFile = fileChooser.getSelectedFile
            if(!inputFile.exists())
            {
                JOptionPane.showMessageDialog(simulationPanel, "Sorry that file doesn't exist")
                return
            }

            try
            {
                var serialized = new JSONObject(new String(Files.readAllBytes(Paths.get(inputFile.toURI)), Charset.defaultCharset()))
                var loadedSimulation = JSONSimulationFactory.createSimulation(serialized)

                Launcher.simulationWindow.setTileSet((simulation.getTileTypes()._1, simulation.getTileTypes()._2.toList))
                simulation.setSimulation(loadedSimulation)
                simulation.pauseSimulation()
                onPause()
                populateOptions(loadedSimulation)
            }
            catch
                {
                    case ex: IOException =>
                    {
                        JOptionPane.showMessageDialog(simulationPanel, "Sorry this file could not be read from")
                        Console.err.println("Couldn't write to file " + inputFile.getAbsolutePath)
                        Console.err.println(ex.getLocalizedMessage)
                        ex.printStackTrace()
                    }
                    case ex: Exception =>
                    {
                        JOptionPane.showMessageDialog(simulationPanel, "Unexpected error while reading from file")
                        Console.err.println("Unexepected error while writing to file " + inputFile.getAbsolutePath)
                        Console.err.println(ex.getLocalizedMessage)
                        ex.printStackTrace()
                    }
                }
        }
    }

    def populateOptions(simulation: Simulation): Unit =
    {
        populating = true
        if(simulation.isInstanceOf[KTAMSimulation])
        {
            val kTAMSimulation = simulation.asInstanceOf[KTAMSimulation]
            simulationPanel.reverseConstantInput.setText(kTAMSimulation.backwardConstant.toString)
            simulationPanel.forwardConstantInput.setText(kTAMSimulation.forwardConstant.toString)

            simulationPanel.simulationMode.setSelectedItem("KTAM")
        }
        else if(simulation.isInstanceOf[SMTAMSimulation])
        {
            val sMTAMSimulation = simulation.asInstanceOf[SMTAMSimulation]

            simulationPanel.checkConnected.setSelected(sMTAMSimulation.checkConnected)

            simulationPanel.simulationMode.setSelectedItem("SMTAM")
        }
        else
        {
            simulationPanel.simulationMode.setSelectedItem("ATAM")
        }
        populating = false
    }

    def onGridClicked(gridPosition: Vector3) =
    {
        selectedTile = gridPosition
        simulationPanel.simulationEditOptionsPanel.getBorder.asInstanceOf[TitledBorder].setTitle("Edit Simulation - (" + gridPosition.toString + ")" )
        simulationPanel.simulationEditOptionsPanel.repaint()
        simulationPanel.simulationEditOptionsPanel.setVisible(true)

        if(simulation.getSimulationState().tiles.contains(gridPosition)) simulationPanel.changeTileTypeCombo.setSelectedIndex(tileTypeIndexes(simulation.getSimulationState().tiles(gridPosition).typeID))
    }

    def onGridUnselect(gridPosition: Vector3) =
    {
        selectedTile = null
        simulationPanel.simulationEditOptionsPanel.setVisible(false)
    }

    def getSimulation(simulationType: String, tileTypes: (Tile, Vector[Tile])): Simulation =
    {
        val functions: Map[String, Function[(Tile, Vector[Tile]), Simulation]] = Map[String, Function[(Tile, Vector[Tile]), Simulation]] ("ATAM" -> createATAM, "KTAM" -> createKTAM, "SMTAM" -> createSMTAM)
        return functions(simulationType)(tileTypes)
    }

    def createATAM(tileTypes: (Tile, Vector[Tile])): Simulation =
    {
        return new Simulation(tileTypes._1, tileTypes._2)
    }

    def createKTAM(tileTypes: (Tile, Vector[Tile])): Simulation =
    {
        val reverseConstant = if(simulationPanel.reverseConstantInput.getInputVerifier.verify(simulationPanel.reverseConstantInput)) simulationPanel.reverseConstantInput.getText.toDouble else 0.0
        val forwardConstant = if(simulationPanel.forwardConstantInput.getInputVerifier.verify(simulationPanel.forwardConstantInput)) simulationPanel.forwardConstantInput.getText.toDouble else 0.0
        return new KTAMSimulation(tileTypes._1, tileTypes._2, reverseConstant, forwardConstant)
    }

    def toSMTAMTile(tile: Tile): SMTAMTile =
    {
        if(tile.isInstanceOf[ATAMTile]) return new SMTAMTile(tile.asInstanceOf[ATAMTile])
        else return tile.asInstanceOf[SMTAMTile]
    }

    def getTile(typeID: Int, smtamTileTypes: (SMTAMTile, Vector[SMTAMTile])): SMTAMTile =
    {
        if(typeID < 0) return smtamTileTypes._1
        return smtamTileTypes._2(typeID)
    }

    def createSMTAM(tileTypes: (Tile, Vector[Tile])): Simulation =
    {
        val smtamTileTypes = (toSMTAMTile(tileTypes._1), tileTypes._2.map(tile => toSMTAMTile(tile)))
        val stateMachines = simulation.getStateMachines().map(pair => (if(pair._1.isInstanceOf[ATAMTile]) (getTile(pair._1.typeID, smtamTileTypes), pair._2) else pair))
        return new SMTAMSimulation(smtamTileTypes._1, smtamTileTypes._2, stateMachines, simulationPanel.checkConnected.isSelected)
    }

    val KTAMConstantsChanged: DocumentListener = new DocumentListener
    {
        override def insertUpdate(e: DocumentEvent): Unit =
        {
            onKTAMSettingsChanged()
        }

        override def changedUpdate(e: DocumentEvent): Unit =
        {
            onKTAMSettingsChanged()
        }

        override def removeUpdate(e: DocumentEvent): Unit =
        {
            onKTAMSettingsChanged()
        }
    }
    
    val connectedListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            onSMTAMSettingsChanged()
        }
    }

    def onKTAMSettingsChanged(): Unit =
    {
        reloadSimulation()
    }
    
    def onSMTAMSettingsChanged(): Unit =
    {
        reloadSimulation()
    }

    def onTileTypesChanged(imageMap: scala.collection.mutable.Map[Tile, BufferedImage]): Unit =
    {
        this.imageMap = imageMap
        this.tileIndexes = imageMap.keySet.toVector.sortBy(tile => tile.typeID)
        this.tileTypeIndexes = (0 until tileIndexes.size).map(index => (tileIndexes(index).typeID -> index)).toMap
        val scalingFactor = 2
        simulationPanel.changeTileTypeCombo.setModel(new DefaultComboBoxModel[ImageIcon](tileIndexes.map(tile => new ImageIcon(imageMap(tile).getScaledInstance(imageMap(tile).getWidth / scalingFactor, imageMap(tile).getHeight / scalingFactor, java.awt.Image.SCALE_SMOOTH))).toArray))

        reloadSimulation()
    }

    def onStateMachinesChanged(stateMachines: Map[Tile, StateMachine]): Unit =
    {
        reloadSimulation()
    }

    var speedChangedListener: ChangeListener = new ChangeListener
    {
        override def stateChanged(e: ChangeEvent): Unit =
        {
            simulation.setSimulationSpeed(simulationPanel.simulationSpeedSlider.getValue / 100.0)
        }
    }

}
