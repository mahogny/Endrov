/*
built-in
varying vec4 gl_Color;
varying vec4 gl_SecondaryColor;
varying vec4 gl_TexCoord[];
varying float gl_FogFragCoord;*/

varying vec3 texCoords;
varying vec4 vcolor;

uniform sampler3D tex;

void main (void)  
	{



	vec4 texCol = texture3D(tex, texCoords);
	float alpha = texCol.w;
	vec4 newCol = vcolor;
	newCol.xyz = newCol.xyz*alpha;
	newCol.w = alpha;
//	vec4 newCol = texCol.wwww;
	gl_FragColor = newCol;

	//Now there are two ways to proceed: blend with alpha or blend with color.
	//Color will cause intermixing, alpha will not.
	}        


