varying vec3 texCoords;
varying vec4 vcolor;

void main()
	{
	gl_Position = ftransform();
	texCoords = gl_MultiTexCoord0.stp; //st //ref p.36
	vcolor = gl_Color;
	gl_ClipVertex = gl_ModelViewMatrix * gl_Vertex; //needed for clipping
	
	//http://www.opengl.org/discussion_boards/ubbthreads.php?ubb=showflat&Number=236760
	}