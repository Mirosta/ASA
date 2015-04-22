package com.tw10g12.ASA.GUI

import java.awt.{Insets, GridBagLayout}
import javax.swing.{JPanel, JCheckBox}

import com.tw10g12.ASA.Controller.SettingsController
import com.tw10g12.ASA.Util

/**
 * Created by Tom on 21/04/2015.
 */
class SettingsPanel extends JPanel(new GridBagLayout())
{
    var useAntiAliasing: JCheckBox = null
    var showAdjacencies: JCheckBox = null

    val controller: SettingsController = new SettingsController(this)

    def setup(): Unit =
    {
        useAntiAliasing = new JCheckBox("Use Anti Aliasing")
        useAntiAliasing.addActionListener(controller.antiAliasingChangeListener)
        showAdjacencies = new JCheckBox("Show Adjacency")
        showAdjacencies.addActionListener(controller.adjacenciesChangeListener)

        Util.addToGridBag(useAntiAliasing, this, 1, 1, 1.0, 0.0, new Insets(5,5,5,5))
        Util.addToGridBag(showAdjacencies, this, 1, 2, 1.0, 0.0, new Insets(5,5,5,5))
    }
}
