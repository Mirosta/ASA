package com.ASA

import java.awt.{Component, Container, GridBagConstraints, Insets}
import javax.media.opengl.fixedfunc.GLMatrixFunc
import javax.media.opengl.glu.GLU
import javax.media.opengl.{GL3, GLProfile}

import com.jogamp.graph.curve.opengl.RenderState
import com.jogamp.graph.geom.Vertex
import com.jogamp.graph.geom.Vertex.Factory
import com.jogamp.graph.geom.opengl.SVertex
import com.jogamp.opengl.util.PMVMatrix
import com.jogamp.opengl.util.gl2.GLUT
import com.jogamp.opengl.util.glsl.{ShaderProgram, ShaderState}
import com.tw10g12.Draw.Engine.{DrawTools, ShaderLoader}
import com.tw10g12.Maths.Vector3

import scala.actors.threadpool.Executors

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
        addToGridBag(c, owner, gridX, gridY, weightX, weightY, new Insets(0,0,0,0))
    }

    def addToGridBag(c: Component, owner: Container, gridX: Int, gridY: Int, weightX: Double, weightY: Double, insets: Insets): Unit =
    {
        addToGridBag(c, owner, gridX, gridY, 1, 1, weightX, weightY, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0)
    }

}