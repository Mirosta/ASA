package com.tw10g12.Draw.Engine;

import com.tw10g12.IO.Util;
import com.tw10g12.Maths.Matrix4;
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
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL3;

public class ShaderLoader 
{
	private static final int VertexShader = 0;
	private static final int FragmentShader = 1;

	private int shaderProgram;
	private int[] shaders;
	private String shaderLocation;
	private boolean loaded = false;
	private boolean linked = false;
	private String modelViewName = "modelView"; //Default values
	private String projectionName = "projection";
	private Map<String, Integer> variableLocations = new HashMap<String, Integer>();

	public ShaderLoader(String shaderLocation)
	{
		this.shaderLocation = shaderLocation;
	}

	public void loadAndCompileShaders(GL3 gl3)
	{
		if(loaded) return;
		
		File vertexFile = new File(shaderLocation + ".v.glsl");
		File fragmentFile = new File(shaderLocation + ".f.glsl");
		shaders = new int[2];
		
		shaders[VertexShader] = gl3.glCreateShader(GL3.GL_VERTEX_SHADER);
		shaders[FragmentShader] = gl3.glCreateShader(GL3.GL_FRAGMENT_SHADER);
		
		try 
		{
			//Read shaders from file
			String rawVertexShader = Util.readFileToEnd(vertexFile);
			String rawFragmentShader = Util.readFileToEnd(fragmentFile);
			
			//Compile Vertex Shader
			gl3.glShaderSource(shaders[VertexShader], 1, new String[]{rawVertexShader}, (IntBuffer) null);
			gl3.glCompileShader(shaders[VertexShader]);
			checkCompileStatus(gl3, VertexShader);
			//Compile Fragment Shader
			gl3.glShaderSource(shaders[FragmentShader], 1, new String[]{rawFragmentShader}, (IntBuffer) null);
			gl3.glCompileShader(shaders[FragmentShader]);
			checkCompileStatus(gl3, FragmentShader);
			
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
	
	public int makeProgram(GL3 gl3)
	{
		if(linked) return shaderProgram;
		
		shaderProgram = gl3.glCreateProgram();
		gl3.glAttachShader(shaderProgram, shaders[VertexShader]);
		gl3.glAttachShader(shaderProgram, shaders[FragmentShader]);
		gl3.glLinkProgram(shaderProgram);
		gl3.glValidateProgram(shaderProgram);
		checkLinkStatus(gl3);
		
		linked = true;
		return shaderProgram;
	}
	
	private void checkLinkStatus(GL3 gl3)
	{
		IntBuffer intBuffer = IntBuffer.allocate(1);
		gl3.glGetProgramiv(shaderProgram, GL3.GL_LINK_STATUS, intBuffer);
        
        if (intBuffer.get(0) != 1)
        {
        	
            gl3.glGetProgramiv(shaderProgram, GL3.GL_INFO_LOG_LENGTH, intBuffer);
            int size = intBuffer.get(0);
            System.err.println("Program link error: ");
            if (size > 0)
            {
                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                gl3.glGetProgramInfoLog(shaderProgram, size, intBuffer, byteBuffer);
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
	
	private void checkCompileStatus(GL3 gl3, int shaderIndex)
	{
		IntBuffer intBuffer = IntBuffer.allocate(1);
		gl3.glGetShaderiv(shaders[shaderIndex], GL3.GL_COMPILE_STATUS, intBuffer);
        
        if (intBuffer.get(0) != 1)
        {
        	
            gl3.glGetShaderiv(shaders[shaderIndex], GL3.GL_INFO_LOG_LENGTH, intBuffer);
            int size = intBuffer.get(0);
            System.err.println((shaderIndex == 0 ? "Vertex" : "Fragment") + " shader compile error: ");
            if (size > 0)
            {
                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                gl3.glGetShaderInfoLog(shaders[shaderIndex], size, intBuffer, byteBuffer);
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
	
	public int getShaderProgram(GL3 gl3)
	{
		loadAndCompileShaders(gl3);
		return makeProgram(gl3);
	}
	
	public int getShaderProgram()
	{
		if(!loaded) throw new UnloadedShaderException("Shader hasn't been loaded!");
		if(!linked) throw new UnlinkedShaderException("Shader hasn't been linked!");
		
		return shaderProgram;
	}

	private int getUniformLocation(GL3 gl3, String name)
	{
		if(!variableLocations.containsKey(name))
		{
			int location = gl3.glGetUniformLocation(shaderProgram, name);
			variableLocations.put(name, location);
		}
		return variableLocations.get(name);
	}

	public <T> void setUniformVariable(GL3 gl3, T value, String name)
	{
		int location = getUniformLocation(gl3, name);
		
		if(value instanceof Float)
		{
			gl3.glUniform1f(location, (Float) value);
		}
		else if(value instanceof Integer)
		{
			gl3.glUniform1i(location, (Integer) value);
		}
		else if(value instanceof Vector2)
		{
			Vector2 vec2Val = (Vector2) value;
			gl3.glUniform2f(location, (float) vec2Val.getX(), (float) vec2Val.getY());
		}
		else if(value instanceof Vector2)
		{
			Vector2 vec2Val = (Vector2) value;
			gl3.glUniform2f(location, (float) vec2Val.getX(), (float) vec2Val.getY());
		}
		else if(value instanceof Vector3)
		{
			Vector3 vec3Val = (Vector3) value;
			gl3.glUniform3f(location, (float) vec3Val.getX(), (float) vec3Val.getY(), (float) vec3Val.getZ());
		}
		else if(value instanceof Matrix4)
		{
			Matrix4 mat4Val = (Matrix4) value;
			gl3.glUniformMatrix4fv(location, 1, false, mat4Val.flattenMatrixValuesAsFloats(), 0);
		}
		else
		{
			throw new UnsupportedTypeException("Type not currently supported for set uniform variable");
		}
	}

	public static class UniformProperties
	{
		private int size;
		private int dataType;
		private String name;

		public UniformProperties(int size, int dataType, String name)
		{
			this.size = size;
			this.dataType = dataType;
			this.name = name;
		}

		public int getSize()
		{
			return size;
		}

		public int getDataType()
		{
			return dataType;
		}

		public String getName()
		{
			return name;
		}
	}

	public UniformProperties getUniformProperties(GL3 gl3, int index)
	{
		byte[] name = new byte[256];
		int[] strLength = new int[1];
		int[] size = new int[1];
		int[] type = new int[1];

		gl3.glGetActiveUniform(getShaderProgram(), index, 256, strLength, 0, size, 0, type, 0, name, 0);
		return new UniformProperties(size[0], type[0], new String(name, 0, strLength[0]));
	}

	public int getAttributeLocation(GL3 gl3, String name)
	{
		return gl3.glGetAttribLocation(shaderProgram, name);
	}
	public int[] getProgramIV(GL3 gl3, int paramName)
	{
		int[] iv = new int[5];
		gl3.glGetProgramiv(getShaderProgram(), paramName, iv, 0);
		return iv;
	}
	public String getModelViewName()
	{
		return modelViewName;
	}

	public void setModelViewName(String modelViewName)
	{
		this.modelViewName = modelViewName;
	}

	public String getProjectionName()
	{
		return projectionName;
	}

	public void setProjectionName(String projectionName)
	{
		this.projectionName = projectionName;
	}
}
