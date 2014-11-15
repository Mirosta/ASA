package com.ASA.GUI

import java.awt.event.{WindowEvent, WindowListener}
import java.awt.{Color, GridLayout}
import javax.media.opengl.{GLCapabilities, GLProfile}
import javax.media.opengl.awt.GLJPanel
import javax.swing.{JFrame, JPanel}

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

        this.setSize(500,500)
        this.setVisible(true)

        val mainPanel = new JPanel()
        mainPanel.setLayout(new GridLayout(1,1))
        this.setLayout(new GridLayout(1,1))
        this.add(mainPanel)
        setupDrawPanel(mainPanel)

        addWindowListener(this)
        println("GUI Setup Complete")
        if(onSetup != null) onSetup.run()
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

        owningPanel.add(panel)
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
