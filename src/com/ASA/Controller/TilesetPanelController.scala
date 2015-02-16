package com.ASA.Controller

import java.awt.event.{ActionEvent, ActionListener, MouseEvent, MouseListener}

import com.ASA.GUI.{EditorWindow, TilesetPanel}
import com.ASA.Launcher


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
            editor.setVisible(true)
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
