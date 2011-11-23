/***
 * Copyright (C) 2010 David Johansson & Arvid Johansson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareFrivolous;

/**
 * @author David Johansson, Arvid Johansson, Johan Henriksson
 */
public class FrivolousPSFGaussian extends FrivolousPSF
	{

	@Override
	public float[] createPSF(FrivolousSettingsNew settings)
		{
		int w = settings.w;
		int h = settings.h;
		int wc = w>>1;
		int hc = h>>1;

		double z = settings.offsetZ; //Only calculate current plane

		double sigma = 1/(2*Math.PI*settings.na)*1000;
		double twoS2 = 2*sigma*sigma;

		float[] psf = new float[w*h];
		float total = 0;
		for (int x = 0; x<w; x++)
			for (int y = 0; y<h; y++)
				{
				double d2 = getD2(x-wc, y-hc, z, settings.pixelSpacing);
				double d22 = getD2(x-wc, y-hc, 0, settings.pixelSpacing);
				psf[x*h+y] = (float) Math.exp(-d2/twoS2);
				total += (float) Math.exp(-d22/twoS2);
				}

		//Normalize
		for (int x = 0; x<w; x++)
			for (int y = 0; y<h; y++)
				psf[x*h+y] /= total;

		FrivolousFourier.shiftQuadrants(w, h, psf);

		return psf;
		}

	private static double getD2(int xPixel, int yPixel, double z,	double pixelSpacing)
		{
		double x = xPixel*pixelSpacing;
		double y = yPixel*pixelSpacing;
		return x*x+y*y+z*z;
		}
	}
