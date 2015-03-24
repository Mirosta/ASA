package com.tw10g12.ASA.GUI

import java.awt.font.TextAttribute
import java.awt.{CardLayout, GridBagLayout, Insets}
import javax.swing._

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
    var simulationMode: JComboBox[String] = null
    var simulationModeOptions: CardLayout = null
    var simulationModeOptionsPanel: JPanel = null
    var forwardConstantInput: JTextField = null
    var reverseConstantInput: JTextField = null
    var simulationSpeedSlider: JSlider = null

    def setup(): Unit =
    {
        startButton = new JButton("Start")
        stopButton = new JButton("Stop")
        val simulationSpeedLabel = new JLabel("Simuation Speed:")
        simulationSpeedSlider = new JSlider(1, 100)
        simulationSpeedSlider.addChangeListener(controller.speedChangedListener)

        val simulationModelLabel = new JLabel("Simulation Model:")
        simulationMode = new JComboBox[String](Array[String]("ATAM", "KTAM"))
        simulationMode.addActionListener(controller.modeSelectListener)
        stopButton.setEnabled(false)

        simulationModeOptions = new CardLayout()
        simulationModeOptionsPanel = new JPanel(simulationModeOptions)

        simulationModeOptionsPanel.add(setupATAMOptions(), "ATAM")
        simulationModeOptionsPanel.add(setupKTAMOptions(), "KTAM")

        startButton.addActionListener(controller.startListener)
        stopButton.addActionListener(controller.stopButtonListener)

        Util.addToGridBag(startButton, this, 1, 1, 1, 1, 0.0, 1.0, new Insets(0, 5, 0, 5))
        Util.addToGridBag(stopButton, this, 2, 1, 1, 1, 0.0, 1.0, new Insets(0, 5, 0, 5))
        Util.addToGridBag(simulationSpeedLabel, this, 1, 3, 2, 1, 0.0, 1.0, new Insets(10, 5, 0, 5))
        Util.addToGridBag(simulationSpeedSlider, this, 1, 4, 2, 1, 0.0, 1.0, new Insets(5, 5, 0, 5))
        Util.addToGridBag(simulationModelLabel, this, 1, 5, 2, 1, 0.0, 1.0, new Insets(10, 0, 0, 0))
        Util.addToGridBag(simulationMode, this, 1, 6, 2, 1, 0, 1.0, new Insets(5, 0, 0, 0))
        Util.addToGridBag(simulationModeOptionsPanel, this, 1, 7, 2, 1, 0, 1.0, new Insets(5, 0, 0, 0))
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
        forwardConstantInput.setInputVerifier(new DecimalValidator(this, forwardConstantInput, "Please enter a number", 4, 0, 1))
        val reverseConstantLabel = new JLabel("Reverse Constant:")
        reverseConstantInput = new JTextField("0")
        reverseConstantInput.setInputVerifier(new DecimalValidator(this, reverseConstantInput, "Please enter a number", 4, 0, 1))

        forwardConstantInput.getDocument.addDocumentListener(controller.KTAMConstantsChanged)
        reverseConstantInput.getDocument.addDocumentListener(controller.KTAMConstantsChanged)

        Util.addToGridBag(panelLabel, panel, 0, 0, 2, 1, 1.0, 0, new Insets(5, 0, 0, 5))
        Util.addToGridBag(forwardConstantLabel, panel, 1, 1, 1, 1, 1, 0, new Insets(5, 10, 0, 5))
        Util.addToGridBag(forwardConstantInput, panel, 1, 2, 1, 1, 1, 0, new Insets(5, 10, 0, 5))
        Util.addToGridBag(reverseConstantLabel, panel, 1, 3, 1, 1, 1, 0, new Insets(5, 10, 0, 5))
        Util.addToGridBag(reverseConstantInput, panel, 1, 4, 1, 1, 1, 0, new Insets(5, 10, 0, 5))

        return panel
    }
}
