//http://duriansoftware.com/joe/An-intro-to-modern-OpenGL.-Chapter-2.2:-Shaders.html
#version 330 core
//extension GL_ARB_separate_shader_objects: disable

uniform mat4 projection;
uniform mat4 modelView;

layout(location=0) in vec4 inVertex;
layout(location=1) in vec4 inColour;

out vec4 vertexColour;
//Vertex shader
void main()
{
	gl_Position = projection * modelView * inVertex;

	//if(total == 0) vertexColour = vec4(1,0,0,1);
	vertexColour = inColour;//projection[1] * vec4(1,1,1,0) + vec4(0,0,0,1);//vec4(1,0,0,1);
}