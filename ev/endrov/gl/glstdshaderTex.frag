//Meant to be a close approximation to the old fixed function shader

uniform sampler2D tex;

void main()
	{
	vec4 color, texel;
	color=gl_Color;
	texel=texture2D(tex, gl_TexCoord[0].st);
	gl_FragColor=color*texel;
	}
