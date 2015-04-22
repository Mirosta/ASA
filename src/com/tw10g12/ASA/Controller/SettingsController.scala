package com.tw10g12.ASA.Controller

import java.awt.event.{ActionEvent, ActionListener}

import com.tw10g12.ASA.GUI.DrawPanel.SimulationDrawPanelEventHandler
import com.tw10g12.ASA.GUI.SettingsPanel
import com.tw10g12.ASA.Launcher

/**
 * Created by Tom on 21/04/2015.
 */
class SettingsController(settingsPanel: SettingsPanel)
{
    val adjacenciesChangeListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            Launcher.simulationWindow.getDrawPanelEventHandler().asInstanceOf[SimulationDrawPanelEventHandler].setShowAdjacencies(settingsPanel.showAdjacencies.isSelected)
        }
    }

    val antiAliasingChangeListener: ActionListener = new ActionListener
    {
        override def actionPerformed(e: ActionEvent): Unit =
        {
            if(settingsPanel.useAntiAliasing.isSelected)
            {
                Launcher.simulationWindow.getDrawPanelEventHandler().onAntiAliasingEnabled()
                Launcher.editorWindow.getDrawPanelEventHandler().onAntiAliasingEnabled()
            }
            else
            {
                Launcher.simulationWindow.getDrawPanelEventHandler().onAntiAliasingDisabled()
                Launcher.editorWindow.getDrawPanelEventHandler().onAntiAliasingDisabled()
            }
        }
    }


}
