package com.ASA.GUI

import java.awt._
import java.awt.event.{WindowEvent, WindowListener}
import javax.media.opengl.{GLCapabilities, GLProfile}
import javax.media.opengl.awt.GLJPanel
import javax.swing.border.{MatteBorder, LineBorder, Border}
import javax.swing.{SwingConstants, JLabel, JFrame, JPanel}

import com.ASA.Controller.SimulationController
import com.ASA.Util
import com.jogamp.opengl.util.FPSAnimator

/**
 * Created by Tom on 20/10/2014.
 */
class GUIWindow(val simulationController: SimulationController) extends javax.swing.JFrame with WindowListener
{
    //Constructor
    def startSetup(): Unit =
    {
        javax.swing.SwingUtilities.invokeLater( Util.toRunnable(setup) )
    }

    var onSetup: Runnable = null
    var onClosed: Runnable = null

    def setup(): Unit =
    {
        println("Starting GUI Setup")

        this.setSize(960,540)
        this.setVisible(true)

        val mainPanel = new JPanel(new GridBagLayout())

        val menuPanel = new JPanel(new GridBagLayout())
        setupMenuPanel(menuPanel)

        this.setLayout(new GridBagLayout())

        addToGridBag(mainPanel, this, 2, 1, 1.0, 1.0)
        addToGridBag(menuPanel, this, 1, 1, 0.0, 1.0)
        setupDrawPanel(mainPanel)

        addWindowListener(this)
        println("GUI Setup Complete")
        if(onSetup != null) onSetup.run()
    }

    def addToGridBag(c: Component, owner: Container, gridX: Int, gridY: Int, gridWidth: Int, gridHeight: Int, weightX: Double, weightY: Double, anchor: Int, fill: Int, insets: Insets, iPadX: Int, iPadY: Int): Unit =
    {
        owner.add(c, new GridBagConstraints(gridX, gridY, gridWidth, gridHeight, weightX, weightY, anchor, fill, insets, iPadX, iPadY))
    }

    def addToGridBag(c: Component, owner: Container, gridX: Int, gridY: Int): Unit =
    {
        addToGridBag(c, owner, gridX, gridY, 0.0, 0.0)
    }

    def addToGridBag(c: Component, owner: Container, gridX: Int, gridY: Int, weightX: Double, weightY: Double): Unit =
    {
        addToGridBag(c, owner, gridX, gridY, 1, 1, weightX, weightY, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0)
    }

    def setupMenuPanel(menuPanel: JPanel): Unit =
    {
        menuPanel.setPreferredSize(new Dimension(250, 0))
        menuPanel.setBorder(new MatteBorder(0, 0, 0, 1, Color.GRAY))

        val currentMenuPanel = new JPanel(new GridBagLayout())
        val currentMenuButton = createMenuItem("Tile Editor")
        addToGridBag(currentMenuButton, currentMenuPanel, 1, 1, 1.0, 0.0)

        val contentPanel = new JPanel(new CardLayout())

        val otherMenusPanel = new JPanel(new GridBagLayout())
        val otherMenuButtons = scala.collection.immutable.List[JLabel](createMenuItem("Simulation"), createMenuItem("Settings"))

        addToGridBag(otherMenuButtons(0), otherMenusPanel, 1, 1, 1.0, 0.0)
        addToGridBag(otherMenuButtons(1), otherMenusPanel, 1, 2, 1.0, 0.0)

        contentPanel.setBorder(new MatteBorder(1,0, 1, 0, Color.GRAY))

        addToGridBag(currentMenuPanel, menuPanel, 1,1, 1.0, 0.0)
        addToGridBag(contentPanel, menuPanel, 1,2, 1.0, 1.0)
        addToGridBag(otherMenusPanel, menuPanel, 1,3, 1.0, 0.0)
    }

    def createMenuItem(name: String): JLabel =
    {
        val menuButton = new JLabel(name)
        menuButton.setBackground(Color.WHITE)
        menuButton.setOpaque(true)
        menuButton.setBorder(new LineBorder(Color.GRAY, 1))
        menuButton.setPreferredSize(new Dimension(250,32))
        menuButton.setFont(menuButton.getFont.deriveFont(15f))
        menuButton.setHorizontalAlignment(SwingConstants.CENTER)

        return menuButton
    }

    def setupDrawPanel(owningPanel: JPanel): Unit =
    {
        val caps = new GLCapabilities(GLProfile.get(GLProfile.GL3))
        val panel = new GLJPanel(caps)
        val animator = new FPSAnimator(panel, 60)
        animator.start()

        val eventHandler: DrawPanelEventHandler = new DrawPanelEventHandler(simulationController)
        panel.addGLEventListener(eventHandler)
        panel.addMouseListener(eventHandler)
        panel.addMouseMotionListener(eventHandler)
        panel.addMouseWheelListener(eventHandler)

        addToGridBag(panel, owningPanel, 1, 1, 1.0, 1.0)
    }

    override def windowOpened(e: WindowEvent): Unit = {}

    override def windowDeiconified(e: WindowEvent): Unit = {}

    override def windowClosing(e: WindowEvent): Unit =
    {
        if(onClosed != null) onClosed.run()
        dispose()
        System.exit(0)
    }

    override def windowClosed(e: WindowEvent): Unit =
    {
    }

    override def windowActivated(e: WindowEvent): Unit = {}

    override def windowDeactivated(e: WindowEvent): Unit = {}

    override def windowIconified(e: WindowEvent): Unit = {}
}
