package com.ASA.GUI

import java.awt._
import javax.swing.JPanel

import com.ASA.Controller.{TilesetPanelController, SimulationController}
import com.ASA.GUI.DrawPanel.{DrawPanelEventHandler, SimulationDrawPanelEventHandler}
import com.ASA.Util._
/**
 * Created by Tom on 20/10/2014.
 */
class SimulationWindow(val simulationController: SimulationController) extends ASAWindow(true)
{
    val CARD_TILE_EDITOR = "tile"
    val CARD_SIMULATION = "simulation"
    val CARD_SETTINGS = "settings"

    override def setupMenuItems(): Unit =
    {
        createMenuItem("Tile Set", CARD_TILE_EDITOR)
        createMenuItem("Simulation", CARD_SIMULATION)
        createMenuItem("Settings", CARD_SETTINGS)
    }

    override def setupMenuCardPanels(): Unit =
    {
        setupTileSetPanel()
        setupSimulationPanel()
        setupSettingsPanel()
    }

    override def menuSetupComplete(): Unit =
    {
        setActiveMenu(menuItems(1))
    }

    def setupTileSetPanel(): Unit =
    {
        var tilesetPanel = new TilesetPanel(100, 2)
        var tileTypes = simulationController.simulation.getTileTypes()
        tilesetPanel.setTiles((tileTypes._1, tileTypes._2.toList))
        tilesetPanel.setup("Edit Tileset", new TilesetPanelController(tilesetPanel))

        contentPanel.add(tilesetPanel, CARD_TILE_EDITOR)
    }

    def setupSimulationPanel(): Unit =
    {
        val panel = new JPanel()
        val simulationPanel = new SimulationPanel(simulationController)
        simulationPanel.setup()
        simulationPanel.setBackground(Color.GREEN)
        addToGridBag(simulationPanel, panel, 1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0)
        contentPanel.add(panel, CARD_SIMULATION)
    }

    def setupSettingsPanel(): Unit =
    {
        val panel = new JPanel()
        panel.setBackground(Color.BLUE)
        contentPanel.add(panel, CARD_SETTINGS)
    }

    override def getDrawPanelEventHandler(): DrawPanelEventHandler =
    {
        return new SimulationDrawPanelEventHandler(simulationController)
    }
}
