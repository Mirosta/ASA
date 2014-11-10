#version 110

uniform float fade_factor;
uniform sampler2D textures[2];

varying vec2 texcoord;

//Fragment shader
void main()
{
	float d = abs(gl_FragCoord.z*2.0)/10.0;
    gl_FragColor = gl_Color;//vec4(d, d, d, 1);
}