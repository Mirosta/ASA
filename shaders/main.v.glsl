//http://duriansoftware.com/joe/An-intro-to-modern-OpenGL.-Chapter-2.2:-Shaders.html

#version 110

attribute vec2 position;
uniform mat4 projection;
uniform mat4 modelView;

//Vertex shader
void main()
{
	gl_Position = ftransform();
    gl_FrontColor = gl_Color;
}