package com.tw10g12.ASA.GUI

import java.awt.Color
import javax.swing.JPanel

import com.tw10g12.ASA.Controller.EditorTilesetPanelController
import com.tw10g12.ASA.GUI.DrawPanel.{EditorDrawPanelEventHandler, DrawPanelEventHandler}
import com.tw10g12.ASA.Model.Tile

/**
 * Created by Tom on 07/02/2015.
 */
class EditorWindow(var tileset: (Tile, List[Tile])) extends ASAWindow(false)
{

    val CARD_TILE_EDITOR = "tile"
    val CARD_OPTIONS = "settings"
    var tilesetPanel: TilesetPanel = null
    var activeTile: Tile = null
    var drawPanelEventHandler: EditorDrawPanelEventHandler = null

    this.setVisible(false)

    override def setupMenuItems(): Unit =
    {
        createMenuItem("Tile Set", CARD_TILE_EDITOR)
        createMenuItem("Tile Options", CARD_OPTIONS)
    }

    override def setupMenuCardPanels(): Unit =
    {
        setupTileSetPanel()
        setupSettingsPanel()
    }

    def setupTileSetPanel(): Unit =
    {
        tilesetPanel = new TilesetPanel(100, 2)
        tilesetPanel.setTiles(tileset)
        tilesetPanel.setup("Edit Tile", new EditorTilesetPanelController(tilesetPanel))

        contentPanel.add(tilesetPanel, CARD_TILE_EDITOR)
    }

    def setupSettingsPanel(): Unit =
    {
        val panel = new JPanel()
        panel.setBackground(Color.BLUE)
        contentPanel.add(panel, CARD_OPTIONS)
    }

    override def menuSetupComplete(): Unit =
    {
        setActiveMenu(menuItems(0))
    }

    override def getDrawPanelEventHandler(): DrawPanelEventHandler =
    {
        if(drawPanelEventHandler == null)
        {
            drawPanelEventHandler = new EditorDrawPanelEventHandler(tileset)
        }
        return drawPanelEventHandler
    }

    def setTileset(tileset: (Tile, List[Tile])): Unit =
    {
        this.tileset = tileset
        if(tilesetPanel != null) tilesetPanel.setTiles(tileset)
    }

    def setActiveTile(tile: Tile): Unit =
    {
        this.activeTile = tile
        this.drawPanelEventHandler.activeTile = tile
    }
}
