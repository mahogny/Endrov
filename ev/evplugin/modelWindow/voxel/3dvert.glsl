varying vec3 texCoords;
varying vec4 vcolor;

void main()
	{
	gl_ClipVertex = gl_ModelViewMatrix * gl_Vertex; //needed for clipping. if last, mac bugs out.
	gl_Position = ftransform();
	texCoords = gl_MultiTexCoord0.stp; //st //ref p.36
	vcolor = gl_Color;
	
	//http://www.opengl.org/discussion_boards/ubbthreads.php?ubb=showflat&Number=236760
	
	
	//http://lists.apple.com/archives/mac-opengl/2006/Jan/msg00028.html //order can matter!
	//yes, same problem
	//http://blogs.guardian.co.uk/technology/2007/12/21/apple_censorship_at_issue_on_its_support_forums_again.html
	//me: NVIDIA GeForce 7300 GT
	
	//http://www.tomsguide.com/us/leopard-osx-problems,review-1028.html
	}