package com.tw10g12.ASA.Controller

import java.awt.event.{ActionEvent, ActionListener}
import java.io.{File, FileWriter, IOException}
import java.nio.charset.Charset
import java.nio.file.{Files, Paths}
import javax.swing.event.{ChangeEvent, ChangeListener, DocumentEvent, DocumentListener}
import javax.swing.filechooser.FileFilter
import javax.swing.{DefaultListModel, JColorChooser, JFileChooser, JOptionPane}

import com.tw10g12.ASA.GUI.EditorWindow
import com.tw10g12.ASA.Model.ATAM.{ATAMGlue, ATAMTile}
import com.tw10g12.ASA.Model.Tile
import com.tw10g12.ASA.Util
import org.json.JSONObject

/**
 * Created by Tom on 24/02/2015.
 */
class EditorController
{
    var editorWindow: EditorWindow = null
    var currentGlue: Integer = null
    val ordinals: Array[String] = Array[String]("North", "East", "South", "West")

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

    val doneListener: ActionListener = new ActionListener
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

    class TilesetFileFilter extends FileFilter
    {
        def getExtension(): String = ".tileset.json"

        override def getDescription: String = "Tilesets (*" + getExtension() + ")"

        override def accept(pathname: File): Boolean =
        {
            return pathname.getName.endsWith(getExtension())
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
                    writer.write(Util.IOUtil.tilesetToJSON(editorWindow.tileset).toString)
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
                    editorWindow.setTileset(tileset)
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
        editorWindow.setActiveTile(editorWindow.activeTile.asInstanceOf[ATAMTile].setGlue(newGlue, currentGlue), update)
        if(update) populateList(editorWindow.activeTile)
    }

    def chooseColour(): Unit =
    {
        val retColor = JColorChooser.showDialog(editorWindow.tileColourPreview, "Edit tile colour", editorWindow.tileColourPreview.getBackground)
        if(retColor != null)
        {
            editorWindow.tileColourPreview.setBackground(retColor)
            editorWindow.setActiveTile(editorWindow.activeTile.asInstanceOf[ATAMTile].setColour(Util.convertColor(retColor)))
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
        editorWindow.editGlueLbl.setText("Edit " + ordinals(this.currentGlue) + " Glue:")
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

}
