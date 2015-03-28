package com.tw10g12.Draw.Engine;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import com.sun.prism.impl.VertexBuffer;
import com.tw10g12.Draw.Engine.Exception.InvalidVertexBufferException;
import com.tw10g12.Maths.Vector2;
import com.tw10g12.Maths.Vector3;

public class Tessellator
{
    private static final int defaultShader = 0;
    private static final int defaultVBO = 0;

    private List<VBO> vertexBuffers = new ArrayList<VBO>();

    private GL3 gl3;
    private List<ShaderLoader> shaders;
    private int currentShader = defaultShader;
    private int currentVBO = defaultVBO;

    public Tessellator(GL3 gl3, List<ShaderLoader> shaders)
    {
        this.gl3 = gl3;
        addDefaultVBO();

        for(ShaderLoader shader : shaders)
        {
            shader.loadAndCompileShaders(gl3);
            shader.makeProgram(gl3);
        }
        this.shaders = shaders;
    }

    private void addDefaultVBO()
    {
        addNewVBO(); //Normal VBO
    }

    public int addVertex(Vector3 pos, Colour col)
    {
        return vertexBuffers.get(currentVBO).addVertex(pos, col);
    }

    public int addVertex(Vector3 pos, Colour col, Vector2 texCoord)
    {
        return vertexBuffers.get(currentVBO).addVertex(pos, col, texCoord);
    }

    public void addIndex(int index)
    {
        vertexBuffers.get(currentVBO).addIndex(index);
    }

    private void resizeBuffers()
    {
        vertexBuffers.get(currentVBO).resizeBuffers();
    }

    private void flushBuffers()
    {
        vertexBuffers.get(currentVBO).flushBuffers();
    }

    public void reset()
    {
        for(VBO buffer : vertexBuffers)
        {
            buffer.reset();
        }
    }

    private void setupClientState()
    {
        /*gl3.glEnableClientState(GL3.GL_VERTEX_ARRAY);
        gl3.glEnableClientState(GL3.GL_COLOR_ARRAY);
        gl3.glEnableClientState(GL3.GL_TEXTURE_COORD_ARRAY);
        gl3.glEnableClientState(GL3.GL_NORMAL_ARRAY);*/
    }

    private void cleanupClientState()
    {
        //gl3.glDisableVertexAttribArray(loadedShader.getAttributeLocation(gl3, "position"));
        /*gl3.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl3.glDisableClientState(GL2.GL_COLOR_ARRAY);
        gl3.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl3.glDisableClientState(GL2.GL_NORMAL_ARRAY);
        gl3.glDisable(GL2.GL_COLOR_MATERIAL);*/
    }

    public void useProgram()
    {
        int program = shaders.get(currentShader).getShaderProgram();
        gl3.glUseProgram(program);
    }

    public void drawInstanced()
    {
        if(!(vertexBuffers.get(currentVBO) instanceof InstanceVBO)) throw new InvalidVertexBufferException("The selected vertex buffer must be an instance vertex buffer");
        InstanceVBO vbo = (InstanceVBO)vertexBuffers.get(currentVBO);

        if(vbo.isEmpty()) return;
        resizeBuffers();
        flushBuffers();

        vbo.bind();

        gl3.glDrawElementsInstanced(GL2.GL_TRIANGLES, vertexBuffers.get(currentVBO).getIndicesSize(), GL3.GL_UNSIGNED_INT, 0, vbo.getPositionsSize());
    }

    public void draw()
    {
        if(vertexBuffers.get(currentVBO).isEmpty()) return;
        resizeBuffers();
        flushBuffers();

        vertexBuffers.get(currentVBO).bind();
        setupClientState();

        gl3.glDrawElements(
                GL2.GL_TRIANGLES,  /* mode */
                vertexBuffers.get(currentVBO).getIndicesSize(),                  /* count */
                GL2.GL_UNSIGNED_INT,  /* type */
                0            /* element array buffer offset */
        );

        cleanup();
    }

    public void drawLines(int drawMode)
    {
        if(vertexBuffers.get(currentVBO).isEmpty()) return;
        resizeBuffers();
        flushBuffers();

        vertexBuffers.get(currentVBO).bind();
        setupClientState();
        if(drawMode != GL3.GL_LINES && drawMode != GL3.GL_LINE_STRIP && drawMode != GL3.GL_LINE_LOOP) throw new RuntimeException("Draw mode must be a line mode");
        gl3.glDrawElements(
                drawMode,  /* mode */
                vertexBuffers.get(currentVBO).getIndicesSize(),                  /* count */
                GL2.GL_UNSIGNED_INT,  /* type */
                0            /* element array buffer offset */
        );

        cleanup();
    }

    private void cleanup()
    {
        //gl3.glUseProgram(0);
        //vertexBuffers.get(currentVBO).unbind();
        //cleanupClientState();
    }

    public void setNormals(int numberVertices, int verticeOffset)
    {
        vertexBuffers.get(currentVBO).setNormals(numberVertices, verticeOffset);
        //System.out.println(positions[0].subtract(positions[1]) + " cross " + positions[2].subtract(positions[1]));
        //System.out.println("Last Normal: " + normal);
    }

    public void setCurrentShader(int currentShader)
    {
        this.currentShader = currentShader;
    }

    public void setCurrentVBO(int currentVBO)
    {
        this.currentVBO = currentVBO;
    }

    public int addNewVBO()
    {
        VBO newVBO = new VBO(this.gl3);
        newVBO.setup();
        vertexBuffers.add(newVBO);

        return vertexBuffers.size() - 1;
    }

    public int addNewInstanceVBO()
    {
        InstanceVBO newVBO = new InstanceVBO(this.gl3);
        newVBO.setup();
        vertexBuffers.add(newVBO);

        return vertexBuffers.size() - 1;
    }

    public ShaderLoader getCurrentShader()
    {
        return shaders.get(currentShader);
    }

    public int getShaderModelViewMatrixLocation()
    {
        return shaders.get(currentShader).getAttributeLocation(gl3, shaders.get(currentShader).getModelViewName());
    }

    public int getShaderProjectionMatrixLocation()
    {
        return shaders.get(currentShader).getAttributeLocation(gl3, shaders.get(currentShader).getProjectionName());
    }

    public void addInstancePosition(Vector3 position)
    {
        if(!(vertexBuffers.get(currentVBO) instanceof InstanceVBO)) throw new InvalidVertexBufferException("The selected vertex buffer must be an instance vertex buffer");
        InstanceVBO vbo = (InstanceVBO)vertexBuffers.get(currentVBO);
        vbo.addInstancePosition(position);
    }

    public void setGL3(GL3 gl3)
    {
        this.gl3 = gl3;
        for(VBO vbo : vertexBuffers)
        {
            vbo.setGL3(gl3);
        }
    }
}