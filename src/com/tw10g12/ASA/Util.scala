package com.tw10g12.ASA

import java.awt.{Color, Component, Container, GridBagConstraints, Insets}
import javax.media.opengl.glu.GLU
import javax.media.opengl.{GL3, GLProfile}

import com.jogamp.opengl.util.gl2.GLUT
import com.tw10g12.ASA.Model.JSON.JSONTileFactory
import com.tw10g12.ASA.Model.Tile
import com.tw10g12.Draw.Engine.{Colour, DrawTools, ShaderLoader}
import com.tw10g12.Maths.Vector3
import org.json.{JSONArray, JSONObject}

import scala.actors.threadpool.Executors
import scala.collection.mutable

/**
 * Created by Tom on 20/10/2014.
 */
object Util
{

    lazy val threadPool = Executors.newFixedThreadPool(5)

    def toRunnable(fun : () => Unit) : Runnable =
    {
        return new Runnable
        {
            override def run(): Unit =
            {
                fun()
            }
        }

    }

    def convertColor(color: Color): Colour =
    {
        val c = new Colour(color.getRed, color.getGreen, color.getBlue, color.getAlpha)
        return c
    }

    def convertColour(colour: Colour): Color =
    {
        val c = new Color(colour.getR,colour.getG,colour.getB, colour.getA)
        return c
    }

    def orientationToVector(orientation: Int): Vector3 =
    {
        orientation match
        {
            case 0 => return new Vector3(0, 1, 0)//North
            case 1 => return new Vector3(-1, 0, 0) //East
            case 2 => return new Vector3(0, -1, 0) //South
            case 3 => return new Vector3(1, 0, 0)//West
            case 4 => return new Vector3(0, 0, -1)//Up
            case 5 => return new Vector3(0, 0, 1) //Down
        }
        throw new IllegalArgumentException("Unknown orientation " + orientation)
    }

    def vectorToOrientation(vector: Vector3): Int =
    {
        val vectorMap = Map(new Vector3(0, 1, 0) -> 0, new Vector3(-1, 0, 0) -> 1, new Vector3(0, -1, 0) -> 2, new Vector3(1, 0, 0) -> 3, new Vector3(0, 0, -1) -> 4, new Vector3(0, 0, 1) -> 5);
        if(!vectorMap.contains(vector)) throw new IllegalArgumentException("Unknown orientation vector " + vector)
        return vectorMap(vector)
    }

    def oppositeOrientation(orientation: Int): Int =
    {
        orientation match
        {
            case 0 => return 2 //North
            case 1 => return 3 //East
            case 2 => return 0 //South
            case 3 => return 1 //West
            case 4 => return 5 //Up
            case 5 => return 4 //Down
        }
        throw new IllegalArgumentException("Unknown orientation " + orientation)
    }


    object JOGLUtil
    {

        lazy val glu: GLU = new GLU()
        lazy val glut: GLUT = new GLUT()

        def getOpenGLProfile(): GLProfile =
        {
            return GLProfile.get(GLProfile.GL3)
        }

        def getShaders(): List[ShaderLoader] =
        {
            val shaderLoader: ShaderLoader = new ShaderLoader("shaders/main")
            val instancedShader: ShaderLoader = new ShaderLoader("shaders/mainInstanced")
            return List(shaderLoader, shaderLoader, instancedShader)
        }
        def getGLU(): GLU = glu
        def getGLUT(): GLUT = glut
        def getDrawTools(gl3: GL3): DrawTools =
        {
            import scala.collection.JavaConverters._
            return new DrawTools(gl3, JOGLUtil.getGLU(), JOGLUtil.getGLUT(), getShaders.asJava)
        }
    }

    object IOUtil
    {
        def tilesetToJSON(tileset: (Tile, List[Tile])): JSONObject =
        {
            val outputObj = new JSONObject()
            outputObj.put("seed", tileset._1.toJSON(new JSONObject()))
            outputObj.put("tiles", new JSONArray(tileset._2.map(tile => tile.toJSON(new JSONObject())).toArray))
            return outputObj
        }

        def JSONtoTileset(serialized: JSONObject): (Tile, List[Tile]) =
        {
            val seed: Tile = JSONTileFactory.createTile(serialized.getJSONObject("seed"))
            val tiles: List[Tile] = JSONArrayToArray[JSONObject](serialized.getJSONArray("tiles")).map(serializedTile => JSONTileFactory.createTile(serializedTile)).toList

            return (seed, tiles)
        }

        def colourToJSON(colour: Colour): JSONObject =
        {
            val outputObj = new JSONObject()
            outputObj.put("r", colour.getR)
            outputObj.put("g", colour.getG)
            outputObj.put("b", colour.getB)
            outputObj.put("a", colour.getA)
            return outputObj
        }

        def JSONToColour(inputObj: JSONObject): Colour =
        {
            val r: Float = if(inputObj.has("r")) inputObj.getDouble("r").asInstanceOf[Float] else 0
            val g: Float = if(inputObj.has("g")) inputObj.getDouble("g").asInstanceOf[Float] else 0
            val b: Float = if(inputObj.has("b")) inputObj.getDouble("b").asInstanceOf[Float] else 0
            val a: Float = if(inputObj.has("a")) inputObj.getDouble("a").asInstanceOf[Float] else 0

            return new Colour(r,g,b,a)
        }

        def vector3ToJSON(vec: Vector3): JSONObject =
        {
            val outputObj = new JSONObject()

            outputObj.put("x", vec.getX)
            outputObj.put("y", vec.getY)
            outputObj.put("z", vec.getZ)

            return outputObj
        }

        def JSONToVector3(inputObj: JSONObject): Vector3 =
        {
            val x: Double = if(inputObj.has("z")) inputObj.getDouble("x") else 0
            val y: Double = if(inputObj.has("y")) inputObj.getDouble("y") else 0
            val z: Double    = if(inputObj.has("z")) inputObj.getDouble("z") else 0

            return new Vector3(x, y, z)
        }

        def JSONMixedArrayToArray(array: JSONArray): mutable.MutableList[AnyRef] =
        {
            val outputArr = new mutable.MutableList[AnyRef]()
            (0 until array.length()).map(i => outputArr += array.get(i))
            return outputArr
        }

        def JSONArrayToArray[T](array: JSONArray): mutable.MutableList[T] =
        {
            val outputArr: mutable.MutableList[T] = new mutable.MutableList[T]()
            (0 until array.length()).map(i => outputArr += (if(array.get(i) == null) null.asInstanceOf[T] else array.get(i).asInstanceOf[T]))
            return outputArr
        }
    }



    def addToGridBag(c: Component, owner: Container, gridX: Int, gridY: Int, gridWidth: Int, gridHeight: Int, weightX: Double, weightY: Double, anchor: Int, fill: Int, insets: Insets, iPadX: Int, iPadY: Int): Unit =
    {
        owner.add(c, new GridBagConstraints(gridX, gridY, gridWidth, gridHeight, weightX, weightY, anchor, fill, insets, iPadX, iPadY))
    }

    def addToGridBag(c: Component, owner: Container, gridX: Int, gridY: Int): Unit =
    {
        addToGridBag(c, owner, gridX, gridY, 0.0, 0.0)
    }

    def addToGridBag(c: Component, owner: Container, gridX: Int, gridY: Int, gridWidth: Int, gridHeight: Int, weightX: Double, weightY: Double): Unit =
    {
        addToGridBag(c, owner, gridX, gridY, gridWidth, gridHeight, weightX, weightY, 0, 0)
    }

    def addToGridBag(c: Component, owner: Container, gridX: Int, gridY: Int, gridWidth: Int, gridHeight: Int, weightX: Double, weightY: Double, iPadX: Int, iPadY: Int): Unit =
    {
        addToGridBag(c, owner, gridX, gridY, gridWidth, gridHeight, weightX, weightY, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), iPadX, iPadY)
    }

    def addToGridBag(c: Component, owner: Container, gridX: Int, gridY: Int, weightX: Double, weightY: Double): Unit =
    {
        addToGridBag(c, owner, gridX, gridY, weightX, weightY, new Insets(0,0,0,0))
    }

    def addToGridBag(c: Component, owner: Container, gridX: Int, gridY: Int, weightX: Double, weightY: Double, insets: Insets): Unit =
    {
        addToGridBag(c, owner, gridX, gridY, 1, 1, weightX, weightY, insets)
    }

    def addToGridBag(c: Component, owner: Container, gridX: Int, gridY: Int, gridWidth: Int, gridHeight: Int, weightX: Double, weightY: Double, insets: Insets): Unit =
    {
        addToGridBag(c, owner, gridX, gridY, gridWidth, gridHeight, weightX, weightY, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0)
    }

}