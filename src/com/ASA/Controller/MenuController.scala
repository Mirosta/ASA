package com.ASA.Controller

import java.awt.event.MouseEvent
import javax.swing.event.MouseInputListener

import com.ASA.GUI.MenuItem

/**
 * Created by Tom on 01/12/2014.
 */
class MenuController(val menuPanel: HasMenu)
{
    val menuItemListener = new MouseInputListener
    {
        override def mouseMoved(e: MouseEvent): Unit = {}

        override def mouseDragged(e: MouseEvent): Unit = {}

        override def mouseExited(e: MouseEvent): Unit = {}

        override def mouseClicked(e: MouseEvent): Unit =
        {
            e.getSource match
            {
                case menuItem: MenuItem =>
                {
                    if(menuPanel.getActiveMenu() != menuItem)
                    {
                        menuPanel.setActiveMenu(menuItem)
                    }
                }
            }
        }

        override def mouseEntered(e: MouseEvent): Unit = {}

        override def mousePressed(e: MouseEvent): Unit = {}

        override def mouseReleased(e: MouseEvent): Unit = {}
    }
}
