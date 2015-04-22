package com.tw10g12.ASA.GUI

import java.awt._
import java.awt.image.BufferedImage
import javax.media.opengl._
import javax.swing._
import javax.swing.border.{Border, LineBorder}

import com.tw10g12.ASA.Controller.TilesetPanelController
import com.tw10g12.ASA.GUI.Draw.{RenderATAMTile, RenderMain}
import com.tw10g12.ASA.Model.StateMachine.StateMachine
import com.tw10g12.ASA.Model.Tile
import com.tw10g12.ASA.Util
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil
import com.tw10g12.Draw.Engine.{Colour, Camera, OrbitCamera, DrawTools}

import scala.collection.immutable.List
import com.tw10g12.ASA.Util._


/**
 * Created by Tom on 05/02/2015.
 */
class TilesetPanel(tileIconSize: Int, noTilesX: Int) extends JPanel
{

    var images: scala.collection.mutable.Map[Tile, BufferedImage] = new scala.collection.mutable.HashMap[Tile, BufferedImage]()
    var tiles: (Tile, List[Tile]) = (null, List[Tile]())
    var stateMachines: Map[Tile, StateMachine] = Map()

    var tileViewer: JPanel = null
    lazy val drawable: GLAutoDrawable = setupOffscreenImage()
    var tileX = 0
    var tileY = 0
    var setupComplete = false
    var controller: TilesetPanelController = null

    def setTiles(tiles: (Tile, List[Tile]))
    {
        this.tiles = tiles
        if(setupComplete)
        {
            setupTileViewer()
            updateImageCache()
        }
    }

    def updateImageCache(): Unit =
    {
        val tileSet: Set[Tile] = Set[Tile](tiles._1) ++ tiles._2
        images = images.filter(tileImage => tileSet.contains(tileImage._1))
    }

    def setup(buttonText: String, controller: TilesetPanelController): Unit =
    {
        this.setLayout(new GridBagLayout())
        this.controller = controller

        tileViewer = new JPanel(new GridBagLayout())
        val scrollable = new JScrollPane(tileViewer)
        setupTileViewer()

        val editButton = new JButton(buttonText)
        editButton.addActionListener(controller.editButtonListener)

        Util.addToGridBag(scrollable, this, 1, 1, 1.0, 1.0, new Insets(5, 5, 5, 5))
        Util.addToGridBag(editButton, this, 1, 2, 1.0, 0.0, new Insets(5, 5, 5, 5))
        setupComplete = true
    }

    def setupTileViewer(): Unit =
    {
        tileX = 0
        tileY = 0
        tileViewer.removeAll()
        setupTile(tiles._1, true)
        tiles._2.map(tile => setupTile(tile, false))
        tileViewer.repaint()
    }

    def getSeedBorder(): Border = new LineBorder(Color.GREEN)
    def getBlankBorder(): Border = new LineBorder(new Color(0,0,0,0))

    protected def setupTile(tile: Tile, seedTile: Boolean): Unit =
    {
        if(tile == null) return

        val image: Image = getTileImage(tile)
        //ImageIO.write(image.asInstanceOf[RenderedImage], "png", new File(tileX + "," + tileY + ".png"))
        val imageLabel: JLabel = new JLabel(new ImageIcon(image))
        if(seedTile) imageLabel.setBorder(getSeedBorder)
        else imageLabel.setBorder(getBlankBorder)
        imageLabel.setMinimumSize(new Dimension(tileIconSize, tileIconSize))
        imageLabel.setName((tileY * noTilesX + tileX - 1).toString)
        imageLabel.addMouseListener(controller.tileMouseListener)

        Util.addToGridBag(imageLabel, tileViewer, tileX, tileY, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0)
        tileX += 1
        if(tileX >= noTilesX)
        {
            tileX = 0
            tileY += 1
        }
    }

    protected def getTileImage(tile: Tile): Image =
    {
        if(!images.contains(tile))
        {
            images.put(tile, renderTileImage(tile))
        }
        return images(tile)
    }
    var currentTile: Tile = null
    var currentTileIcon: BufferedImage = null

    protected def renderTileImage(tile: Tile): BufferedImage =
    {
        currentTile = tile
        drawable.display()
        val tileIcon: BufferedImage = currentTileIcon
        return tileIcon
    }

    class OffscreenDrawableEventListener extends GLEventListener
    {
        var drawTools: DrawTools = null
        var camera: Camera = new OrbitCamera(10)

        override def init(glAutoDrawable: GLAutoDrawable): Unit =
        {
            drawable.setGL(new DebugGL3(drawable.getGL().getGL3()))
            val gl3: GL3 = glAutoDrawable.getGL().getGL3()
            drawTools = JOGLUtil.getDrawTools(gl3)
            drawTools.setupPerspectiveProjection(drawable.getWidth, drawable.getHeight)
            gl3.glEnable(GL.GL_DEPTH_TEST)
            gl3.glDepthFunc(GL.GL_LESS)
            gl3.glEnable(GL.GL_BLEND)
            gl3.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA)
        }

        override def display(glAutoDrawable: GLAutoDrawable): Unit =
        {
            val gl3: GL3 = glAutoDrawable.getGL().getGL3()
            drawTools.setGL3(gl3)
            val shouldDraw = currentTile != null
            //gl3.glViewport(0, 0, tileIconSize, tileIconSize)
            if(shouldDraw)
            {
                RenderMain.before(drawTools, camera, new Colour(0.0f, 0.0f, 0.0f, 0.0f))
                RenderATAMTile.renderTile(currentTile, 6, drawTools)
                RenderATAMTile.afterRender(currentTile, currentTile.getPosition, 6, false, null, drawTools)
                RenderMain.after(drawTools)

                currentTileIcon = new AWTGLReadBufferUtil(drawable.getGLProfile(), true).readPixelsToBufferedImage(drawable.getGL(), 0, 0, tileIconSize, tileIconSize, true)
            }
        }

        override def reshape(glAutoDrawable: GLAutoDrawable, i: Int, i1: Int, i2: Int, i3: Int): Unit =
        {
            drawTools.setupPerspectiveProjection(drawable.getWidth, drawable.getHeight)
        }

        override def dispose(glAutoDrawable: GLAutoDrawable): Unit = {}
    }

    //http://forum.jogamp.org/Render-offscreen-buffer-to-image-td4032144.html
    protected def setupOffscreenImage(): GLAutoDrawable =
    {
        val glp: GLProfile = JOGLUtil.getOpenGLProfile()
        val caps: GLCapabilities = new GLCapabilities(glp)
        caps.setHardwareAccelerated(true)
        caps.setDoubleBuffered(false)
        caps.setAlphaBits(8)
        caps.setRedBits(8)
        caps.setBlueBits(8)
        caps.setGreenBits(8)
        caps.setOnscreen(false)

        val factory: GLDrawableFactory = GLDrawableFactory.getFactory(glp)
        val drawable: GLAutoDrawable = factory.createOffscreenAutoDrawable(factory.getDefaultDevice(), caps, new DefaultGLCapabilitiesChooser(), tileIconSize, tileIconSize)
        val context = drawable.createContext(null)
        drawable.setContext(context, true)
        drawable.addGLEventListener(new OffscreenDrawableEventListener())
        return drawable
    }
}
