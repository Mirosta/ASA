package com.tw10g12.ASA.Controller

import java.awt.Color
import java.awt.event.{MouseEvent, MouseListener, ActionEvent, ActionListener}
import javax.swing.{JOptionPane, JLabel}
import javax.swing.border.{LineBorder, Border}

import com.tw10g12.ASA.GUI.{EditorWindow, TilesetPanel}
import com.tw10g12.ASA.Model.ATAM.ATAMTile
import com.tw10g12.ASA.Model.Glue
import com.tw10g12.Draw.Engine.Colour
import com.tw10g12.Maths.Vector3

/**
 * Created by Tom on 07/02/2015.
 */
class EditorTilesetPanelController(tilesetPanel: TilesetPanel, editorWindow: EditorWindow) extends TilesetPanelController(tilesetPanel)
{
    val createTileButtonListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            val newTile = new ATAMTile(Vector[Glue](null, null, null, null), Vector[Colour](Colour.PleasantBlue), new Vector3(0,0,0), editorWindow.tileset._2.size)

            editorWindow.setTileset((editorWindow.tileset._1, editorWindow.tileset._2 :+ newTile))
            editorWindow.setActiveTile(newTile)
            editorWindow.setActiveMenu(editorWindow.menuItems(0))
        }
    }
    override val editButtonListener: ActionListener = createTileButtonListener //Used as a create button

    val editTileButtonListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            if (activeLabel != null)
            {
                val tileIndex = Integer.parseInt(activeLabel.getName)
                val tile = if (tileIndex == -1) tilesetPanel.tiles._1 else tilesetPanel.tiles._2(tileIndex)
                editorWindow.setActiveTile(tile)
                editorWindow.setActiveMenu(editorWindow.menuItems(1))
            }
        }
    }

    val removeButtonListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            if (activeLabel != null)
            {
                val tileIndex = Integer.parseInt(activeLabel.getName)
                if (tileIndex == -1)
                {
                    JOptionPane.showMessageDialog(editorWindow, "Sorry, you can't delete the seed tile")
                    return
                }
                var newIndex = -1
                val newTileList = (editorWindow.tileset._2.take(tileIndex) ++ editorWindow.tileset._2.drop(tileIndex + 1)).map(tile => { newIndex += 1; tile.asInstanceOf[ATAMTile].setTypeID(newIndex) })
                editorWindow.setTileset((editorWindow.tileset._1, newTileList))
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
