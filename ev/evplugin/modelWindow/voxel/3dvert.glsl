varying vec3 texCoords;
varying vec4 vcolor;

void main()
	{
	gl_Position = ftransform();
	texCoords = gl_MultiTexCoord0.stp; //st //ref p.36
	vcolor = gl_Color;
	}