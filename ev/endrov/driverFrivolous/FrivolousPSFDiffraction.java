/* Bob Dougherty, OptiNav, Inc.  Plugin to compute the 3D point spread function of a diffraction limited
microscope.
Version 0	May2, 2005
Version 1   May 4, 2005.  Fixed bug on center and large z, applied symmetry in z.
Version 1.1 May 4, 2005.  Simpson's rule instead of trapezoidal rule for more speed.
Version 1.2 May 5, 2005.  Changed inputs to n*sin(theta), lambda, and n
Version 2	May 6, 2005.  Added spherical aberration.
 */
/*	License:
 Copyright (c) 2005, OptiNav, Inc.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 Neither the name of OptiNav, Inc. nor the names of its contributors
 may be used to endorse or promote products derived from this software
 without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package endrov.driverFrivolous;

public class FrivolousPSFDiffraction extends FrivolousPSF
	{

	public float[] createPSF(FrivolousSettingsNew settings)
		{
		//long curTime=System.currentTimeMillis();

		double lambda = settings.lambda; // Wavelength in um
		double n = settings.indexRefr; // Index of refraction
		double dRho = settings.pixelSpacing; // Resolution XY (pixel spacing)
		double na = settings.na; // Numerical aperture (na = n * sin(theta))
		int w = settings.w; // PSF width in pixels
		int h = settings.h; // PSF height in pixels
		double offsetZ = settings.offsetZ; // Z offset in um

		int xMax = w/2;
		int yMax = h/2;
		int rMax = 2+(int) Math.sqrt(xMax*xMax+yMax*yMax); // Maximal radius of PSF
																												// in pixels
		double thetaMax = Math.asin(na/n); // The maximal angle (Theoretically
																				// between 0 < tM < pi/2

		int integralSteps = 100; // Smoothness of the integral
		double k = 2*Math.PI*n/lambda; // The wavenumber

		double[] sumR = new double[rMax]; // For storing the real part of the
																			// integral
		double[] sumI = new double[rMax]; // For storing the imaginary part ot the
																			// integral

		for (int thetaIndex = 0; thetaIndex<=integralSteps; thetaIndex++)
			{
			double theta = thetaIndex*thetaMax/integralSteps;

			double cosTheta = Math.cos(theta);
			double sinTheta = Math.sin(theta);
			double sqrCosTheta = Math.sqrt(cosTheta);

			double koffsetZcosTheta=k*offsetZ*cosTheta;
			double eR = Math.cos(koffsetZcosTheta);
			double eI = -Math.sin(koffsetZcosTheta);

			for (int r = 0; r<rMax; r++)
				{
				double rho = r*dRho;

				double j0 = J0(k*rho*sinTheta);

				double sqrCosThetaj0sinTheta=sqrCosTheta*j0*sinTheta;
				sumR[r] += eR*sqrCosThetaj0sinTheta;// * thetaMax / integralSteps;
				sumI[r] += eI*sqrCosThetaj0sinTheta;// * thetaMax / integralSteps;
				}
			}

		float[] line = new float[rMax];
		for (int r = 0; r<rMax; r++)
			{
			line[r] = (float) (thetaMax/integralSteps*thetaMax/integralSteps*(sumR[r]*sumR[r]+sumI[r]*sumI[r]));
			}

		float[] pixels = new float[w*h];
		float area = 0;
		for (int j = 0; j<h/2; j++)
		//for (int j = 0; j<h; j++)
			{
			double dj=j-h/2;
			double dj2=dj*dj;
			for (int i = 0; i<w/2; i++)
		//	for (int i = 0; i<w; i++)
				{
				double di=i-w/2;
				double d = Math.sqrt(di*di+dj2);
				pixels[i+w*j] = interp(line, (float) d);
				area += pixels[i+w*j];
				}
			}
		
		//Assume size a factor of 2, then use 2-fold symmetry
		for(int j = 0;j<h/2;j++)
			for(int i = w/2;i<w;i++)
				pixels[i+w*j]=pixels[(w-1-i)+w*j];
		for(int j = h/2;j<h;j++)
			for(int i = 0;i<w;i++)
				pixels[i+w*j]=pixels[i+w*(h-1-j)];
		area*=4;
		
		//System.out.println(area);
		for (int ind = 0; ind<pixels.length; ind++)
			{
			pixels[ind] /= 36.701687;// TODO: area;
			}
		float[] output = new float[1024*1024];
		FrivolousFourier.shiftQuadrants(w, h, pixels, 1024, 1024, output);
		
		long curTime2=System.currentTimeMillis();
		//System.out.println(curTime2-curTime);
		
		return output;
		}

	private float interp(float[] y, float x)
		{
		int i = (int) x;
		float fract = x-i;
		return (1-fract)*y[i]+fract*y[i+1];
		}

	// Bessel function J0(x). Uses the polynomial approximations on p. 369-70 of
	// Abramowitz & Stegun
	// The error in J0 is supposed to be less than or equal to 5 x 10^-8.
	private double J0(double xIn)
		{
		// Constants for Bessel function approximation.
		final double[] t = new double[]
			{ 1, -2.2499997, 1.2656208, -0.3163866, 0.0444479, -0.0039444, 0.0002100 };
		final double[] p = new double[]
			{ -0.78539816, -0.04166397, -0.00003954, 0.00262573, -0.00054125,
					-0.00029333, 0.00013558 };
		final double[] f = new double[]
			{ 0.79788456, -0.00000077, -0.00552740, -0.00009512, 0.00137237,
					-0.00072805, 0.00014476 };

		double x = xIn;
		if (x<0)
			x = -x;
		//double r;
		if (x<=3)
			{
			double y = x*x/9.0;
			return t[0]+y*(t[1]+y*(t[2]+y*(t[3]+y*(t[4]+y*(t[5]+y*t[6])))));
			}
		else
			{
			double y = 3.0/x;
			double theta0 = x+p[0]+y*(p[1]+y*(p[2]+y*(p[3]+y*(p[4]+y*(p[5]+y*p[6])))));
			double f0 = f[0]+y*(f[1]+y*(f[2]+y*(f[3]+y*(f[4]+y*(f[5]+y*f[6])))));
			return Math.sqrt(1.0/x)*f0*Math.cos(theta0);
			}
		//return r;
		}
	}
