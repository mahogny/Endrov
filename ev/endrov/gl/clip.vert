varying float vsclip;
uniform vec4 clipplane;


void main()
{


	//Hm. when to apply?
	//vsclip = dot(gl_ModelViewMatrix * gl_Vertex,clipplane);
	vsclip = dot(gl_Vertex,clipplane);


	gl_Position = ftransform();
	//gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;  same as
	gl_TexCoord[0] = gl_MultiTexCoord0;
}

