//Meant to be a clip-only module, need to be combined with other shaders for color.
//Not sure if it works or if color etc always have to be set

varying float vsclip;
uniform vec4 clipplane;

void main()
	{
	//Hm. when to apply?
	//vsclip = dot(gl_ModelViewMatrix * gl_Vertex,clipplane);
	vsclip = dot(gl_Vertex,clipplane);
}

