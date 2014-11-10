package com.tw10g12.Draw.Engine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;
import com.tw10g12.Maths.Vector2;
import com.tw10g12.Maths.Vector3;

public class Tessellator
{
    private static final int VerticeIndex = 0;
    private static final int IndiceIndex = 1;
    private List<Float> vertices = new ArrayList<Float>();
    private List<Short> indices = new ArrayList<Short>();
    private int[] bufferPtrs = new int[]{-1,-1};
    private int[] bufferSizes = new int[]{0,0};
    private Vector2 emptyCoord = new Vector2(-1024,-1024);

    private GL2 gl2;
    private ShaderLoader loadedShader;
    private ShaderLoader noLightingShader;

    public Tessellator(GL2 gl2, ShaderLoader loadedShader, ShaderLoader noLightingShader)
    {
        this.gl2 = gl2;
        makeBuffers();

        loadedShader.loadAndCompileShaders(gl2);
        loadedShader.makeProgram(gl2);
        noLightingShader.loadAndCompileShaders(gl2);
        noLightingShader.makeProgram(gl2);

        this.loadedShader = loadedShader;
        this.noLightingShader = noLightingShader;
    }

    private void setupPointers()
    {
        gl2.glEnableClientState( GL2.GL_VERTEX_ARRAY );
        gl2.glEnableClientState( GL2.GL_COLOR_ARRAY );
        gl2.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl2.glEnableClientState(GL2.GL_NORMAL_ARRAY);

        gl2.glVertexPointer( 3, GL.GL_FLOAT, 12 * Buffers.SIZEOF_FLOAT, 0 );
        gl2.glColorPointer( 4, GL.GL_FLOAT, 12 * Buffers.SIZEOF_FLOAT, 3 * Buffers.SIZEOF_FLOAT );
        gl2.glTexCoordPointer(2, GL.GL_FLOAT, 12 * Buffers.SIZEOF_FLOAT, 7 * Buffers.SIZEOF_FLOAT);
        gl2.glNormalPointer(GL.GL_FLOAT, 12 * Buffers.SIZEOF_FLOAT, 9 * Buffers.SIZEOF_FLOAT);
        //int posAttribPos = loadedShader.getAttributeLocation(gl2, "position");
        //gl2.glVertexAttribPointer(
        //		posAttribPos,  /* attribute */
        //        2,                                /* size */
        //        GL2.GL_FLOAT,                         /* type */
        //        false,                         /* normalized? */
        //       Buffers.SIZEOF_FLOAT*7,                /* stride */
        //        0L                      /* array buffer offset */
        //    );
        //    gl2.glEnableVertexAttribArray(posAttribPos);
    }

    public int addVertex(Vector3 pos, Colour col)
    {
        return addVertex(pos, col, emptyCoord);
    }

    public int addVertex(Vector3 pos, Colour col, Vector2 texCoord)
    {
        int index = vertices.size();
        vertices.add((float)pos.getX());
        vertices.add((float)pos.getY());
        vertices.add((float)pos.getZ());
        vertices.add(col.getR());
        vertices.add(col.getG());
        vertices.add(col.getB());
        vertices.add(col.getA());
        vertices.add((float)texCoord.getX());
        vertices.add((float)texCoord.getY());
        vertices.add(0f);
        vertices.add(0f);
        vertices.add(0f);
        return index/12;
    }

    public void addIndex(short index)
    {
        indices.add(index);
    }

    private void makeBuffers()
    {
        gl2.glGenBuffers(1, bufferPtrs, 0);
        gl2.glGenBuffers(1, bufferPtrs, 1);
    }

    private void resizeBuffer(int elements, int elementSize, int bufferIndex, int bufferType, int usage)
    {
        if(bufferSizes[bufferIndex] != elements)
        {
            gl2.glBindBuffer(bufferType, bufferPtrs[bufferIndex]);
            gl2.glBufferData(bufferType, elements*elementSize, null, usage);
            bufferSizes[bufferIndex] = elements;
        }
    }

    private void resizeBuffers()
    {
        resizeBuffer(vertices.size(), Buffers.SIZEOF_FLOAT, VerticeIndex, GL2.GL_ARRAY_BUFFER, GL2.GL_DYNAMIC_DRAW);
        resizeBuffer(indices.size(), Buffers.SIZEOF_SHORT, IndiceIndex, GL2.GL_ELEMENT_ARRAY_BUFFER, GL2.GL_DYNAMIC_DRAW);
    }

    private void flushBuffers()
    {
        gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferPtrs[VerticeIndex]);
        ByteBuffer byteBuff = gl2.glMapBuffer(GL.GL_ARRAY_BUFFER, GL2.GL_WRITE_ONLY);
        FloatBuffer floatBuff = byteBuff.order(ByteOrder.nativeOrder()).asFloatBuffer();

        for(Float f : vertices)
        {
            floatBuff.put(f);
        }

        gl2.glUnmapBuffer(GL2.GL_ARRAY_BUFFER);

        gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferPtrs[IndiceIndex]);
        byteBuff = gl2.glMapBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, GL2.GL_WRITE_ONLY);
        ShortBuffer intBuff = byteBuff.order(ByteOrder.nativeOrder()).asShortBuffer();

        for(Short i : indices)
        {
            intBuff.put(i);
        }

        gl2.glUnmapBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER);
    }

    public void reset()
    {
        vertices.clear();
        indices.clear();
    }

    public void draw(int shaderMode)
    {
        if(vertices.size() == 0 || indices.size() == 0) return;
        resizeBuffers();
        flushBuffers();

        int program = 0;
        if(shaderMode == 1) program = loadedShader.getShaderProgram();
        else if(shaderMode == 2) program = noLightingShader.getShaderProgram();
        gl2.glUseProgram(program);

        gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferPtrs[VerticeIndex]);
        setupPointers();

        //gl2.glDrawArrays(GL2.GL_TRIANGLES, 0, 3);
        gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferPtrs[IndiceIndex]);

        gl2.glDrawElements(
                GL2.GL_TRIANGLES,  /* mode */
                indices.size(),                  /* count */
                GL2.GL_UNSIGNED_SHORT,  /* type */
                0            /* element array buffer offset */
        );

        cleanup();
    }

    private void cleanup()
    {
        gl2.glUseProgram(0);
        gl2.glUnmapBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER);
        gl2.glUnmapBuffer(GL2.GL_ARRAY_BUFFER);

        //gl2.glDisableVertexAttribArray(loadedShader.getAttributeLocation(gl2, "position"));
        gl2.glDisableClientState( GL2.GL_VERTEX_ARRAY );
        gl2.glDisableClientState( GL2.GL_COLOR_ARRAY );
        gl2.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl2.glDisableClientState(GL2.GL_NORMAL_ARRAY);
        gl2.glDisable( GL2.GL_COLOR_MATERIAL );
    }

    public void setNormals(int numberVertices, int verticeOffset)
    {
        int index = vertices.size();
        Vector3[] positions = new Vector3[3];
        int n =0;
        for(int i = numberVertices-verticeOffset; i > 0 && n < 3; i--)
        {
            positions[n] = new Vector3(vertices.get(index-i*12),vertices.get(index-i*12 + 1),vertices.get(index-i*12 + 2));
            n++;
        }
        Vector3 normal = Vector3.cross(positions[0].subtract(positions[1]), positions[2].subtract(positions[1])).normalise();

        for(int i = 0; i < numberVertices; i++)
        {
            vertices.set(index-i*12-3, (float)normal.getX());
            vertices.set(index-i*12-2, (float)normal.getY());
            vertices.set(index-i*12-1, (float)normal.getZ());
        }

        //System.out.println(positions[0].subtract(positions[1]) + " cross " + positions[2].subtract(positions[1]));
        //System.out.println("Last Normal: " + normal);
    }
}