package com.tw10g12.ASA.GUI

import java.awt._
import java.awt.event.WindowEvent
import java.awt.font.TextAttribute
import javax.media.opengl.awt.GLJPanel
import javax.swing._
import javax.swing.border.{LineBorder, TitledBorder}

import com.tw10g12.ASA.Controller.{EditorController, EditorTilesetPanelController}
import com.tw10g12.ASA.GUI.DrawPanel.{DrawPanelEventHandler, EditorDrawPanelEventHandler, EditorState}
import com.tw10g12.ASA.Model.StateMachine.GlueState.GlueState
import com.tw10g12.ASA.Model.StateMachine.{GlueState, StateMachine, StateNode, StateTransition}
import com.tw10g12.ASA.Model.Tile
import com.tw10g12.ASA.Util
import com.tw10g12.Maths.Vector3
import com.tw10g12.Validation.DecimalValidator

import scala.util.Random

/**
 * Created by Tom on 07/02/2015.
 */
class EditorWindow(var tileset: (Tile, scala.collection.immutable.List[Tile]), val editorController: EditorController) extends ASAWindow(false)
{

    val CARD_TILE_EDITOR = "tile"
    val CARD_TILE_OPTIONS = "tileSettings"
    val CARD_TILESET_OPTIONS = "tilesetSettings"
    val CARD_STATE_MACHINE_EDITOR = "stateMachine"

    val CARD_DETAIL_NODE_EDIT = "nodeEdit"
    val CARD_DETAIL_TRANSITION_EDIT = "transitionEdit"

    var stateMachines: Map[Tile, StateMachine] = Map(null.asInstanceOf[Tile] -> createStateMachine())
    var editingStateMachine: Boolean = false
    def createStateMachine(): StateMachine =
    {
        val nodes: scala.collection.immutable.List[StateNode] = scala.collection.immutable.List[StateNode](new StateNode(Map[Int, GlueState](), new Vector3(0,0,0), "N1"), new StateNode(Map[Int, GlueState](0 -> GlueState.Disabled), new Vector3(5, 0, 0), "N2"))
        val transitions: scala.collection.immutable.List[StateTransition] = scala.collection.immutable.List[StateTransition](new StateTransition(nodes.head, nodes.tail.head, 1.0, new Vector3(1, 1, 0).normalise(), new Vector3(-1, 1, 0).normalise()))
        nodes.head.setTransitions(Map[String, scala.collection.immutable.List[StateTransition]]("S+" -> transitions))
        nodes.tail.head.setTransitions(Map[String, scala.collection.immutable.List[StateTransition]]())
        val stateMachine: StateMachine = new StateMachine(nodes.head, Map[Int, GlueState](), new Random(), nodes)
        return stateMachine
    }

    var tilesetPanel: TilesetPanel = null
    var activeTile: Tile = null
    var drawPanelEventHandler: EditorDrawPanelEventHandler = null
    var glueList: JList[String] = null
    var glueEditPanel: JPanel = null

    var editGlueBorderLbl: TitledBorder = null
    var glueStrengthSlider: JSlider = null
    var glueLabelText: JTextField = null

    var editPanel: JPanel = null
    var noTileSelectedLabel: JLabel = null
    var tileColourPreview: JLabel = null

    var stateMachineEditPanel: JPanel = null
    var addStateButton: JToggleButton = null
    var addTransitionButton: JToggleButton = null
    var setStartButton: JToggleButton = null
    var stateMachineDetailCardLayout: CardLayout = null
    var stateMachineDetailPanel: JPanel = null

    var nodeNameInput: JTextField = null
    var newGlueStateGlue: JComboBox[String] = null
    var newGlueStateState: JComboBox[String] = null
    var chosenGlueStates: JTable = null

    var probabilityInput: JTextField = null
    var transitionOnInput: JTextField = null

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


    override def setupDrawPanel(owningPanel: JPanel): Unit =
    {
        super.setupDrawPanel(owningPanel)
        val panel = owningPanel.getComponent(0).asInstanceOf[GLJPanel]
        panel.addKeyListener(drawPanelEventHandler)
    }

    def setupSettingsPanel(): Unit =
    {
        val outerPanel = new JPanel(new GridBagLayout())
        val scroll = new JScrollPane(outerPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
        scroll.setViewportBorder(null)
        scroll.setBorder(null)

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

        val stateMachineLbl = new JLabel("State Machine:")
        stateMachineLbl.setFont(gluesLbl.getFont.deriveFont(underlineProperties))
        val stateMachineEditButton = new JButton("Edit State Machine")
        stateMachineEditButton.addActionListener(editorController.editStateMachineListener)

        stateMachineEditPanel = new JPanel()
        setupStateMachineEditPanel(stateMachineEditPanel)

        Util.addToGridBag(tileLbl, editPanel, 0, 0, 3, 1, 0, 0, new Insets(5, 5, 0, 5))
        Util.addToGridBag(tileColourLbl, editPanel, 1, 1, 2, 1, 0, 0, new Insets(5, 20, 0, 5))
        Util.addToGridBag(tileColourPreview, editPanel, 1, 2, 1, 1, 1, 0, new Insets(5, 25, 5, 5))
        Util.addToGridBag(tileColourChange, editPanel, 2, 2, 1, 1, 0, 0, new Insets(5, 5, 5, 5))
        Util.addToGridBag(gluesLbl, editPanel, 0, 3, 3, 1, 0, 0, new Insets(5, 5, 0, 5))
        Util.addToGridBag(glueEditBtn, buttonPnl, 0, 0, 1, 1, 0, 0, new Insets(5, 5, 5, 5))
        Util.addToGridBag(glueResetBtn, buttonPnl, 1, 0, 1, 1, 0, 0, new Insets(5, 5, 5, 5))
        Util.addToGridBag(buttonPnl, editPanel, 1, 4, 2, 1, 0, 0, new Insets(5, 25, 0, 5))
        Util.addToGridBag(glueList, editPanel, 1, 5, 2, 1, 0, 0, new Insets(5, 25, 5, 5))
        Util.addToGridBag(glueEditPanel, editPanel, 0, 6, 3, 1, 0, 0, new Insets(5, 5, 0, 5))
        Util.addToGridBag(stateMachineLbl, editPanel, 0, 7, 3, 1, 0, 0, new Insets(5, 5, 0, 5))
        Util.addToGridBag(stateMachineEditButton, editPanel, 0, 8, 2, 1, 0, 0, new Insets(5, 20, 0 ,5))
        //Util.addToGridBag(stateMachineEditPanel, editPanel, 0, 9, 3, 1, 0, 0, new Insets(5, 0, 0, 0))

        Util.addToGridBag(editPanel, outerPanel, 0, 0, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0,0)
        Util.addToGridBag(noTileSelectedLabel, outerPanel, 0, 0, 1, 1, 0, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0, 0)
        contentPanel.add(scroll, CARD_TILE_OPTIONS)
        contentPanel.add(stateMachineEditPanel, CARD_STATE_MACHINE_EDITOR)
    }

    def setupGlueEditPanel(panel: JPanel) =
    {
        val innerPanel = new JPanel(new GridBagLayout())

        panel.setLayout(new GridBagLayout())
        panel.setVisible(false)

        editGlueBorderLbl = new TitledBorder(new LineBorder(Color.black), "Edit $GlueOrdinal Glue:")
        panel.setBorder(editGlueBorderLbl)
        val underlineProperties = new java.util.HashMap[TextAttribute, Object]()
        underlineProperties.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)
        val glueStrengthLbl = new JLabel("Glue Strength:")
        editGlueBorderLbl.setTitleFont(glueStrengthLbl.getFont.deriveFont(underlineProperties))
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
        doneButton.addActionListener(editorController.glueDoneListener)

        //Util.addToGridBag(editGlueLbl, panel, 0, 0, 3, 1, 0, 0, new Insets(0, 5, 0, 5))
        Util.addToGridBag(glueStrengthLbl, innerPanel, 1, 1, 2, 1, 0, 0, new Insets(5, 10, 0, 5))
        Util.addToGridBag(glueStrengthSlider, innerPanel, 1, 2, 2, 1, 1, 0, new Insets(5, 15, 5, 5))
        Util.addToGridBag(glueLabelLbl, innerPanel, 1, 3, 2, 1, 0, 0, new Insets(5, 10, 0, 5))
        Util.addToGridBag(glueLabelText, innerPanel, 1, 4, 2, 1, 1, 0, new Insets(5, 15, 5, 5))
        Util.addToGridBag(buttonPanel, innerPanel, 0, 5, 3, 1, 1, 0, new Insets(0, 0, 0, 0))
        Util.addToGridBag(doneButton, buttonPanel, 0, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(5, 5, 5, 5), 0, 0)

        innerPanel.setPreferredSize(new Dimension(210, innerPanel.getPreferredSize.getHeight.asInstanceOf[Int]))
        Util.addToGridBag(innerPanel, panel, 0, 0, 1, 1, 0, 1, new Insets(0,0,0,10))
    }

    def setupStateMachineEditPanel(panel: JPanel) =
    {
        val innerPanel = new JPanel(new GridBagLayout())
        panel.setLayout(new GridBagLayout())

        val editStateMachineLbl = new JLabel("Edit State Machine:")
        val underlineProperties = new java.util.HashMap[TextAttribute, Object]()
        underlineProperties.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)
        editStateMachineLbl.setFont(editStateMachineLbl.getFont.deriveFont(underlineProperties))

        val upperButtonPanel = new JPanel(new GridBagLayout())
        //val buttonGroup: ButtonGroup = new ButtonGroup()
        addStateButton = new JToggleButton("Add State")
        addStateButton.addActionListener(editorController.addStateListener)
        addTransitionButton = new JToggleButton("Add Transition")
        addTransitionButton.addActionListener(editorController.addTransitionListener)
        setStartButton = new JToggleButton("Set Starting State")
        setStartButton.addActionListener(editorController.setStartListener)

        //buttonGroup.add(addStateButton)
        //buttonGroup.add(addTransitionButton)
        //buttonGroup.add(setStartButton)

        stateMachineDetailCardLayout = new CardLayout(10, 10)
        stateMachineDetailPanel = new JPanel(stateMachineDetailCardLayout)
        stateMachineDetailPanel.setVisible(false)
        val nodeEditPanel = setupNodeEditPanel()

        val transitionEditPanel = setupTransitionEditPanel()

        stateMachineDetailPanel.add(transitionEditPanel, CARD_DETAIL_TRANSITION_EDIT)
        stateMachineDetailPanel.add(nodeEditPanel, CARD_DETAIL_NODE_EDIT)

        val buttonPanel = new JPanel(new GridBagLayout())
        val doneButton = new JButton("Done")
        doneButton.addActionListener(editorController.stateMachineDoneListener)
        Util.addToGridBag(editStateMachineLbl, innerPanel, 0, 0, 3, 1, 0, 0, new Insets(0, 5, 0, 5))

        Util.addToGridBag(addStateButton, upperButtonPanel, 0, 0, 1, 1, 1, 0, new Insets(5, 20, 5, 5))
        Util.addToGridBag(addTransitionButton, upperButtonPanel, 1, 0, 1, 1, 1, 0, new Insets(5, 5, 5, 5))
        Util.addToGridBag(setStartButton, upperButtonPanel, 0, 1, 2, 1, 1, 0, new Insets(5, 20, 5, 5))

        Util.addToGridBag(upperButtonPanel, innerPanel, 1, 1, 2, 1, 1, 0, new Insets(0, 0, 0, 0))
        Util.addToGridBag(stateMachineDetailPanel, innerPanel, 1, 2, 2, 1, 1, 0, new Insets(0, 0, 0, 0))
        Util.addToGridBag(buttonPanel, innerPanel, 0, 5, 3, 1, 1, 0, new Insets(0, 0, 0, 0))

        Util.addToGridBag(doneButton, buttonPanel, 0, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(5, 5, 5, 5), 0, 0)

        Util.addToGridBag(innerPanel, panel, 0, 0, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0,0)
    }

    def setupNodeEditPanel(): JPanel =
    {
        val panel = new JPanel(new GridBagLayout())
        panel.setBorder(new TitledBorder(new LineBorder(Color.black), "Edit State Node:"))
        val nodeNameLbl = new JLabel("Name:")
        nodeNameInput = new JTextField("")
        nodeNameInput.getDocument.addDocumentListener(editorController.stateNodeNameChangeListener)

        val glueStateLbl = new JLabel("Glue States:")
        val addGlueStatePanel = new JPanel(new GridBagLayout())
        newGlueStateGlue = new JComboBox[String](Array[String]("North", "East", "South", "West"))
        newGlueStateState = new JComboBox[String](GlueState.values.toVector.sorted.map(state => state.toString).toArray)
        val addGlueStateButton = new JButton("Add")
        addGlueStateButton.addActionListener(editorController.glueStateAddListener)

        chosenGlueStates = new JTable(new ReadOnlyTableModel(Array[Array[AnyRef]](Array("", "")), Array[AnyRef]("Glue", "Glue State")))
        chosenGlueStates.addKeyListener(editorController.glueStatesKeyListener)
        val tableScrollPane = new JScrollPane(chosenGlueStates)
        tableScrollPane.setMinimumSize(new Dimension(tableScrollPane.getMinimumSize.getWidth.asInstanceOf[Int], 50))

        val buttonPanel = new JPanel(new GridBagLayout())
        val doneButton = new JButton("Done")
        doneButton.addActionListener(editorController.stateMachineDetailDoneListener)

        //Util.addToGridBag(nodeEditLbl, panel, 0, 0, 3, 1, 0, 0, new Insets(0, 5, 0, 5))
        Util.addToGridBag(nodeNameLbl, panel, 1, 1, 2, 1, 0, 0, new Insets(5, 5, 0, 5))
        Util.addToGridBag(nodeNameInput, panel, 1, 2, 2, 1, 1, 0, new Insets(5, 10, 5, 5))

        Util.addToGridBag(glueStateLbl, addGlueStatePanel, 0, 0, 2, 1, 1, 0, new Insets(0, 5, 0, 5))
        Util.addToGridBag(newGlueStateGlue, addGlueStatePanel, 0, 1, 1, 1, 1, 0, new Insets(0, 10, 0, 0))
        Util.addToGridBag(newGlueStateState, addGlueStatePanel, 1, 1, 1, 1, 1, 0, new Insets(0, 5, 0, 0))
        Util.addToGridBag(addGlueStateButton, addGlueStatePanel, 2, 1, 1, 1, 1, 0, new Insets(0, 5, 0, 0))

        Util.addToGridBag(addGlueStatePanel, panel, 1, 3, 2, 1, 1, 0, new Insets(5, 0, 5, 5))
        Util.addToGridBag(tableScrollPane, panel, 1, 4, 2, 1, 1, 0, new Insets(10, 5, 0, 5))

        //Util.addToGridBag(glueLabelLbl, panel, 1, 3, 2, 1, 0, 0, new Insets(5, 20, 0, 5))
        //Util.addToGridBag(glueLabelText, panel, 1, 4, 2, 1, 1, 0, new Insets(5, 25, 5, 5))
        Util.addToGridBag(buttonPanel, panel, 0, 5, 3, 1, 1, 0, new Insets(0, 0, 0, 0))
        Util.addToGridBag(doneButton, buttonPanel, 0, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(5, 5, 5, 5), 0, 0)

        return panel
    }

    def setupTransitionEditPanel(): JPanel =
    {
        val panel = new JPanel(new GridBagLayout())
        panel.setBorder(new TitledBorder(new LineBorder(Color.black), "Edit Transition:"))
        val transitionOnLbl = new JLabel("Transition On:")
        transitionOnInput = new JTextField("")
        transitionOnInput.getDocument.addDocumentListener(editorController.stateTransitionOnChangeListener)
        val probabilityLbl = new JLabel("Weighted Probability")
        probabilityInput = new JTextField("0.00")
        probabilityInput.setInputVerifier(new DecimalValidator(panel, probabilityInput, "Please enter a valid probability", 5, 0, Float.MaxValue))
        probabilityInput.getDocument.addDocumentListener(editorController.stateTransitionProbabilityChangeListener)

        val buttonPanel = new JPanel(new GridBagLayout())
        val doneButton = new JButton("Done")
        doneButton.addActionListener(editorController.stateMachineDetailDoneListener)

        //Util.addToGridBag(nodeEditLbl, panel, 0, 0, 3, 1, 0, 0, new Insets(0, 5, 0, 5))
        Util.addToGridBag(transitionOnLbl, panel, 1, 1, 2, 1, 0, 0, new Insets(5, 5, 0, 5))
        Util.addToGridBag(transitionOnInput, panel, 1, 2, 2, 1, 1, 0, new Insets(5, 10, 5, 5))
        Util.addToGridBag(probabilityLbl, panel, 1, 3, 2, 1, 0, 0, new Insets(5, 5, 0, 5))
        Util.addToGridBag(probabilityInput, panel, 1, 4, 2, 1, 1, 0, new Insets(5, 10, 5, 5))
        Util.addToGridBag(buttonPanel, panel, 0, 5, 3, 1, 1, 0, new Insets(0, 0, 0, 0))
        Util.addToGridBag(doneButton, buttonPanel, 0, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(5, 5, 5, 5), 0, 0)

        return panel
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
        return drawPanelEventHandler
    }

    override def getDrawPanelEventHandler(panel: GLJPanel): DrawPanelEventHandler =
    {
        if(drawPanelEventHandler == null)
        {
            drawPanelEventHandler = new EditorDrawPanelEventHandler(tileset, panel)
        }
        return drawPanelEventHandler
    }

    def setTileset(tileset: (Tile, scala.collection.immutable.List[Tile])): Unit =
    {
        setTileset(tileset, true)
    }

    def setTileset(tileset: (Tile, scala.collection.immutable.List[Tile]), clearActiveTile: Boolean): Unit =
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
        if(this.drawPanelEventHandler != null) this.drawPanelEventHandler.activeTile = tile

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

    override def setActiveMenu(activeMenu: MenuItem): Unit =
    {
        super.setActiveMenu(activeMenu)
        editingStateMachine = false
        editorController.updateEditingState(EditorState.Default)
    }
}
