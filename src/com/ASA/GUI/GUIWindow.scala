package com.ASA.GUI

import java.awt._
import java.awt.event.{WindowEvent, WindowListener}
import javax.media.opengl.awt.GLJPanel
import javax.media.opengl.{GLCapabilities, GLProfile}
import javax.swing.border.{LineBorder, MatteBorder}
import javax.swing.{JPanel, SwingConstants}

import com.ASA.Controller.{HasMenu, MenuController, SimulationController}
import com.ASA.Util._
import com.jogamp.opengl.util.FPSAnimator

/**
 * Created by Tom on 20/10/2014.
 */
class GUIWindow(val simulationController: SimulationController) extends javax.swing.JFrame with WindowListener with HasMenu
{
    val menuController: MenuController = new MenuController(this)

    var activeMenuItem: MenuItem = null
    var menuItems: Vector[MenuItem] = Vector[MenuItem]()
    var currentMenuPanel: JPanel = null
    var otherMenusPanel: JPanel = null

    var contentPanel: JPanel = null
    var cardLayout: CardLayout = null
    val CARD_TILE_EDITOR = "tile"
    val CARD_SIMULATION = "simulation"
    val CARD_SETTINGS = "settings"

    //Constructor
    def startSetup(): Unit =
    {
        javax.swing.SwingUtilities.invokeLater( toRunnable(setup) )
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

    def setupMenuPanel(menuPanel: JPanel): Unit =
    {
        menuPanel.setPreferredSize(new Dimension(250, 0))
        menuPanel.setBorder(new MatteBorder(0, 0, 0, 1, Color.GRAY))

        currentMenuPanel = new JPanel(new GridBagLayout())
        val currentMenuButton = createMenuItem("Tile Editor", CARD_TILE_EDITOR)

        cardLayout = new CardLayout()
        contentPanel = new JPanel(cardLayout)

        otherMenusPanel = new JPanel(new GridBagLayout())
        val otherMenuButtons = scala.collection.immutable.List[MenuItem](createMenuItem("Simulation", CARD_SIMULATION), createMenuItem("Settings", CARD_SETTINGS))

        addToGridBag(currentMenuButton, otherMenusPanel, 1, 1, 1.0, 0.0)
        addToGridBag(otherMenuButtons(0), otherMenusPanel, 1, 2, 1.0, 0.0)
        addToGridBag(otherMenuButtons(1), otherMenusPanel, 1, 3, 1.0, 0.0)

        contentPanel.setBorder(new MatteBorder(1,0, 1, 0, Color.GRAY))

        setupTileEditorPanel()
        setupSimulationPanel()
        setupSettingsPanel()

        addToGridBag(currentMenuPanel, menuPanel, 1,1, 1.0, 0.0)
        addToGridBag(contentPanel, menuPanel, 1,2, 1.0, 1.0)
        addToGridBag(otherMenusPanel, menuPanel, 1,3, 1.0, 0.0)

        setActiveMenu(otherMenuButtons(0))
    }

    def setupTileEditorPanel(): Unit =
    {
        val panel = new JPanel()
        panel.setBackground(Color.RED)
        contentPanel.add(panel, CARD_TILE_EDITOR)
    }

    def setupSimulationPanel(): Unit =
    {
        val panel = new JPanel()
        val simulationPanel = new SimulationPanel(simulationController)
        simulationPanel.setup()

        addToGridBag(simulationPanel, panel, 1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0)
        contentPanel.add(panel, CARD_SIMULATION)
    }

    def setupSettingsPanel(): Unit =
    {
        val panel = new JPanel()
        panel.setBackground(Color.BLUE)
        contentPanel.add(panel, CARD_SETTINGS)
    }

    def createMenuItem(name: String, cardPanel: String): MenuItem =
    {
        val menuButton = new MenuItem(cardPanel, menuItems.length)
        menuButton.setText(name)
        menuButton.setBackground(Color.WHITE)
        menuButton.setOpaque(true)
        menuButton.setBorder(new LineBorder(Color.GRAY, 1))
        menuButton.setPreferredSize(new Dimension(250,32))
        menuButton.setFont(menuButton.getFont.deriveFont(15f))
        menuButton.setHorizontalAlignment(SwingConstants.CENTER)
        menuButton.addMouseListener(menuController.menuItemListener)
        menuItems = menuItems.+:(menuButton)

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

    override def setActiveMenu(activeMenu: MenuItem): Unit =
    {
        if(activeMenu != this.activeMenuItem)
        {
            if(this.activeMenuItem != null)
            {
                currentMenuPanel.remove(this.activeMenuItem)
                addToGridBag(this.activeMenuItem, otherMenusPanel, 1, this.activeMenuItem.index + 1, 1.0, 0.0)
            }

            otherMenusPanel.remove(activeMenu)
            addToGridBag(activeMenu, currentMenuPanel, 1, 1, 1.0, 0.0)
            this.activeMenuItem = activeMenu
            otherMenusPanel.repaint()
            currentMenuPanel.repaint()

            cardLayout.show(contentPanel, activeMenu.cardPanel)
        }
    }

    override def getActiveMenu(): MenuItem = this.activeMenuItem

    override def getMenuItems(): Vector[MenuItem] = this.menuItems
}
