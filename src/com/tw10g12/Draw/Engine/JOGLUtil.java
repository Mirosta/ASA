package com.tw10g12.Draw.Engine;

import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.GLRegion;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.geom.Triangle;
import com.jogamp.graph.geom.Vertex;
import com.jogamp.graph.geom.opengl.SVertex;
import com.jogamp.opengl.util.PMVMatrix;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 07/02/2015.
 */
public class JOGLUtil
{
    static ShaderProgram getShaderProgram(ShaderLoader shader)
    {
        ShaderProgram shaderProgram = new ShaderProgram();
        return shaderProgram;
    }

    static class LoadedShaderProgram extends ShaderProgram
    {

        public LoadedShaderProgram(ShaderLoader shader)
        {
            this.programLinked = true;
            this.programInUse = false;
            this.shaderProgram = shader.getShaderProgram();
        }
    }

    static ShaderState getShaderState(DrawTools drawTools)
    {
        ShaderState shaderState = new ShaderState();
        //shaderState.attachShaderProgram(drawTools.getGL3(), getShaderProgram(drawTools.getCurrentShader()), true);
        return shaderState;
    }

    static Vertex.Factory<? extends Vertex> getVertexFactory(DrawTools drawTools)
    {
        return SVertex.factory();
    }

    static PMVMatrix getPMVMatrix(DrawTools drawTools)
    {
        PMVMatrix pMVMatrix = new PMVMatrix();
        pMVMatrix.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        pMVMatrix.glLoadMatrixf(drawTools.getModelView().flattenMatrixValuesAsFloats(),0);
        pMVMatrix.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        pMVMatrix.glLoadMatrixf(drawTools.getProjectionMatrix().flattenMatrixValuesAsFloats(),0);

        return pMVMatrix;
    }

    public static RenderState getRenderState(DrawTools drawTools)
    {
        RenderState renderState = RenderState.createRenderState(getShaderState(drawTools), getVertexFactory(drawTools), getPMVMatrix(drawTools));
        return renderState;
    }

    public static class AccessibleRegion extends Region
    {

        public AccessibleRegion(Region original)
        {
            super(0);
            try
            {
                Field trianglesField = Region.class.getDeclaredField("triangles");
                Field verticesField = Region.class.getDeclaredField("vertices");
                trianglesField.setAccessible(true);
                verticesField.setAccessible(true);

                ArrayList<Triangle> triangles = (ArrayList<Triangle>)trianglesField.get(original);
                ArrayList<Vertex> vertices = (ArrayList<Vertex>)verticesField.get(original);

                this.addTriangles(triangles);
                this.addVertices(vertices);
            } catch (NoSuchFieldException e)
            {
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }

        public List<Triangle> getTriangles()
        {
            return this.triangles;
        }

        public List<Vertex> getVertices()
        {
            return this.vertices;
        }
    }
}
