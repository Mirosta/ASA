package com.tw10g12.ASA.Controller

import com.tw10g12.ASA.GUI.MenuItem

/**
 * Created by Tom on 01/12/2014.
 */
trait HasMenu
{
    def setActiveMenu(activeMenu: MenuItem): Unit
    def getActiveMenu(): MenuItem
    def getMenuItems(): Vector[MenuItem]
}
