varying vec2 texCoords;
varying vec4 vcolor;

void main()
	{
	gl_Position = ftransform();
	texCoords = gl_MultiTexCoord0.st;
	vcolor = gl_Color;
	}