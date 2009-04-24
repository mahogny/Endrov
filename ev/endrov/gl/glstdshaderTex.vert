//Meant to be a close approximation to the old fixed function shader

void main()
	{
	//ftransform gone in 3.1
	gl_Position = ftransform();
	gl_TexCoord[0] = gl_MultiTexCoord0;
	}
