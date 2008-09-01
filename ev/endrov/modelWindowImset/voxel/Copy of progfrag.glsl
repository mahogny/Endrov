#define NNOISE 4

#define PI 3.141592653

#define PALE_BLUE vec4(0.25, 0.25, 0.35, 1.0)
//#define PALE_BLUE vec4(0.90, 0.90, 1.0, 1.0)
#define MEDIUM_BLUE vec4(0.10, 0.10, 0.30, 1.0)
#define DARK_BLUE vec4(0.05, 0.05, 0.26, 1.0)
#define DARKER_BLUE vec4(0.03, 0.03, 0.20, 1.0)

varying vec3 normal;
varying vec4 pos;
varying vec4 rawpos;

//uniform float scale;



//////////////////////////////////////////////////////////////

/*
 * This contains a number of functions that may be useful
 * in a number of shaders.
 */
#pragma debug(on)
/*
 * these are used in the spline function
 */
#define CR00 (-0.5)
#define CR01 (1.5)
#define CR02 (-1.5)
#define CR03 (0.5)
#define CR10 (1.0)
#define CR11 (-2.5)
#define CR12 (2.0)
#define CR13 (-0.5)
#define CR20 (-0.5)
#define CR21 (0.0)
#define CR22 (0.5)
#define CR23 (0.0)
#define CR30 (0.0)
#define CR31 (1.0)
#define CR32 (0.0)
#define CR33 (0.0)

/*
 * Based off of the C code given in Texturing & Modeling, 
 * Third Edition, pp. 34-35.
 */
float spline(in float x, in int nknots, in float knots[4]) {
	int nspans = nknots - 3;
	if (nspans < 1) {
		//there must be at least one span
		return 0.0;
	} else if (x <= 0.0) {
			return knots[1];
	} else if (x >= 1.0) {
			return knots[nknots-2];
	} else {
		float y = clamp(x, 0.0, 1.0) * float(nspans);
		int span = int(y);
		if (span >= nknots) {
			// I think this means x was greater than or equal to 1
			span = nspans;
		}

	
		y -= float(span);

		float c3 = CR00*knots[span+0] + CR01*knots[span+1] + CR02*knots[span+2] + CR03*knots[span+3];
		float c2 = CR10*knots[span+0] + CR11*knots[span+1] + CR12*knots[span+2] + CR13*knots[span+3];
		float c1 = CR20*knots[span+0] + CR21*knots[span+1] + CR22*knots[span+2] + CR23*knots[span+3];
		float c0 = CR30*knots[span+0] + CR31*knots[span+1] + CR32*knots[span+2] + CR33*knots[span+3];
		return ((c3*y + c2)*y +c1)*y + c0;}
}

// This function takes a value between -1 and 1, and moves it to between 0 and 1
float unsign(float x) {
	return x * 0.5 + 0.5;
}

#define CR00 (-0.5)
#define CR01 (1.5)
#define CR02 (-1.5)
#define CR03 (0.5)
#define CR10 (1.0)
#define CR11 (-2.5)
#define CR12 (2.0)
#define CR13 (-0.5)
#define CR20 (-0.5)
#define CR21 (0.0)
#define CR22 (0.5)
#define CR23 (0.0)
#define CR30 (0.0)
#define CR31 (1.0)
#define CR32 (0.0)
#define CR33 (0.0)

vec4 spline(float x, int nknots, vec4 knots[25]) {
	int nspans = nknots - 3;
	if (nspans < 1) {
		//there must be at least one span
		return vec4(0.0);
	} else if (x < 0.0) {
			return knots[1];
	} else if (x >= 1.0) {
			return knots[nknots-2];
	} else {
		vec4 val0, val1, val2, val3;
		if (x < 1.0/float(nspans)) {
			val0 = knots[0];
			val1 = knots[1];
			val2 = knots[2];
			val3 = knots[3];
		} else if (x < 2.0/float(nspans)) {
			val0 = knots[1];
			val1 = knots[2];
			val2 = knots[3];
			val3 = knots[4];
		} else if (x < 3.0/float(nspans)) {
			val0 = knots[2];
			val1 = knots[3];
			val2 = knots[4];
			val3 = knots[5];
		} else if (x < 4.0/float(nspans)) {
			val0 = knots[3];
			val1 = knots[4];
			val2 = knots[5];
			val3 = knots[6];
		} else if (x < 5.0/float(nspans)) {
			val0 = knots[4];
			val1 = knots[5];
			val2 = knots[6];
			val3 = knots[7];
		} else if (x < 6.0/float(nspans)) {
			val0 = knots[5];
			val1 = knots[6];
			val2 = knots[7];
			val3 = knots[8];
		} else if (x < 7.0/float(nspans)) {
			val0 = knots[6];
			val1 = knots[7];
			val2 = knots[8];
			val3 = knots[9];
		} else if (x < 8.0/float(nspans)) {
			val0 = knots[7];
			val1 = knots[8];
			val2 = knots[9];
			val3 = knots[10];
		} else if (x < 9.0/float(nspans)) {
			val0 = knots[8];
			val1 = knots[9];
			val2 = knots[10];
			val3 = knots[11];
		} else if (x < 10.0/float(nspans)) {
			val0 = knots[9];
			val1 = knots[10];
			val2 = knots[11];
			val3 = knots[12];
		} else if (x < 11.0/float(nspans)) {
			val0 = knots[10];
			val1 = knots[11];
			val2 = knots[12];
			val3 = knots[13];
		} else if (x < 12.0/float(nspans)) {
			val0 = knots[11];
			val1 = knots[12];
			val2 = knots[13];
			val3 = knots[14];
		} else if (x < 13.0/float(nspans)) {
			val0 = knots[12];
			val1 = knots[13];
			val2 = knots[14];
			val3 = knots[15];
		} else if (x < 14.0/float(nspans)) {
			val0 = knots[13];
			val1 = knots[14];
			val2 = knots[15];
			val3 = knots[16];
		} else if (x < 15.0/float(nspans)) {
			val0 = knots[14];
			val1 = knots[15];
			val2 = knots[16];
			val3 = knots[17];
		} else if (x < 16.0/float(nspans)) {
			val0 = knots[15];
			val1 = knots[16];
			val2 = knots[17];
			val3 = knots[18];
		} else if (x < 17.0/float(nspans)) {
			val0 = knots[16];
			val1 = knots[17];
			val2 = knots[18];
			val3 = knots[19];
		} else if (x < 18.0/float(nspans)) {
			val0 = knots[17];
			val1 = knots[18];
			val2 = knots[19];
			val3 = knots[20];
		} else if (x < 19.0/float(nspans)) {
			val0 = knots[18];
			val1 = knots[19];
			val2 = knots[20];
			val3 = knots[21];
		} else if (x < 20.0/float(nspans)) {
			val0 = knots[19];
			val1 = knots[20];
			val2 = knots[21];
			val3 = knots[22];
		} else if (x < 21.0/float(nspans)) {
			val0 = knots[20];
			val1 = knots[21];
			val2 = knots[22];
			val3 = knots[23];
		} else {
			val0 = knots[21];
			val1 = knots[22];
			val2 = knots[23];
			val3 = knots[24];
		}
		
		float y = fract(clamp(x, 0.0, 1.0) * float(nspans));
		
		vec4 c3 = CR00*val0 + CR01*val1 + CR02*val2 + CR03*val3;
		vec4 c2 = CR10*val0 + CR11*val1 + CR12*val2 + CR13*val3;
		vec4 c1 = CR20*val0 + CR21*val1 + CR22*val2 + CR23*val3;
		vec4 c0 = CR30*val0 + CR31*val1 + CR32*val2 + CR33*val3;
		
		//return (val0 + val1 + val2 + val3)/4.0;
		return ((c3*y + c2)*y +c1)*y + c0;
		//return c1;
	}
}

// we should be able to reuse the logic of the vec4 version
// just stuff the smaller ones into vec4s, then use that
// version, then pull it out
float spline(float x, int nknots, float knots[25]) {
	vec4 v[25];
	
	// I wish i could use a loop here and let the compiler
	// unroll it.
	v[0] = vec4(knots[0]);
	v[1] = vec4(knots[1]);
	v[2] = vec4(knots[2]);
	v[3] = vec4(knots[3]);
	v[4] = vec4(knots[4]);
	v[5] = vec4(knots[5]);
	v[6] = vec4(knots[6]);
	v[7] = vec4(knots[7]);
	v[8] = vec4(knots[8]);
	v[9] = vec4(knots[9]);
	v[10] = vec4(knots[10]);
	v[11] = vec4(knots[11]);
	v[12] = vec4(knots[12]);
	v[13] = vec4(knots[13]);
	v[14] = vec4(knots[14]);
	v[15] = vec4(knots[15]);
	v[16] = vec4(knots[16]);
	v[17] = vec4(knots[17]);
	v[18] = vec4(knots[18]);
	v[19] = vec4(knots[19]);
	v[20] = vec4(knots[20]);
	v[21] = vec4(knots[21]);
	v[22] = vec4(knots[22]);
	v[23] = vec4(knots[23]);
	v[24] = vec4(knots[24]);
	
	float res = spline(x, nknots, v).x;
	
	return res;
}

vec2 spline(float x, int nknots, vec2 knots[25]) {
	vec4 v[25];
	
	// I wish i could use a loop here and let the compiler
	// unroll it.
	v[0] = vec4(knots[0], 0.0, 0.0);
	v[1] = vec4(knots[1], 0.0, 0.0);
	v[2] = vec4(knots[2], 0.0, 0.0);
	v[3] = vec4(knots[3], 0.0, 0.0);
	v[4] = vec4(knots[4], 0.0, 0.0);
	v[5] = vec4(knots[5], 0.0, 0.0);
	v[6] = vec4(knots[6], 0.0, 0.0);
	v[7] = vec4(knots[7], 0.0, 0.0);
	v[8] = vec4(knots[8], 0.0, 0.0);
	v[9] = vec4(knots[9], 0.0, 0.0);
	v[10] = vec4(knots[10], 0.0, 0.0);
	v[11] = vec4(knots[11], 0.0, 0.0);
	v[12] = vec4(knots[12], 0.0, 0.0);
	v[13] = vec4(knots[13], 0.0, 0.0);
	v[14] = vec4(knots[14], 0.0, 0.0);
	v[15] = vec4(knots[15], 0.0, 0.0);
	v[16] = vec4(knots[16], 0.0, 0.0);
	v[17] = vec4(knots[17], 0.0, 0.0);
	v[18] = vec4(knots[18], 0.0, 0.0);
	v[19] = vec4(knots[19], 0.0, 0.0);
	v[20] = vec4(knots[20], 0.0, 0.0);
	v[21] = vec4(knots[21], 0.0, 0.0);
	v[22] = vec4(knots[22], 0.0, 0.0);
	v[23] = vec4(knots[23], 0.0, 0.0);
	v[24] = vec4(knots[24], 0.0, 0.0);
	
	vec2 res = spline(x, nknots, v).xy;
	
	return res;
}

vec3 spline(float x, int nknots, vec3 knots[25]) {
	vec4 v[25];
	
	// I wish i could use a loop here and let the compiler
	// unroll it.
	v[0] = vec4(knots[0], 0.0);
	v[1] = vec4(knots[1], 0.0);
	v[2] = vec4(knots[2], 0.0);
	v[3] = vec4(knots[3], 0.0);
	v[4] = vec4(knots[4], 0.0);
	v[5] = vec4(knots[5], 0.0);
	v[6] = vec4(knots[6], 0.0);
	v[7] = vec4(knots[7], 0.0);
	v[8] = vec4(knots[8], 0.0);
	v[9] = vec4(knots[9], 0.0);
	v[10] = vec4(knots[10], 0.0);
	v[11] = vec4(knots[11], 0.0);
	v[12] = vec4(knots[12], 0.0);
	v[13] = vec4(knots[13], 0.0);
	v[14] = vec4(knots[14], 0.0);
	v[15] = vec4(knots[15], 0.0);
	v[16] = vec4(knots[16], 0.0);
	v[17] = vec4(knots[17], 0.0);
	v[18] = vec4(knots[18], 0.0);
	v[19] = vec4(knots[19], 0.0);
	v[20] = vec4(knots[20], 0.0);
	v[21] = vec4(knots[21], 0.0);
	v[22] = vec4(knots[22], 0.0);
	v[23] = vec4(knots[23], 0.0);
	v[24] = vec4(knots[24], 0.0);
	
	vec3 res = spline(x, nknots, v).xyz;
	
	return res;
}

////////////////////////////////













float noise(vec4);
float snoise(vec4);
float noise(vec3);
float snoise(vec3);
vec4 marble_color(float);
//vec4 spline(float x, int y, vec4 z[]);

void main() {
	//vec4 color = gl_FrontMaterial.diffuse;
	vec4 matspec = gl_FrontMaterial.specular;
	float shininess = gl_FrontMaterial.shininess;
	vec4 lightspec = gl_LightSource[0].specular;
	vec4 lpos = gl_LightSource[0].position;
	vec4 s = -normalize(pos-lpos); 	//not sure why this needs to 
									// be negated, but it does.
	vec3 light = s.xyz;
	vec3 n = normalize(normal);
	vec3 r = -reflect(light, n);
	r = normalize(r);
	vec3 v = -pos.xyz; // We are in eye coordinates,
					   // so the viewing vector is
					   // [0.0 0.0 0.0] - pos
	v = normalize(v);
	
	float scalelocal;
/*	if (scale == 0.0) {
		scalelocal = 1.0; //default value
	} else {
		scalelocal = scale;
	}*/
	scalelocal = 1.0;

	vec4 tp = gl_TexCoord[0] * scalelocal;
	vec3 rp = rawpos.xyz * scalelocal;
	
	// create the grayscale marbling here
	float marble=0.0;
	float f = 1.0;
	for(int i=0; i < NNOISE; i++) {
		marble += noise(rp*f)/f;
		f *= 2.17;
	}
	
	vec4 color;
	color = marble_color(marble);
	
	// for some reason the colors are awfully dark
	// I think it looks better this way
	color *= 2.85;
/*	
	float x = pow(sin(marble * PI) * 0.5 + 0.5, 10.0);
	float y=0.1;
	vec4 matdiffcol= gl_FrontMaterial.diffuse;
	vec4 othercolor = vec4( vec3(1.0)-((vec3(1.0)-matdiffcol.rgb)*y), 1.0);
	//vec4 color = mix(vec4(1.0, 1.0, 1.0, 1.0), matdiffcol, x);
	color = mix(othercolor, matdiffcol, x);
*/
	
	//color = mix(vec4(1.0, 1.0, 1.0, 1.0), vec4(0.4, 0.6, 1.0, 1.0), vec4(marble, marble, marble, 1.0));
//	color = vec4(marble, marble, marble, 1.0);
	
	vec4 diffuse  = color * max(0.0, dot(n, s.xyz)) * gl_LightSource[0].diffuse;
	vec4 specular;
	if (shininess != 0.0) {
		specular = lightspec * matspec * pow(max(0.0, dot(r, v)), shininess);
	} else {
		specular = vec4(0.0, 0.0, 0.0, 0.0);
	}
	
	gl_FragColor = diffuse + specular;
//	gl_FragColor = noise4(pos) != 0.0 ? vec4(1.0, 0.0, 0.0, 1.0) : vec4(0.0, 0.0, 1.0, 1.0);

}

vec4 marble_color(float m) {
	vec4 c[25];
	
	c[0] = PALE_BLUE;
	c[1] = PALE_BLUE;
	c[2] = MEDIUM_BLUE;
	c[3] = MEDIUM_BLUE;
	c[4] = MEDIUM_BLUE;
	c[5] = PALE_BLUE;
	c[6] = PALE_BLUE;
	c[7] = DARK_BLUE;
	c[8] = DARK_BLUE;
	c[9] = DARKER_BLUE;
	c[10] = DARKER_BLUE;
	c[11] = PALE_BLUE;
	c[12] = DARKER_BLUE;
	
	vec4 res = spline(clamp(2.0*m + 0.75, 0.0, 1.0), 13, c);
	
	return res;
}










/*
	//Get texture color
	vec4 texCol = texture3D(tex, texCoords);
	float alpha = texCol.w;

	//Colors to interpolate
	vec4 oldCol = gl_FragColor;
	vec4 newCol = texCol.wwww;

	//Interpolate
//	gl_FragColor = alpha*newCol+(1.0-alpha)*oldCol;

//No interpol
newCol.w=1.0;
	gl_FragColor = newCol;
	
	
	*/
	
	/*
	vec4 texCol = texture3D(tex, texCoords);
	float alpha = texCol.w;
//	vec4 newCol = gl_Color;
//	vec4 newCol = vec4(1.0,0.0,0.0,0.0);
	vec4 newCol = vcolor;
	newCol.w=texCol.w;
//	vec4 newCol = texCol.wwww;
	
	gl_FragColor = newCol;
	
	*/	
	
	
	//   gl_FragColor = vec4(0.0, 1.0, 0.0, 1.0);  
	
	//	gl_FragColor = texture3D(tex, texCoords);
	




