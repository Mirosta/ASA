package com.tw10g12.ASA.GUI

import java.awt.event.WindowEvent
import java.awt.font.TextAttribute
import java.awt.{GridBagConstraints, Color, GridBagLayout, Insets}
import javax.swing._
import javax.swing.border.LineBorder

import com.tw10g12.ASA.Controller.{EditorController, EditorTilesetPanelController}
import com.tw10g12.ASA.GUI.DrawPanel.{DrawPanelEventHandler, EditorDrawPanelEventHandler}
import com.tw10g12.ASA.Model.Tile
import com.tw10g12.ASA.Util

/**
 * Created by Tom on 07/02/2015.
 */
class EditorWindow(var tileset: (Tile, List[Tile]), val editorController: EditorController) extends ASAWindow(false)
{

    val CARD_TILE_EDITOR = "tile"
    val CARD_TILE_OPTIONS = "tileSettings"
    val CARD_TILESET_OPTIONS = "tilesetSettings"

    var tilesetPanel: TilesetPanel = null
    var activeTile: Tile = null
    var drawPanelEventHandler: EditorDrawPanelEventHandler = null
    var glueList: JList[String] = null
    var glueEditPanel: JPanel = null

    var editGlueLbl: JLabel = null
    var glueStrengthSlider: JSlider = null
    var glueLabelText: JTextField = null

    var editPanel: JPanel = null
    var noTileSelectedLabel: JLabel = null
    var tileColourPreview: JLabel = null

    var owningSimulationWindow: SimulationWindow = null

    this.setVisible(false)
    editorController.setEditorWindow(this)

    override def setupMenuItems(): Unit =
    {
        createMenuItem("Tile Set", CARD_TILE_EDITOR)
        createMenuItem("Tile Options", CARD_TILE_OPTIONS)
        createMenuItem("Tile Set Options", CARD_TILESET_OPTIONS)
    }

    override def setupMenuCardPanels(): Unit =
    {
        setupTileSetPanel()
        setupSettingsPanel()
        setupTileSetSettingsPanel()
    }

    def setupTileSetPanel(): Unit =
    {
        tilesetPanel = new TilesetPanel(100, 2)
        val controller = new EditorTilesetPanelController(tilesetPanel, this)
        tilesetPanel.setTiles(tileset)
        tilesetPanel.setup("Add Tile", controller)

        val buttonPanel = new JPanel(new GridBagLayout())
        val editButton = new JButton("Edit Tile")
        val removeButton = new JButton("Remove Tile")

        editButton.addActionListener(controller.editTileButtonListener)
        removeButton.addActionListener(controller.removeButtonListener)

        Util.addToGridBag(editButton, buttonPanel, 1, 2, 1, 1, 0.5, 0, new Insets(0,5,5,5))
        Util.addToGridBag(removeButton, buttonPanel, 2, 2, 1, 1, 0.5, 0, new Insets(0,5,5,5))

        Util.addToGridBag(buttonPanel, tilesetPanel, 1, 3, 1, 0)
        contentPanel.add(tilesetPanel, CARD_TILE_EDITOR)
    }

    def setupSettingsPanel(): Unit =
    {
        val outerPanel = new JPanel(new GridBagLayout())
        noTileSelectedLabel = new JLabel("Please edit a tile first")

        editPanel = new JPanel(new GridBagLayout())
        editPanel.setVisible(false)

        val underlineProperties = new java.util.HashMap[TextAttribute, Object]()
        underlineProperties.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)
        val tileLbl = new JLabel("Tile:")
        tileLbl.setFont(tileLbl.getFont.deriveFont(underlineProperties))
        val tileColourLbl = new JLabel("Tile Colour:")
        tileColourPreview = new JLabel("")
        tileColourPreview.setOpaque(true)
        tileColourPreview.setBorder(new LineBorder(Color.black))
        val tileColourChange = new JButton("...")
        tileColourChange.addActionListener(editorController.changeColourListener)
        val gluesLbl = new JLabel("Glues:")
        gluesLbl.setFont(gluesLbl.getFont.deriveFont(underlineProperties))
        val buttonPnl = new JPanel(new GridBagLayout())
        val glueEditBtn = new JButton("Edit")
        glueEditBtn.addActionListener(editorController.editGlueListener)
        val glueResetBtn = new JButton("Reset")
        glueResetBtn.addActionListener(editorController.resetGlueListener)

        glueList = new JList[String](Array[String] ("North Glue - $NGLbl","East Glue - $EGLbl","South Glue - $SGLbl","West Glue - $WGLbl"))
        glueList.setBorder(new LineBorder(Color.black))
        glueEditPanel = new JPanel()
        setupGlueEditPanel(glueEditPanel)

        Util.addToGridBag(tileLbl, editPanel, 0, 0, 3, 1, 0, 0, new Insets(5, 5, 0, 5))
        Util.addToGridBag(tileColourLbl, editPanel, 1, 1, 2, 1, 0, 0, new Insets(5, 20, 0, 5))
        Util.addToGridBag(tileColourPreview, editPanel, 1, 2, 1, 1, 1, 0, new Insets(5, 25, 5, 5))
        Util.addToGridBag(tileColourChange, editPanel, 2, 2, 1, 1, 0, 0, new Insets(5, 5, 5, 5))
        Util.addToGridBag(gluesLbl, editPanel, 0, 3, 3, 1, 0, 0, new Insets(5, 5, 0, 5))
        Util.addToGridBag(glueEditBtn, buttonPnl, 0, 0, 1, 1, 0, 0, new Insets(5, 5, 5, 5))
        Util.addToGridBag(glueResetBtn, buttonPnl, 1, 0, 1, 1, 0, 0, new Insets(5, 5, 5, 5))
        Util.addToGridBag(buttonPnl, editPanel, 1, 4, 2, 1, 0, 0, new Insets(5, 25, 0, 5))
        Util.addToGridBag(glueList, editPanel, 1, 5, 2, 1, 0, 0, new Insets(5, 25, 5, 5))
        Util.addToGridBag(glueEditPanel, editPanel, 0, 6, 3, 1, 0, 0, new Insets(5, 0, 0, 0))

        Util.addToGridBag(editPanel, outerPanel, 0, 0, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0,0)
        Util.addToGridBag(noTileSelectedLabel, outerPanel, 0, 0, 1, 1, 0, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0, 0)
        contentPanel.add(outerPanel, CARD_TILE_OPTIONS)
    }

    def setupGlueEditPanel(panel: JPanel) =
    {
        panel.setLayout(new GridBagLayout())
        panel.setVisible(false)

        editGlueLbl = new JLabel("Edit $GlueOrdinal Glue:")
        val underlineProperties = new java.util.HashMap[TextAttribute, Object]()
        underlineProperties.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)
        editGlueLbl.setFont(editGlueLbl.getFont.deriveFont(underlineProperties))
        val glueStrengthLbl = new JLabel("Glue Strength:")
        glueStrengthSlider = new JSlider(0, 2)
        glueStrengthSlider.createStandardLabels(1)
        glueStrengthSlider.setMajorTickSpacing(1)
        glueStrengthSlider.setSnapToTicks(true)
        glueStrengthSlider.setPaintLabels(true)
        glueStrengthSlider.setPaintTicks(true)
        glueStrengthSlider.addChangeListener(editorController.glueStrengthListener)

        val glueLabelLbl = new JLabel("Glue Label:")
        glueLabelText = new JTextField()
        glueLabelText.getDocument().addDocumentListener(editorController.glueLabelChangeListener)
        val buttonPanel = new JPanel(new GridBagLayout())
        val doneButton = new JButton("Done")
        doneButton.addActionListener(editorController.doneListener)

        Util.addToGridBag(editGlueLbl, panel, 0, 0, 3, 1, 0, 0, new Insets(0, 5, 0, 5))
        Util.addToGridBag(glueStrengthLbl, panel, 1, 1, 2, 1, 0, 0, new Insets(5, 20, 0, 5))
        Util.addToGridBag(glueStrengthSlider, panel, 1, 2, 2, 1, 1, 0, new Insets(5, 25, 5, 5))
        Util.addToGridBag(glueLabelLbl, panel, 1, 3, 2, 1, 0, 0, new Insets(5, 20, 0, 5))
        Util.addToGridBag(glueLabelText, panel, 1, 4, 2, 1, 1, 0, new Insets(5, 25, 5, 5))
        Util.addToGridBag(buttonPanel, panel, 0, 5, 3, 1, 1, 0, new Insets(0, 0, 0, 0))
        Util.addToGridBag(doneButton, buttonPanel, 0, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(5, 5, 5, 5), 0, 0)
    }

    def setupTileSetSettingsPanel(): Unit =
    {
        val tilesetSettingsPanel = new JPanel(new GridBagLayout())

        val buttonPanel = new JPanel(new GridBagLayout())
        val saveButton = new JButton("Save Tile Set")
        val loadButton = new JButton("Load Tile Set")

        saveButton.addActionListener(editorController.saveTileSetButtonListener)
        loadButton.addActionListener(editorController.loadTileSetButtonListener)

        val updateButton = new JButton("Load Tile Set into Simulator")

        updateButton.addActionListener(editorController.updateTileSetButtonListener)

        Util.addToGridBag(saveButton, buttonPanel, 1, 2, 1, 1, 0.5, 0, new Insets(0,5,5,5))
        Util.addToGridBag(loadButton, buttonPanel, 2, 2, 1, 1, 0.5, 0, new Insets(0,5,5,5))

        Util.addToGridBag(buttonPanel, tilesetSettingsPanel, 1, 1, 1, 0)
        Util.addToGridBag(updateButton, tilesetSettingsPanel, 1, 2, 1, 1, 1, 0, new Insets(0,5,5,5))

        contentPanel.add(tilesetSettingsPanel, CARD_TILESET_OPTIONS)
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
        setTileset(tileset, true)
    }

    def setTileset(tileset: (Tile, List[Tile]), clearActiveTile: Boolean): Unit =
    {
        this.tileset = tileset
        if(tilesetPanel != null)
        {
            tilesetPanel.setTiles(tileset)
            tilesetPanel.controller.asInstanceOf[EditorTilesetPanelController].setActiveLabel(null)
        }
        if(clearActiveTile) this.setActiveTile(null)
    }

    def setActiveTile(tile: Tile): Unit =
    {
        setActiveTile(tile, true)
    }

    def setActiveTile(tile: Tile, notifyController: Boolean): Unit =
    {
        val shouldUpdateTileset = tile != null && this.activeTile != null && tile.typeID == activeTile.typeID
        this.activeTile = tile
        this.drawPanelEventHandler.activeTile = tile

        if(notifyController)
        {
            if(shouldUpdateTileset)
            {
                val seed = if (tile.typeID == -1) tile else tileset._1
                var index = -1
                val tiles = tileset._2.map(oldTile =>
                {
                    index += 1; if (index == tile.typeID) tile else oldTile
                })
                setTileset((seed, tiles), false)
            }
            editorController.setActiveTile(tile)
        }
    }

    def setOwningSimulationWindow(simulationWindow: SimulationWindow): Unit =
    {
        this.owningSimulationWindow = simulationWindow
    }

    override def windowClosing(e: WindowEvent): Unit = {}
}
