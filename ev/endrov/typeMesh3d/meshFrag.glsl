//http://www.dhpoware.com/demos/glObjViewer.html

uniform sampler2D colorMap;
uniform float materialAlpha;

varying vec3 normal;

void main()
{   
    vec3 n = normalize(normal);

    float nDotL = max(0.0, dot(n, gl_LightSource[0].position.xyz));
    float nDotH = max(0.0, dot(normal, vec3(gl_LightSource[0].halfVector)));
    float power = (nDotL == 0.0) ? 0.0 : pow(nDotH, gl_FrontMaterial.shininess);
    
    vec4 ambient = gl_FrontLightProduct[0].ambient;
    vec4 diffuse = gl_FrontLightProduct[0].diffuse * nDotL;
    vec4 specular = gl_FrontLightProduct[0].specular * power;
    vec4 color = gl_FrontLightModelProduct.sceneColor + ambient + diffuse + specular;
    
    gl_FragColor = color * texture2D(colorMap, gl_TexCoord[0].st);
    gl_FragColor.a = materialAlpha;
}



/*


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
	gl_FragColor = newCol;

	//Now there are two ways to proceed: blend with alpha or blend with color.
	//Color will cause intermixing, alpha will not.
	}        


*/