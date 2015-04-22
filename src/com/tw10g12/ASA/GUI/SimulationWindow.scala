package com.tw10g12.ASA.GUI

import java.awt._
import java.util.Timer
import javax.media.opengl.awt.GLJPanel
import javax.swing.{JButton, JPanel}

import com.tw10g12.ASA.Controller.{SimulationController, TilesetPanelController}
import com.tw10g12.ASA.GUI.DrawPanel.{DrawPanelEventHandler, SimulationDrawPanelEventHandler}
import com.tw10g12.ASA.Model.StateMachine.StateMachine
import com.tw10g12.ASA.Model.Tile
import com.tw10g12.ASA.Util._
/**
 * Created by Tom on 20/10/2014.
 */
class SimulationWindow(val simulationController: SimulationController) extends ASAWindow(true)
{
    val CARD_TILE_EDITOR = "tile"
    val CARD_SIMULATION = "simulation"
    val CARD_SETTINGS = "settings"

    var tilesetPanel: TilesetPanel = null
    var simulationPanel: SimulationPanel = null

    var statsPanel: StatisticsPanel = null

    var settingsPanel: SettingsPanel = null

    var updateStatsTimer: Timer = new Timer()

    var drawPanelEventHandler: SimulationDrawPanelEventHandler = null

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


    override def setupDrawPanel(owningPanel: JPanel): Unit =
    {
        super.setupDrawPanel(owningPanel)

        statsPanel = new StatisticsPanel(Vector("incorrectBindings", "stateMachines", "disabledGlues", "inertGlues"), Map("incorrectBindings" -> "Incorrect Bindings", "stateMachines" -> "State Machines", "disabledGlues" -> "Disabled Glues", "inertGlues" -> "Inert Glues"))
        statsPanel.setup()
        statsPanel.setVisible(false)

        addToGridBag(statsPanel, owningPanel, 1, 2, 0, 0, new Insets(5, 5, 5, 5))

    }

    override def menuSetupComplete(): Unit =
    {
        setActiveMenu(menuItems(1))

        updateStatsTimer.schedule(toTimerTask(() => onStatsTimerTick), 100, 250)
    }

    def onStatsTimerTick(): Unit =
    {
        val simulationState = simulationController.getSimulationState()
        if(statsPanel != null) statsPanel.update(simulationState.stats, (simulationController.getTileTypes()._1, simulationController.getTileTypes()._2.toList))
    }

    def setupTileSetPanel(): Unit =
    {
        val containingPanel = new JPanel(new GridBagLayout())
        tilesetPanel = new TilesetPanel(100, 2)

        val tileTypes = simulationController.simulation.getTileTypes()
        tilesetPanel.setTiles((tileTypes._1, tileTypes._2.toList))
        tilesetPanel.setup("Edit Tileset", new TilesetPanelController(tilesetPanel))

        val loadTilesetButton = new JButton("Load Tileset")
        loadTilesetButton.addActionListener(tilesetPanel.controller.loadTilesetListener)

        addToGridBag(tilesetPanel, containingPanel, 1, 1, 1, 1)
        addToGridBag(loadTilesetButton, containingPanel, 1, 2, 1, 1, 0, 0, new Insets(0, 5, 5, 5))

        contentPanel.add(containingPanel, CARD_TILE_EDITOR)
    }

    def setupSimulationPanel(): Unit =
    {
        val panel = new JPanel()
        simulationPanel = new SimulationPanel(simulationController)
        simulationPanel.setup()
        addToGridBag(simulationPanel, panel, 1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0)
        contentPanel.add(panel, CARD_SIMULATION)
        simulationPanel.controller.onTileTypesChanged(tilesetPanel.images)
    }

    def setupSettingsPanel(): Unit =
    {
        settingsPanel = new SettingsPanel()
        settingsPanel.setup()

        contentPanel.add(settingsPanel, CARD_SETTINGS)
    }

    override def getDrawPanelEventHandler(): DrawPanelEventHandler =
    {
        return drawPanelEventHandler
    }

    override def getDrawPanelEventHandler(panel: GLJPanel): DrawPanelEventHandler =
    {
        if(drawPanelEventHandler == null)
        {
            drawPanelEventHandler = new SimulationDrawPanelEventHandler(simulationController, panel)
        }
        return drawPanelEventHandler
    }

    def setTileSet(tileSet: (Tile, scala.collection.immutable.List[Tile])): Unit =
    {
        tilesetPanel.setTiles(tileSet)
        simulationController.setTileTypes((tileSet._1, tileSet._2.toVector))
        simulationPanel.controller.onTileTypesChanged(tilesetPanel.images)
        repaint()
    }

    def setStateMachines(stateMachines: Map[Tile, StateMachine]): Unit =
    {
        simulationController.setStateMachines(stateMachines)
        tilesetPanel.stateMachines = stateMachines
        simulationPanel.controller.onStateMachinesChanged(stateMachines)
    }
}
