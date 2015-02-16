package com.ASA.Controller

import java.awt.Color
import java.awt.event.{MouseEvent, MouseListener, ActionEvent, ActionListener}
import javax.swing.JLabel
import javax.swing.border.{LineBorder, Border}

import com.ASA.GUI.{EditorWindow, TilesetPanel}

/**
 * Created by Tom on 07/02/2015.
 */
class EditorTilesetPanelController(tilesetPanel: TilesetPanel) extends TilesetPanelController(tilesetPanel)
{
    override val editButtonListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            if (activeLabel != null)
            {
                val tileIndex = Integer.parseInt(activeLabel.getName)
                val tile = if (tileIndex == -1) tilesetPanel.tiles._1 else tilesetPanel.tiles._2(tileIndex)
                val editor: EditorWindow = activeLabel.getRootPane.getParent.asInstanceOf[EditorWindow]
                editor.setActiveTile(tile)
            }
        }
    }

    var activeLabel: JLabel = null

    def setActiveLabel(label: JLabel): Unit =
    {
        if(activeLabel != null)
        {
            val noBorder: Border = if(activeLabel.getName == "-1") tilesetPanel.getSeedBorder() else tilesetPanel.getBlankBorder()
            activeLabel.setBorder(noBorder)
        }
        activeLabel = label
        if(activeLabel != null) activeLabel.setBorder(getActiveBorder)
    }

    def getActiveBorder(): Border = new LineBorder(Color.BLACK)

    override val tileMouseListener: MouseListener = new MouseListener
    {
        override def mouseExited(e: MouseEvent): Unit = {}

        override def mouseClicked(e: MouseEvent): Unit =
        {
            if (e.getSource.isInstanceOf[JLabel])
            {
                val label = e.getSource.asInstanceOf[JLabel]
                if(label == activeLabel) setActiveLabel(null)
                else setActiveLabel(label)
            }
        }

        override def mouseEntered(e: MouseEvent): Unit = {}

        override def mousePressed(e: MouseEvent): Unit = {}

        override def mouseReleased(e: MouseEvent): Unit = {}
    }
}
