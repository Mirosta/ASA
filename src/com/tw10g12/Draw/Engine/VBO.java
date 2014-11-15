package com.tw10g12.Draw.Engine;

import com.jogamp.common.nio.Buffers;
import com.tw10g12.Maths.Vector2;
import com.tw10g12.Maths.Vector3;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import java.nio.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 10/11/2014.
 */
public class VBO
{
    private static final int VerticeIndex = 0;
    private static final int IndiceIndex = 1;
    private static final int ShaderVertexLocation = 0;
    private static final int ShaderColourLocation = 1;

    private Vector2 emptyCoord = new Vector2(-1024,-1024);

    protected List<Float> vertices = new ArrayList<Float>();
    protected List<Integer> indices = new ArrayList<Integer>();
    protected int[] bufferPtrs = new int[]{-1,-1};
    protected int[] vaoPtrs = new int[]{-1};
    protected int[] bufferSizes = new int[]{0,0};
    protected GL3 gl3;

    public VBO(GL3 gl3)
    {
        this.gl3 = gl3;
    }

    public void setup()
    {
        makeBuffers();
    }

    private void makeBuffers()
    {
        gl3.glGenBuffers(1, bufferPtrs, 0);
        gl3.glGenBuffers(1, bufferPtrs, 1);
        gl3.glGenVertexArrays(1, vaoPtrs, 0);
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

    public void addIndex(int index)
    {
        indices.add(index);
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
    }

    protected void resizeBuffer(int elements, int elementSize, int bufferIndex, int bufferType, int usage)
    {
        if(bufferSizes[bufferIndex] != elements)
        {
            gl3.glBindBuffer(bufferType, bufferPtrs[bufferIndex]);
            gl3.glBufferData(bufferType, elements * elementSize, null, usage);
            bufferSizes[bufferIndex] = elements;
        }
    }

    public void resizeBuffers()
    {
        resizeBuffer(vertices.size(), Buffers.SIZEOF_FLOAT, VerticeIndex, GL2.GL_ARRAY_BUFFER, GL2.GL_DYNAMIC_DRAW);
        resizeBuffer(indices.size(), Buffers.SIZEOF_INT, IndiceIndex, GL2.GL_ELEMENT_ARRAY_BUFFER, GL2.GL_DYNAMIC_DRAW);
    }

    public void flushBuffers()
    {
        gl3.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferPtrs[VerticeIndex]);
        ByteBuffer byteBuff = gl3.glMapBuffer(GL.GL_ARRAY_BUFFER, GL2.GL_WRITE_ONLY);
        FloatBuffer floatBuff = byteBuff.order(ByteOrder.nativeOrder()).asFloatBuffer();

        for(Float f : vertices)
        {
            floatBuff.put(f);
        }

        gl3.glUnmapBuffer(GL2.GL_ARRAY_BUFFER);

        gl3.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferPtrs[IndiceIndex]);
        byteBuff = gl3.glMapBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, GL2.GL_WRITE_ONLY);
        IntBuffer intBuff = byteBuff.order(ByteOrder.nativeOrder()).asIntBuffer();

        for(Integer i : indices)
        {
            intBuff.put(i);
        }

        gl3.glUnmapBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER);
    }

    private void setupPointers()
    {
        /*gl3.glVertexPointer(3, GL.GL_FLOAT, 12 * Buffers.SIZEOF_FLOAT, 0);
        gl3.glColorPointer(4, GL.GL_FLOAT, 12 * Buffers.SIZEOF_FLOAT, 3 * Buffers.SIZEOF_FLOAT);
        gl3.glTexCoordPointer(2, GL.GL_FLOAT, 12 * Buffers.SIZEOF_FLOAT, 7 * Buffers.SIZEOF_FLOAT);
        gl3.glNormalPointer(GL.GL_FLOAT, 12 * Buffers.SIZEOF_FLOAT, 9 * Buffers.SIZEOF_FLOAT);*/
        //int posAttribPos = loadedShader.getAttributeLocation(gl3, "position");
        gl3.glEnableVertexAttribArray(ShaderVertexLocation);
        gl3.glVertexAttribPointer(
                ShaderVertexLocation,  /* attribute */
                3,                                /* size */
                GL2.GL_FLOAT,                         /* type */
                false,                         /* normalized? */
                Buffers.SIZEOF_FLOAT * 12,                /* stride */
                0L                      /* array buffer offset */
        );
        gl3.glEnableVertexAttribArray(ShaderColourLocation);
        gl3.glVertexAttribPointer(
                ShaderColourLocation,  /* attribute */
                4,                                /* size */
                GL2.GL_FLOAT,                         /* type */
                false,                         /* normalized? */
                Buffers.SIZEOF_FLOAT * 12,                /* stride */
                3L * Buffers.SIZEOF_FLOAT                      /* array buffer offset */
        );
        //    gl3.glEnableVertexAttribArray(posAttribPos);
    }

    public void bind()
    {
        gl3.glBindVertexArray(vaoPtrs[VerticeIndex]);
        gl3.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferPtrs[VerticeIndex]);
        setupPointers();

        //gl3.glDrawArrays(GL2.GL_TRIANGLES, 0, 3);
        gl3.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferPtrs[IndiceIndex]);
    }

    public void unbind()
    {
        gl3.glUnmapBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER);
        gl3.glUnmapBuffer(GL2.GL_ARRAY_BUFFER);
        //gl3.glBindVertexArray(0);
    }

    public void reset()
    {
        vertices.clear();
        indices.clear();
    }

    public int getIndicesSize()
    {
        return indices.size();
    }

    public boolean isEmpty()
    {
        return vertices.size() == 0 || indices.size() == 0;
    }
}
