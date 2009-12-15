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

package endrov.frivolous.model;

public class DiffractionPSF {
	// Constants for Bessel function approximation.
	private static double[] t = new double[] { 
		 1,
		-2.2499997,
		 1.2656208,
		-0.3163866,
		 0.0444479,
		-0.0039444,
		 0.0002100 };
	private static double[] p = new double[] {
		-0.78539816,
		-0.04166397,
		-0.00003954,
		 0.00262573,
		-0.00054125,
		-0.00029333,
		 0.00013558 };
	private static double[] f = new double[] {
		 0.79788456,
		-0.00000077,
		-0.00552740,
		-0.00009512,
		 0.00137237,
		-0.00072805,
		 0.00014476 };

	public static float[] createPSF(Settings_new settings) {

		double lambda = settings.lambda;
		double indexRefr = settings.indexRefr;
		double pixelSpacing = settings.pixelSpacing;
		double na = settings.na;
		double sa = settings.sa;
		int w = settings.w;
		int h = settings.h;
		double offsetZ = settings.offsetZ;

		int stepsPerCycle = 8;

		int ic = w / 2;
		int jc = h / 2;

		float[] pixels = new float[w * h];
		int rMax = 2 + (int) Math.sqrt(ic * ic + jc * jc);
		float[] integral = new float[rMax];
		double upperLimit = Math.tan(Math.asin(na / indexRefr));
		double waveNumber = 2 * Math.PI * indexRefr / lambda;
		double kz = waveNumber * offsetZ;
		for (int r = 0; r < rMax; r++) {
			double kr = waveNumber * r * pixelSpacing;
			int numCyclesJ = 1 + (int) (kr * upperLimit / 3);
			int numCyclesCos = 1 + (int) (Math.abs(kz) * 0.36 * upperLimit / 6);
			int numCycles = numCyclesJ;
			if (numCyclesCos > numCycles)
				numCycles = numCyclesCos;
			int nStep = 2 * stepsPerCycle * numCycles;
			int m = nStep / 2;
			double step = upperLimit / nStep;
			double sumR = 0;
			double sumI = 0;
			// Simpson's rule
			// Assume that the sperical aberration varies with the (%
			// aperture)^4
			// f(a) = f(0) = 0, so no contribution
			double u = 0;
			double bessel = 1;
			double root = 1;
			double angle = kz;
			// 2j terms
			for (int j = 1; j < m; j++) {
				u = 2 * j * step;
				kz = waveNumber * (offsetZ + // (k - kc)*sliceSpacing +
						sa * (u / upperLimit) * (u / upperLimit)
								* (u / upperLimit) * (u / upperLimit));
				root = Math.sqrt(1 + u * u);
				bessel = J0(kr * u / root);
				angle = kz / root;
				sumR += 2 * Math.cos(angle) * u * bessel / 2;
				sumI += 2 * Math.sin(angle) * u * bessel / 2;
			}

			// 2j - 1 terms
			for (int j = 1; j <= m; j++) {
				u = (2 * j - 1) * step;
				kz = waveNumber * (offsetZ + // (k - kc)*sliceSpacing +
						sa * (u / upperLimit) * (u / upperLimit)
								* (u / upperLimit) * (u / upperLimit));
				root = Math.sqrt(1 + u * u);
				bessel = J0(kr * u / root);
				angle = kz / root;
				sumR += 4 * Math.cos(angle) * u * bessel / 2;
				sumI += 4 * Math.sin(angle) * u * bessel / 2;
			}

			// f(b)
			u = upperLimit;
			kz = waveNumber * (offsetZ + sa);// (k - kc)*sliceSpacing + sa);
			root = Math.sqrt(1 + u * u);
			bessel = J0(kr * u / root);
			angle = kz / root;
			sumR += Math.cos(angle) * u * bessel / 2;
			sumI += Math.sin(angle) * u * bessel / 2;

			integral[r] = (float) (step * step * (sumR * sumR + sumI * sumI) / 9);
		}

		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				double rPixels = Math.sqrt((i - ic) * (i - ic) + (j - jc)
						* (j - jc));
				pixels[i + w * j] = interp(integral, (float) rPixels);
			}
		}

		int n = w * h;

		float area = 0;
		for (int ind = 0; ind < n; ind++) {
			area += pixels[ind];
		}
		System.out.println(area);
		for (int ind = 0; ind < n; ind++) {
			pixels[ind] /= area;
		}
		shiftQuadrants(w, h, pixels);
		return pixels;
	}

	static float interp(float[] y, float x) {
		int i = (int) x;
		float fract = x - i;
		return (1 - fract) * y[i] + fract * y[i + 1];
	}

	// Bessel function J0(x). Uses the polynomial approximations on p. 369-70 of
	// Abramowitz & Stegun
	// The error in J0 is supposed to be less than or equal to 5 x 10^-8.
	static double J0(double xIn) {
		double x = xIn;
		if (x < 0)
			x *= -1;
		double r;
		if (x <= 3) {
			double y = x * x / 9;
			r = t[0]
					+ y
					* (t[1] + y
							* (t[2] + y
									* (t[3] + y
											* (t[4] + y * (t[5] + y * t[6])))));
		} else {
			double y = 3 / x;
			double theta0 = x
					+ p[0]
					+ y
					* (p[1] + y
							* (p[2] + y
									* (p[3] + y
											* (p[4] + y * (p[5] + y * p[6])))));
			double f0 = f[0]
					+ y
					* (f[1] + y
							* (f[2] + y
									* (f[3] + y
											* (f[4] + y * (f[5] + y * f[6])))));
			r = Math.sqrt(1 / x) * f0 * Math.cos(theta0);
		}
		return r;
	}

	static void shiftQuadrants(int w, int h, float[] x) {
		// TODO: göra en bättre shiftning!
		int k1P, k2P;
		float temp;
		int wHalf = w / 2;
		int hHalf = h / 2;

		for (int k2 = 0; k2 < hHalf; k2++) {
			k2P = k2 + hHalf;
			for (int k1 = 0; k1 < w; k1++) {
				temp = x[k1 + w * k2];
				x[k1 + w * k2] = x[k1 + w * k2P];
				x[k1 + w * k2P] = temp;
			}
		}
		for (int k1 = 0; k1 < wHalf; k1++) {
			k1P = k1 + wHalf;
			for (int k2 = 0; k2 < h; k2++) {
				temp = x[k1 + w * k2];
				x[k1 + w * k2] = x[k1P + w * k2];
				x[k1P + w * k2] = temp;
			}
		}
	}
}