uniform sampler2D tex;
varying float vsclip;

void main()
{
if(vsclip<0.0)
	discard;


vec4 color, texel;
color=gl_Color;
//texel=texture2D(texUnit0, gl_TexCoord[0].xy);
texel=texture2D(tex, gl_TexCoord[0].st);
color=texel;
gl_FragColor=color;


//gl_FragColor=vsclip.xxxx;

}

