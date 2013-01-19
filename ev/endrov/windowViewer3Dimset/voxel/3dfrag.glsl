/*
built-in
varying vec4 gl_Color;
varying vec4 gl_SecondaryColor;
varying vec4 gl_TexCoord[];
varying float gl_FogFragCoord;*/

varying vec3 texCoords;
varying vec4 vcolor;


//varying float contrast;
//varying float brightness;

uniform float contrast;
uniform float brightness;




uniform sampler3D tex;

void main (void)  
	{
	/*
	There are two things to decide here: color and alpha.
	alpha can be decided using contrast/brightness as parameters.
	(this is a simple transfer function, can be extended later!)
	colors, we get back to this. using transfer functions.
	
	Multi-texturing can be used to combine colors of several different stacks
	*/
	
	
	
	vec4 texCol = texture3D(tex, texCoords);
	//float alpha = texCol.w;
	float alpha = texCol.w+brightness;
	
	//TODO how to handle negative values
	
	if(alpha<0.0)
		alpha=0.0;
	
	//vec4 newCol = vcolor;
	vec4 newCol = vcolor*contrast;
	newCol.xyz = newCol.xyz*alpha;
	newCol.w = alpha;
	gl_FragColor = newCol;
	}        


