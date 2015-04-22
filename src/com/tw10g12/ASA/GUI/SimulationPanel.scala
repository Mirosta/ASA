package com.tw10g12.ASA.GUI

import java.awt.font.TextAttribute
import java.awt._
import javax.swing._
import javax.swing.border.{TitledBorder, LineBorder}

import com.tw10g12.ASA.Controller.{SimulationController, SimulationPanelController}
import com.tw10g12.ASA.Util
import com.tw10g12.Validation.DecimalValidator

/**
 * Created by Tom on 01/12/2014.
 */
class SimulationPanel(simulation: SimulationController) extends JPanel(new GridBagLayout())
{
    val controller = new SimulationPanelController(simulation, this)

    var startButton: JButton = null
    var stopButton: JButton = null
    var saveButton: JButton = null
    var loadButton: JButton = null
    var showHideButton: JButton = null

    var simulationMode: JComboBox[String] = null
    var simulationModeOptions: CardLayout = null
    var simulationModeOptionsPanel: JPanel = null

    var simulationSpeedSlider: JSlider = null

    var forwardConstantInput: JTextField = null
    var reverseConstantInput: JTextField = null

    var checkConnected: JCheckBox = null

    var simulationEditOptionsPanel: JPanel = null
    var changeTileTypeCombo: JComboBox[ImageIcon] = null

    def setup(): Unit =
    {
        val upperButtonPanel = new JPanel(new GridBagLayout())
        val lowerButtonPanel = new JPanel(new GridBagLayout())
        startButton = new JButton("Start")
        stopButton = new JButton("Stop")
        saveButton = new JButton("<html><center>Save <p>Simulation</p></center></html>")
        loadButton = new JButton("<html><center>Load <p>Simulation</p></center></html>")
        showHideButton = new JButton("Show Statistics")

        val simulationSpeedLabel = new JLabel("Simuation Speed:")
        simulationSpeedSlider = new JSlider(1, 100)
        simulationSpeedSlider.setBorder(new LineBorder(Color.gray))
        simulationSpeedSlider.addChangeListener(controller.speedChangedListener)

        val simulationModelLabel = new JLabel("Simulation Model:")
        simulationMode = new JComboBox[String](Array[String]("ATAM", "KTAM", "SMTAM"))
        simulationMode.addActionListener(controller.modeSelectListener)
        stopButton.setEnabled(false)

        simulationModeOptions = new CardLayout()
        simulationModeOptionsPanel = new JPanel(simulationModeOptions)

        simulationModeOptionsPanel.add(setupATAMOptions(), "ATAM")
        simulationModeOptionsPanel.add(setupKTAMOptions(), "KTAM")
        simulationModeOptionsPanel.add(setupSMTAMOptions(), "SMTAM")

        startButton.addActionListener(controller.startListener)
        stopButton.addActionListener(controller.stopButtonListener)
        saveButton.addActionListener(controller.saveSimulationListener)
        loadButton.addActionListener(controller.loadSimulationListener)
        showHideButton.addActionListener(controller.showHideButtonListener)

        simulationEditOptionsPanel = setupEditPanel()

        Util.addToGridBag(startButton, upperButtonPanel, 1, 1, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0)
        Util.addToGridBag(stopButton, upperButtonPanel, 2, 1, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0)
        Util.addToGridBag(upperButtonPanel, this, 1, 1, 2, 1, 1.0, 1.0, new Insets(5, 0, 5, 0))

        Util.addToGridBag(simulationSpeedLabel, this, 1, 3, 2, 1, 0.0, 1.0, new Insets(10, 5, 0, 5))
        Util.addToGridBag(simulationSpeedSlider, this, 1, 4, 2, 1, 0.0, 1.0, new Insets(5, 5, 0, 5))

        Util.addToGridBag(saveButton, lowerButtonPanel, 1, 1, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0)
        Util.addToGridBag(loadButton, lowerButtonPanel, 2, 1, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0)
        Util.addToGridBag(showHideButton, lowerButtonPanel, 1, 2, 2, 1, 0.0, 1.0, new Insets(5, 5, 0, 5))
        Util.addToGridBag(lowerButtonPanel, this, 1, 5, 2, 1, 1.0, 1.0, new Insets(5, 0, 0, 0))

        Util.addToGridBag(simulationModelLabel, this, 1, 6, 2, 1, 0.0, 1.0, new Insets(10, 0, 0, 0))
        Util.addToGridBag(simulationMode, this, 1, 7, 2, 1, 0, 1.0, new Insets(5, 5, 0, 5))
        Util.addToGridBag(simulationModeOptionsPanel, this, 1, 8, 2, 1, 0, 1.0, new Insets(5, 0, 0, 0))
        Util.addToGridBag(simulationEditOptionsPanel, this, 1, 9, 2, 1, 0, 1.0, new Insets(5, 0, 0, 0))
    }

    def setupATAMOptions(): JPanel =
    {
        return new JPanel()
    }

    def setupKTAMOptions(): JPanel =
    {
        val panel = new JPanel(new GridBagLayout())

        val panelLabel = new JLabel("KTAM Options:")
        val underlineProperties = new java.util.HashMap[TextAttribute, Object]()
        underlineProperties.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)
        panelLabel.setFont(panelLabel.getFont.deriveFont(underlineProperties))

        val forwardConstantLabel = new JLabel("Forward Constant:")
        forwardConstantInput = new JTextField("0")
        forwardConstantInput.setInputVerifier(new DecimalValidator(this, forwardConstantInput, "Please enter a number", 4, 0, 100))
        val reverseConstantLabel = new JLabel("Reverse Constant:")
        reverseConstantInput = new JTextField("0")
        reverseConstantInput.setInputVerifier(new DecimalValidator(this, reverseConstantInput, "Please enter a number", 4, 0, 100))

        forwardConstantInput.getDocument.addDocumentListener(controller.KTAMConstantsChanged)
        reverseConstantInput.getDocument.addDocumentListener(controller.KTAMConstantsChanged)

        Util.addToGridBag(panelLabel, panel, 0, 0, 2, 1, 1.0, 0, new Insets(5, 0, 0, 5))
        Util.addToGridBag(forwardConstantLabel, panel, 1, 1, 1, 1, 1, 0, new Insets(5, 10, 0, 5))
        Util.addToGridBag(forwardConstantInput, panel, 1, 2, 1, 1, 1, 0, new Insets(5, 10, 0, 5))
        Util.addToGridBag(reverseConstantLabel, panel, 1, 3, 1, 1, 1, 0, new Insets(5, 10, 0, 5))
        Util.addToGridBag(reverseConstantInput, panel, 1, 4, 1, 1, 1, 0, new Insets(5, 10, 0, 5))

        return panel
    }

    def setupSMTAMOptions(): JPanel =
    {
        val panel = new JPanel(new GridBagLayout())

        val panelLabel = new JLabel("SMTAM Options:")
        checkConnected = new JCheckBox("Check Connected", true)
        checkConnected.addActionListener(controller.connectedListener)
        var fillerPanel = new JPanel()

        Util.addToGridBag(panelLabel, panel, 0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 5), 0, 0)
        Util.addToGridBag(checkConnected, panel, 0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 5), 0, 0)
        Util.addToGridBag(fillerPanel, panel, 0, 2, 1, 1, 1.0, 1.0)

        return panel
    }

    def setupEditPanel(): JPanel =
    {
        val panel = new JPanel(new GridBagLayout())
        panel.setVisible(false)
        panel.setBorder(new TitledBorder(new LineBorder(Color.black), "Edit Simulation - (x, y, z)"))

        changeTileTypeCombo = new JComboBox[ImageIcon]()
        val changeTileButton = new JButton("Change")
        changeTileButton.addActionListener(controller.changeTileListener)
        val removeTileButton = new JButton("Remove")
        removeTileButton.addActionListener(controller.removeTileListener)

        Util.addToGridBag(changeTileTypeCombo, panel, 1, 1, 1, 1, 0, 1, new Insets(5, 5, 5, 5))
        Util.addToGridBag(changeTileButton, panel, 2, 1, 1, 1, 0, 0, new Insets(5, 5, 5, 5))
        Util.addToGridBag(removeTileButton, panel, 1, 2, 2, 1, 0, 0, new Insets(5, 5, 5, 5))

        return panel
    }
}
