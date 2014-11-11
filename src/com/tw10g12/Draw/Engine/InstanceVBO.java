package com.tw10g12.Draw.Engine;

import com.jogamp.common.nio.Buffers;
import com.tw10g12.Maths.Vector2;
import com.tw10g12.Maths.Vector3;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 11/11/2014.
 */
public class InstanceVBO extends VBO
{
    private static final int InstanceIndex = 2;
    private static final int ShaderInstanceLocation = 2;
    private static final int ShaderInstanceDivisor = 1;

    protected List<Float> positions = new ArrayList<Float>();

    public InstanceVBO(GL3 gl3)
    {
        super(gl3);
        bufferPtrs = new int[]{-1, -1, -1};
        bufferSizes = new int[]{0, 0, 0};
    }

    @Override
    public void setup()
    {
        super.setup();
        makeBuffers();
    }

    private void makeBuffers()
    {
        gl3.glGenBuffers(1, bufferPtrs, 2);
    }

    @Override
    public int addVertex(Vector3 pos, Colour col)
    {
        return super.addVertex(pos, col);
    }

    @Override
    public int addVertex(Vector3 pos, Colour col, Vector2 texCoord)
    {
        return super.addVertex(pos, col, texCoord);
    }

    @Override
    public void addIndex(short index)
    {
        super.addIndex(index);
    }

    @Override
    public void setNormals(int numberVertices, int verticeOffset)
    {
        super.setNormals(numberVertices, verticeOffset);
    }

    @Override
    public void resizeBuffers()
    {
        super.resizeBuffers();
        resizeBuffer(positions.size(), Buffers.SIZEOF_FLOAT, InstanceIndex, GL2.GL_ARRAY_BUFFER, GL2.GL_DYNAMIC_DRAW);
    }

    @Override
    public void flushBuffers()
    {
        super.flushBuffers();

        gl3.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferPtrs[InstanceIndex]);
        ByteBuffer byteBuff = gl3.glMapBuffer(GL.GL_ARRAY_BUFFER, GL2.GL_WRITE_ONLY);
        FloatBuffer floatBuff = byteBuff.order(ByteOrder.nativeOrder()).asFloatBuffer();

        for(Float f : positions)
        {
            floatBuff.put(f);
        }

        gl3.glUnmapBuffer(GL2.GL_ARRAY_BUFFER);
    }

    private void setupPointers()
    {
        gl3.glEnableVertexAttribArray(ShaderInstanceLocation);
        gl3.getGL3().glVertexAttribDivisor( ShaderInstanceLocation, ShaderInstanceDivisor);
    }

    @Override
    public void bind()
    {
        super.bind();
        gl3.glBindBuffer( GL2.GL_ARRAY_BUFFER, bufferPtrs[InstanceIndex]);
        setupPointers();
    }

    @Override
    public void unbind()
    {
        super.unbind();
    }

    @Override
    public void reset()
    {
        super.reset();
    }

    @Override
    public int getIndicesSize()
    {
        return super.getIndicesSize();
    }

    @Override
    public boolean isEmpty()
    {
        return super.isEmpty();
    }
}
