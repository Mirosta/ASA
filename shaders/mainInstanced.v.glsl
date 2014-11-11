//http://duriansoftware.com/joe/An-intro-to-modern-OpenGL.-Chapter-2.2:-Shaders.html
#version 330 core
//extension GL_ARB_separate_shader_objects: disable

uniform mat4 projection;
uniform mat4 modelView;

layout(location=0) in vec4 inVertex;
layout(location=1) in vec4 inColour;
layout(location=2) in vec3 inPosition;

out vec4 vertexColour;
//Vertex shader
void main()
{
	gl_Position = projection * modelView * (inVertex.xyz + inPosition.xyz, inVertex.w);
	float total = 0;

	for(int i =0; i < 4; i++)
	{
	    total += projection[i].x;
	    total += projection[i].y;
	    total += projection[i].z;
	    total += projection[i].w;
	}

	//if(total == 0) vertexColour = vec4(1,0,0,1);
	vertexColour = inColour;//projection[1] * vec4(1,1,1,0) + vec4(0,0,0,1);//vec4(1,0,0,1);
}