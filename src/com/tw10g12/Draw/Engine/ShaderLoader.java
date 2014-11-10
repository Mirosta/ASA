package com.tw10g12.Draw.Engine;

import com.tw10g12.IO.Util;
import com.tw10g12.Maths.Vector2;
import com.tw10g12.Maths.Vector3;
import com.tw10g12.Draw.Engine.Exception.UnlinkedShaderException;
import com.tw10g12.Draw.Engine.Exception.UnloadedShaderException;
import com.tw10g12.Draw.Engine.Exception.UnsupportedTypeException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL2;

public class ShaderLoader 
{
	private static final int VertexShader = 0;
	private static final int FragmentShader = 1;
	private int shaderProgram;
	private int[] shaders;
	private String shaderLocation;
	private boolean loaded = false;
	private boolean linked = false;
	
	public ShaderLoader(String shaderLocation)
	{
		this.shaderLocation = shaderLocation;
	}
	
	public void loadAndCompileShaders(GL2 gl2)
	{
		if(loaded) return;
		
		File vertexFile = new File(shaderLocation + ".v.glsl");
		File fragmentFile = new File(shaderLocation + ".f.glsl");
		shaders = new int[2];
		
		shaders[VertexShader] = gl2.glCreateShader(GL2.GL_VERTEX_SHADER);
		shaders[FragmentShader] = gl2.glCreateShader(GL2.GL_FRAGMENT_SHADER);
		
		try 
		{
			//Read shaders from file
			String rawVertexShader = Util.readFileToEnd(vertexFile);
			String rawFragmentShader = Util.readFileToEnd(fragmentFile);
			
			//Compile Vertex Shader
			gl2.glShaderSource(shaders[VertexShader], 1, new String[] {rawVertexShader}, (IntBuffer)null);
			gl2.glCompileShader(shaders[VertexShader]);
			checkCompileStatus(gl2, VertexShader);
			//Compile Fragment Shader
			gl2.glShaderSource(shaders[FragmentShader], 1, new String[] {rawFragmentShader}, (IntBuffer)null);
			gl2.glCompileShader(shaders[FragmentShader]);
			checkCompileStatus(gl2, FragmentShader);
			
			loaded = true;
		} 
		catch (FileNotFoundException e) 
		{
			System.err.println("Couldn't load shader: " + e.getMessage());
		} 
		catch (IOException e) 
		{
			System.err.println("Couldn't load shader: " + e.getMessage());
		}
	}
	
	public int makeProgram(GL2 gl2)
	{
		if(linked) return shaderProgram;
		
		shaderProgram = gl2.glCreateProgram();
		gl2.glAttachShader(shaderProgram, shaders[VertexShader]);
		gl2.glAttachShader(shaderProgram, shaders[FragmentShader]);
		gl2.glLinkProgram(shaderProgram);
		gl2.glValidateProgram(shaderProgram);
		checkLinkStatus(gl2);
		
		linked = true;
		return shaderProgram;
	}
	
	private void checkLinkStatus(GL2 gl2)
	{
		IntBuffer intBuffer = IntBuffer.allocate(1);
		gl2.glGetProgramiv(shaderProgram, GL2.GL_LINK_STATUS, intBuffer);
        
        if (intBuffer.get(0) != 1)
        {
        	
            gl2.glGetProgramiv(shaderProgram, GL2.GL_INFO_LOG_LENGTH, intBuffer);
            int size = intBuffer.get(0);
            System.err.println("Program link error: ");
            if (size > 0)
            {
                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                gl2.glGetProgramInfoLog(shaderProgram, size, intBuffer, byteBuffer);
                for (byte b : byteBuffer.array())
                {
                    System.err.print((char) b);
                }
            }
            else
            {
                System.err.println("Unknown");
            }
        }
	}
	
	private void checkCompileStatus(GL2 gl2, int shaderIndex)
	{
		IntBuffer intBuffer = IntBuffer.allocate(1);
		gl2.glGetShaderiv(shaders[shaderIndex], GL2.GL_COMPILE_STATUS, intBuffer);
        
        if (intBuffer.get(0) != 1)
        {
        	
            gl2.glGetShaderiv(shaders[shaderIndex], GL2.GL_INFO_LOG_LENGTH, intBuffer);
            int size = intBuffer.get(0);
            System.err.println((shaderIndex == 0 ? "Vertex" : "Fragment") + " shader compile error: ");
            if (size > 0)
            {
                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                gl2.glGetShaderInfoLog(shaders[shaderIndex], size, intBuffer, byteBuffer);
                for (byte b : byteBuffer.array())
                {
                    System.err.print((char) b);
                }
            }
            else
            {
                System.err.println("Unknown");
            }
        }
	}
	
	public int getShaderProgram(GL2 gl2)
	{
		loadAndCompileShaders(gl2);
		return makeProgram(gl2);
	}
	
	public int getShaderProgram()
	{
		if(!loaded) throw new UnloadedShaderException("Shader hasn't been loaded!");
		if(!linked) throw new UnlinkedShaderException("Shader hasn't been linked!");
		
		return shaderProgram;
	}
	
	public <T> void setUniformVariable(T value, String name, GL2 gl2)
	{
		int location = gl2.glGetUniformLocation(shaderProgram, name);
		
		if(value instanceof Float)
		{
			gl2.glUniform1f(location, (Float)value);
		}
		else if(value instanceof Integer)
		{
			gl2.glUniform1i(location, (Integer)value);
		}
		else if(value instanceof Vector2)
		{
			Vector2 vec2Val = (Vector2) value;
			gl2.glUniform2f(location, (float)vec2Val.getX(), (float)vec2Val.getY());
		}
		else if(value instanceof Vector2)
		{
			Vector2 vec2Val = (Vector2) value;
			gl2.glUniform2f(location, (float)vec2Val.getX(), (float)vec2Val.getY());
		}
		else if(value instanceof Vector3)
		{
			Vector3 vec3Val = (Vector3) value;
			gl2.glUniform3f(location, (float)vec3Val.getX(), (float)vec3Val.getY(), (float)vec3Val.getZ());
		}
		else
		{
			throw new UnsupportedTypeException("Type not currently supported for set uniform variable");
		}
	}

	public int getAttributeLocation(GL2 gl2, String name)
	{
		return gl2.glGetAttribLocation(shaderProgram, name);
	}
}
