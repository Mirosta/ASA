package com.tw10g12.ASA.Controller

import java.awt.event.{ActionEvent, ActionListener, KeyEvent, KeyListener}
import java.io.{File, FileWriter, IOException}
import java.nio.charset.Charset
import java.nio.file.{Files, Paths}
import javax.swing._
import javax.swing.event.{ChangeEvent, ChangeListener, DocumentEvent, DocumentListener}

import com.tw10g12.ASA.GUI.DrawPanel.EditorState
import com.tw10g12.ASA.GUI.DrawPanel.EditorState.EditorState
import com.tw10g12.ASA.GUI.Interaction.Intersectable
import com.tw10g12.ASA.GUI.{EditorWindow, ReadOnlyTableModel}
import com.tw10g12.ASA.Model.ATAM.{ATAMGlue, ATAMTile}
import com.tw10g12.ASA.Model.StateMachine.{GlueState, StateMachine, StateNode, StateTransition}
import com.tw10g12.ASA.Model.Tile
import com.tw10g12.ASA.Util.IOUtil.TilesetFileFilter
import com.tw10g12.ASA.{Launcher, Util}
import com.tw10g12.Maths.Vector3
import org.json.JSONObject

import scala.util.Random

/**
 * Created by Tom on 24/02/2015.
 */
class EditorController
{

    var editorWindow: EditorWindow = null
    var currentGlue: Integer = null
    val ordinals: Array[String] = Array[String]("North", "East", "South", "West")
    var populating = false

    val glueStateAddListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            if(editorWindow.newGlueStateGlue.getSelectedIndex >= 0 && editorWindow.newGlueStateState.getSelectedIndex >= 0)
            {
                val selectedNode = editorWindow.drawPanelEventHandler.selected.getAttachedModelObject.asInstanceOf[StateNode]
                val filteredGlues = (0 to 3).filter(orientation => selectedNode.getGlueState(orientation) == null)
                val newGlue = filteredGlues(editorWindow.newGlueStateGlue.getSelectedIndex)
                val newState = GlueState.values.toVector.sorted.apply(editorWindow.newGlueStateState.getSelectedIndex)

                val updatedNode = selectedNode.addGlueState(newGlue, newState)
                doUpdateNode(updatedNode)
                populateNodePanel(updatedNode)
            }
        }
    }

    val glueStatesKeyListener: KeyListener = new KeyListener
    {
        override def keyTyped(e: KeyEvent): Unit =
        {
        }

        override def keyPressed(e: KeyEvent): Unit = {}

        override def keyReleased(e: KeyEvent): Unit =
        {
            if(e.getKeyCode == KeyEvent.VK_DELETE)
            {
                if(editorWindow.chosenGlueStates.getSelectedRow >= 0)
                {
                    val selectedNode: StateNode = editorWindow.drawPanelEventHandler.selected.getAttachedModelObject.asInstanceOf[StateNode]
                    val filteredOrientations = (0 to 3).filter(orientation => selectedNode.getGlueState(orientation) != null)
                    if(filteredOrientations.size <= editorWindow.chosenGlueStates.getSelectedRow) return
                    val updatedNode = selectedNode.removeGlueState(filteredOrientations(editorWindow.chosenGlueStates.getSelectedRow))

                    doUpdateNode(updatedNode)
                    populateGlueStateTable(updatedNode)
                }
            }
        }
    }

    def onStateSetAsStarting(newStartingNode: StateNode) =
    {
        val newStateMachine: StateMachine = editorWindow.stateMachines(editorWindow.activeTile).setCurrentNode(newStartingNode)
        editorWindow.stateMachines = editorWindow.stateMachines + (editorWindow.activeTile -> newStateMachine)
    }

    def onTransitionAdded(fromNode: StateNode, fromDirection: Vector3, toNode: StateNode, toDirection: Vector3) =
    {
        val newTransition = new StateTransition(fromNode, toNode, 0.0, fromDirection, toDirection)
        val newList: List[StateTransition] = if(fromNode.transitions.contains("")) newTransition :: fromNode.transitions("") else List(newTransition)
        fromNode.setTransitions(fromNode.transitions + ("" -> newList))
    }

    def onStateAdded(mousePos: Vector3) =
    {
        val newState = new StateNode(Map(), mousePos, "N" + (Launcher.editorWindow.stateMachines(editorWindow.activeTile).stateNodes.size + 1))
        newState.setTransitions(Map())
        val newStateMachine: StateMachine = editorWindow.stateMachines(editorWindow.activeTile).addStateNode(newState)
        editorWindow.stateMachines = editorWindow.stateMachines + (editorWindow.activeTile -> newStateMachine)
    }

    def onStateRemoved(node: StateNode) =
    {
        val newStateMachine = editorWindow.stateMachines(editorWindow.activeTile).removeState(node)

        editorWindow.stateMachines = editorWindow.stateMachines + (editorWindow.activeTile -> newStateMachine)
    }

    def onTransitionRemoved(transition: (String, StateTransition)) =
    {
        transition._2.from.setTransitions(transition._2.from.transitions.map(pair => (pair._1, pair._2.filter(t => t != transition._2))).filter(pair => !pair._2.isEmpty))
    }

    val stateTransitionProbabilityChangeListener: DocumentListener = new DocumentListener
    {
        override def insertUpdate(e: DocumentEvent): Unit =
        {
            updateTransition()
        }

        override def changedUpdate(e: DocumentEvent): Unit =
        {
            updateTransition()
        }

        override def removeUpdate(e: DocumentEvent): Unit =
        {
            updateTransition()
        }
    }

    val stateNodeNameChangeListener: DocumentListener = new DocumentListener
    {

        override def insertUpdate(e: DocumentEvent): Unit =
        {
            updateNode()
        }

        override def changedUpdate(e: DocumentEvent): Unit =
        {
            updateNode()
        }

        override def removeUpdate(e: DocumentEvent): Unit =
        {
            updateNode()
        }
    }

    val stateTransitionOnChangeListener: DocumentListener = new DocumentListener
    {
        override def insertUpdate(e: DocumentEvent): Unit =
        {
            updateTransitionOn()
        }

        override def changedUpdate(e: DocumentEvent): Unit =
        {
            updateTransitionOn()
        }

        override def removeUpdate(e: DocumentEvent): Unit =
        {
            updateTransitionOn()
        }
    }

    val stateMachineDetailDoneListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            editorWindow.stateMachineDetailPanel.setVisible(false)
            editorWindow.drawPanelEventHandler.selected = null
        }
    }

    val setStartListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            if(editorWindow.setStartButton.isSelected) updateEditingState(EditorState.SetStarting)
            else updateEditingState(EditorState.Default)
        }
    }

    val addTransitionListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            if(editorWindow.addTransitionButton.isSelected) updateEditingState(EditorState.AddTransition)
            else updateEditingState(EditorState.Default)
        }
    }

    val addStateListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            if(editorWindow.addStateButton.isSelected) updateEditingState(EditorState.AddState)
            else updateEditingState(EditorState.Default)
        }
    }

    val stateMachineDoneListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            updateEditingState(EditorState.Default)
            editorWindow.editingStateMachine = false
            editorWindow.cardLayout.show(editorWindow.contentPanel, editorWindow.CARD_TILE_OPTIONS)
        }
    }

    var editStateMachineListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            if(editorWindow.activeTile != null)
            {
                editorWindow.editingStateMachine = true
                editorWindow.cardLayout.show(editorWindow.contentPanel, editorWindow.CARD_STATE_MACHINE_EDITOR)
                if(!editorWindow.stateMachines.contains(editorWindow.activeTile) || editorWindow.stateMachines(editorWindow.activeTile) == null)
                {
                    editorWindow.stateMachines = editorWindow.stateMachines + (editorWindow.activeTile -> new StateMachine(null, Map(), new Random(), List()))
                }
            }
        }
    }

    val editGlueListener: ActionListener = new ActionListener()
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            editGlue()
        }
    }

    val resetGlueListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            editGlue()
            resetGlue()
        }
    }

    val changeColourListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            chooseColour()
        }
    }

    val glueDoneListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            updateGlue(true)
            currentGlue = null
            editorWindow.glueEditPanel.setVisible(false)
        }
    }

    val glueStrengthListener: ChangeListener = new ChangeListener
    {
        override def stateChanged(e: ChangeEvent): Unit =
        {
            updateGlue()
        }
    }

    val glueLabelChangeListener: DocumentListener = new DocumentListener
    {
        override def insertUpdate(e: DocumentEvent): Unit =
        {
            updateGlue()
        }

        override def changedUpdate(e: DocumentEvent): Unit =
        {
            updateGlue()
        }

        override def removeUpdate(e: DocumentEvent): Unit =
        {
            updateGlue()
        }
    }

    def saveTileSetButtonListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            val fileChooser: JFileChooser = new JFileChooser(System.getProperty("user.home"))
            val filter = new TilesetFileFilter

            fileChooser.addChoosableFileFilter(filter)
            fileChooser.setFileFilter(filter)

            val returnVal = fileChooser.showSaveDialog(editorWindow)

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
                    val jsonObject = Util.IOUtil.tilesetToJSON(editorWindow.tileset, editorWindow.stateMachines)
                    writer.write(jsonObject.toString)
                }
                catch
                {
                    case ex: IOException =>
                    {
                        JOptionPane.showMessageDialog(editorWindow, "Sorry this file could not be written to")
                        Console.err.println("Couldn't write to file " + outputFile.getAbsolutePath)
                        Console.err.println(ex.getLocalizedMessage)
                        ex.printStackTrace()
                    }
                    case ex: Exception =>
                    {
                        JOptionPane.showMessageDialog(editorWindow, "Unexpected error while writing to file")
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
    }

    def loadTileSetButtonListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            if(JOptionPane.showConfirmDialog(editorWindow, "You will lose any unsaved changes to the current tile set, are you sure?", "Load new tile set", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) return
            val fileChooser: JFileChooser = new JFileChooser(System.getProperty("user.home"))
            val filter = new TilesetFileFilter

            fileChooser.addChoosableFileFilter(filter)
            fileChooser.setFileFilter(filter)

            val returnVal = fileChooser.showOpenDialog(editorWindow)

            if(returnVal == JFileChooser.APPROVE_OPTION)
            {
                var inputFile = fileChooser.getSelectedFile
                if(!inputFile.exists())
                {
                    JOptionPane.showMessageDialog(editorWindow, "Sorry that file doesn't exist")
                    return
                }

                try
                {
                    var serialized = new JSONObject(new String(Files.readAllBytes(Paths.get(inputFile.toURI)), Charset.defaultCharset()))
                    var tileset = Util.IOUtil.JSONtoTileset(serialized)
                    editorWindow.setTileset((tileset._1, tileset._2))
                    editorWindow.stateMachines = tileset._3
                }
                catch
                {
                    case ex: IOException =>
                    {
                        JOptionPane.showMessageDialog(editorWindow, "Sorry this file could not be read from")
                        Console.err.println("Couldn't write to file " + inputFile.getAbsolutePath)
                        Console.err.println(ex.getLocalizedMessage)
                        ex.printStackTrace()
                    }
                    case ex: Exception =>
                    {
                        JOptionPane.showMessageDialog(editorWindow, "Unexpected error while reading from file")
                        Console.err.println("Unexepected error while writing to file " + inputFile.getAbsolutePath)
                        Console.err.println(ex.getLocalizedMessage)
                        ex.printStackTrace()
                    }
                }
            }
        }
    }

    def updateTileSetButtonListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            editorWindow.owningSimulationWindow.setTileSet(editorWindow.tileset)
            editorWindow.owningSimulationWindow.setStateMachines(editorWindow.stateMachines)
        }
    }

    def updateGlue(): Unit =
    {
        updateGlue(false)
    }

    def updateGlue(update: Boolean): Unit =
    {
        if(currentGlue == -1) return

        val newGlue = new ATAMGlue(editorWindow.glueLabelText.getText, editorWindow.glueStrengthSlider.getValue)
        updateTile(editorWindow.activeTile.asInstanceOf[ATAMTile].setGlue(newGlue, currentGlue), update)
        if(update) populateList(editorWindow.activeTile)
    }

    def updateTile(newTile: Tile): Unit = updateTile(newTile, true)

    def updateTile(newTile: Tile, update: Boolean): Unit =
    {
        if(editorWindow.stateMachines.contains(editorWindow.activeTile))
        {
            editorWindow.stateMachines = editorWindow.stateMachines - editorWindow.activeTile + (newTile -> editorWindow.stateMachines(editorWindow.activeTile))
        }
        editorWindow.setActiveTile(newTile, update)
    }

    def chooseColour(): Unit =
    {
        val retColor = JColorChooser.showDialog(editorWindow.tileColourPreview, "Edit tile colour", editorWindow.tileColourPreview.getBackground)
        if(retColor != null)
        {
            editorWindow.tileColourPreview.setBackground(retColor)
            updateTile(editorWindow.activeTile.asInstanceOf[ATAMTile].setColour(Util.convertColor(retColor)))
        }
    }

    def setEditorWindow(editorWindow: EditorWindow): Unit =
    {
        this.editorWindow = editorWindow
    }

    def editGlue(): Unit =
    {
        if(editorWindow.glueList.getSelectedIndex == -1) return
        val currentGlue = editorWindow.activeTile.glues(editorWindow.glueList.getSelectedIndex)
        this.currentGlue = editorWindow.glueList.getSelectedIndex
        if(currentGlue == null)
        {
            populateGluePanel(0, "")
        }
        else
        {
            populateGluePanel(currentGlue.strength, currentGlue.label)
        }
        editorWindow.glueEditPanel.setVisible(true)
    }

    def resetGlue(): Unit =
    {
        if(this.currentGlue != null) populateGluePanel(0, "")
    }

    def populateGluePanel(strength: Int, label: String): Unit =
    {
        editorWindow.editGlueBorderLbl.setTitle("Edit " + ordinals(this.currentGlue) + " Glue:")
        editorWindow.glueStrengthSlider.setValue(strength)
        editorWindow.glueLabelText.setText(label)
    }

    def setActiveTile(tile: Tile): Unit =
    {
        if(tile == null)
        {
            editorWindow.editPanel.setVisible(false)
            editorWindow.noTileSelectedLabel.setVisible(true)
        }
        else
        {
            editorWindow.editPanel.setVisible(true)
            editorWindow.noTileSelectedLabel.setVisible(false)

            editorWindow.tileColourPreview.setBackground(Util.convertColour(tile.getColour))
            editorWindow.tileColourPreview.invalidate()
            populateList(tile)
        }
    }

    private def populateList(tile: Tile): Unit =
    {
        var i: Int = 0
        val newModel: DefaultListModel[String] = new DefaultListModel[String]()
        for(i <- 0 to ordinals.length - 1)
        {
            val glueLabel = if(tile.glues(i) != null) tile.glues(i).label else ""
            newModel.addElement(ordinals(i) + " - " + glueLabel)
        }

        editorWindow.glueList.setModel(newModel)
    }

    def updateEditingState(editorState: EditorState): Unit =
    {
        if(editorWindow != null && editorWindow.drawPanelEventHandler != null)
        {
            editorWindow.drawPanelEventHandler.editingState = editorState
            editorWindow.addStateButton.setSelected(editorState == EditorState.AddState)
            editorWindow.addTransitionButton.setSelected(editorState == EditorState.AddTransition)
            editorWindow.setStartButton.setSelected(editorState == EditorState.SetStarting)
            editorWindow.drawPanelEventHandler.onEditingStateChanged(editorState)
        }
    }

    def onStateMachinePartSelected(selected: Intersectable) =
    {
        if(selected == null || selected.getAttachedModelObject == null)
        {
            editorWindow.stateMachineDetailPanel.setVisible(false)
        }
        else
        {
            editorWindow.drawPanelEventHandler.panel.requestFocus()
            if(selected.getAttachedModelObject.isInstanceOf[StateNode])
            {
                editorWindow.stateMachineDetailPanel.setVisible(true)
                editorWindow.stateMachineDetailCardLayout.show(editorWindow.stateMachineDetailPanel, editorWindow.CARD_DETAIL_NODE_EDIT)
                populateNodePanel(selected.getAttachedModelObject.asInstanceOf[StateNode])
            }
            else if(selected.getAttachedModelObject.isInstanceOf[(String, StateTransition)])
            {
                editorWindow.stateMachineDetailPanel.setVisible(true)
                editorWindow.stateMachineDetailCardLayout.show(editorWindow.stateMachineDetailPanel, editorWindow.CARD_DETAIL_TRANSITION_EDIT)
                val selectedPair = selected.getAttachedModelObject.asInstanceOf[(String, StateTransition)]
                populateTransitionPanel(selectedPair._1, selectedPair._2)
            }
            else
            {
                editorWindow.stateMachineDetailPanel.setVisible(false)
            }
        }
    }

    def populateNodePanel(stateNode: StateNode): Unit =
    {
        populating = true
        editorWindow.nodeNameInput.setText(stateNode.label)
        populateGlueStateTable(stateNode)
        populating = false
    }

    def populateGlueStateTable(stateNode: StateNode): Unit =
    {
        val columnNames = Array[AnyRef]("Glue", "Glue State")
        val newTableModel = new ReadOnlyTableModel(generateGlueStateTable(stateNode).asInstanceOf[Array[Array[AnyRef]]], columnNames)
        editorWindow.chosenGlueStates.setModel(newTableModel)
        editorWindow.newGlueStateGlue.setModel(new DefaultComboBoxModel[String]((0 to 3).filter(orientation => stateNode.getGlueState(orientation) == null).map(orientation => Util.orientationToFullHeading(orientation)).toArray))
    }

    def generateGlueStateTable(stateNode: StateNode): Array[Array[String]] =
    {
        val filteredOrientations = (0 to 3).filter(orientation => stateNode.getGlueState(orientation) != null)
        if(filteredOrientations.isEmpty) return Array(Array("", ""))
        return filteredOrientations.map(orientation => Array(Util.orientationToFullHeading(orientation), stateNode.getGlueState(orientation).toString)).toArray
    }

    def populateTransitionPanel(transitionOn: String, stateTransition: StateTransition): Unit =
    {
        populating = true
        editorWindow.transitionOnInput.setText(transitionOn)
        editorWindow.probabilityInput.setText(stateTransition.probability.toString)
        populating = false
    }

    def doUpdateNode(updatedNode: StateNode): Unit =
    {
        val oldNode: StateNode = editorWindow.drawPanelEventHandler.selected.getAttachedModelObject.asInstanceOf[StateNode]
        val oldStateMachine: StateMachine = editorWindow.stateMachines(editorWindow.activeTile)
        val updatedStateMachine: StateMachine = oldStateMachine.updateStateNode(oldNode, updatedNode)
        editorWindow.drawPanelEventHandler.selected.setAttachedModelObject(updatedNode)

        editorWindow.stateMachines = editorWindow.stateMachines + (editorWindow.activeTile -> updatedStateMachine)
    }

    def updateNode(): Unit =
    {
        val oldNode: StateNode = editorWindow.drawPanelEventHandler.selected.getAttachedModelObject.asInstanceOf[StateNode]
        val updatedNode = oldNode.setLabel(editorWindow.nodeNameInput.getText)
        doUpdateNode(updatedNode)
    }

    def updateTransitionOn(): Unit =
    {
        if(populating) return
        val transition: (String, StateTransition) = editorWindow.drawPanelEventHandler.selected.getAttachedModelObject.asInstanceOf[(String, StateTransition)]
        val newTransitionOn = editorWindow.transitionOnInput.getText
        if(transition._1.equals(newTransitionOn)) return
        val updatedOldList = transition._2.from.transitions(transition._1).filter(t => t != transition._2)
        val newTransitionList = transition._2 :: (if(transition._2.from.transitions.contains(transition._1)) transition._2.from.transitions(transition._1).filter(t => transition._2 != t) else List())
        transition._2.from.setTransitions(transition._2.from.transitions + (transition._1 -> updatedOldList) + (newTransitionOn -> newTransitionList))
        editorWindow.drawPanelEventHandler.selected.setAttachedModelObject((newTransitionOn, transition._2))
    }

    def updateTransition(): Unit =
    {
        if(populating) return
        editorWindow.probabilityInput.validate()
        if(!editorWindow.probabilityInput.getInputVerifier.verify(editorWindow.probabilityInput)) return
        val oldTransition: (String, StateTransition) = editorWindow.drawPanelEventHandler.selected.getAttachedModelObject.asInstanceOf[(String, StateTransition)]
        val newTransition: StateTransition = oldTransition._2.setProbability(editorWindow.probabilityInput.getText.toDouble)
        val newTransitions: Map[String, List[StateTransition]] = oldTransition._2.from.transitions.map(pair => if(pair._1 == oldTransition._1) (pair._1, newTransition :: pair._2.filter(transition => transition != oldTransition._2)) else pair)
        oldTransition._2.from.setTransitions(newTransitions)
        editorWindow.drawPanelEventHandler.selected.setAttachedModelObject((oldTransition._1, newTransition))
    }
}
