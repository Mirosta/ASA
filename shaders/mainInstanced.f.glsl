#version 330 core
//extension GL_ARB_separate_shader_objects: disable

uniform float fade_factor;
uniform sampler2D textures[2];
//layout(location=2) in vec2 texcoord;

in vec4 vertexColour;
out vec4 fragColour;
//Fragment shader
void main()
{
	//float d = abs(gl_FragCoord.z*2.0)/10.0;
    fragColour = vertexColour;//vec4(d, d, d, 1);
}