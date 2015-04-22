package com.tw10g12.ASA.Controller

import java.awt.event.{ActionEvent, ActionListener, MouseEvent, MouseListener}
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.{Paths, Files}
import javax.swing.{JFileChooser, JOptionPane}

import com.tw10g12.ASA.GUI.{EditorWindow, TilesetPanel}
import com.tw10g12.ASA.{Util, Launcher}
import com.tw10g12.ASA.Util.IOUtil.TilesetFileFilter
import org.json.JSONObject


/**
 * Created by Tom on 07/02/2015.
 */
class TilesetPanelController(tilesetPanel: TilesetPanel)
{
    val editButtonListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            val editor: EditorWindow = Launcher.editorWindow
            editor.setTileset(tilesetPanel.tiles)
            editor.stateMachines = tilesetPanel.stateMachines
            editor.setVisible(true)
        }
    }

    val loadTilesetListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            loadTileset()
        }
    }

    def loadTileset(): Unit =
    {
        val fileChooser: JFileChooser = new JFileChooser(System.getProperty("user.home"))
        val filter = new TilesetFileFilter

        fileChooser.addChoosableFileFilter(filter)
        fileChooser.setFileFilter(filter)

        val returnVal = fileChooser.showOpenDialog(tilesetPanel)

        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            var inputFile = fileChooser.getSelectedFile
            if(!inputFile.exists())
            {
                JOptionPane.showMessageDialog(tilesetPanel, "Sorry that file doesn't exist")
                return
            }

            try
            {
                val serialized = new JSONObject(new String(Files.readAllBytes(Paths.get(inputFile.toURI)), Charset.defaultCharset()))
                val tileset = Util.IOUtil.JSONtoTileset(serialized)
                Launcher.simulationWindow.setTileSet((tileset._1, tileset._2))
                Launcher.simulationWindow.setStateMachines(tileset._3)
            }
            catch
                {
                    case ex: IOException =>
                    {
                        JOptionPane.showMessageDialog(tilesetPanel, "Sorry this file could not be read from")
                        Console.err.println("Couldn't write to file " + inputFile.getAbsolutePath)
                        Console.err.println(ex.getLocalizedMessage)
                        ex.printStackTrace()
                    }
                    case ex: Exception =>
                    {
                        JOptionPane.showMessageDialog(tilesetPanel, "Unexpected error while reading from file")
                        Console.err.println("Unexepected error while writing to file " + inputFile.getAbsolutePath)
                        Console.err.println(ex.getLocalizedMessage)
                        ex.printStackTrace()
                    }
                }
        }
    }

    val tileMouseListener: MouseListener = new MouseListener
    {
        override def mouseExited(e: MouseEvent): Unit = {}

        override def mouseClicked(e: MouseEvent): Unit = {}

        override def mouseEntered(e: MouseEvent): Unit = {}

        override def mousePressed(e: MouseEvent): Unit = {}

        override def mouseReleased(e: MouseEvent): Unit = {}
    }
}
