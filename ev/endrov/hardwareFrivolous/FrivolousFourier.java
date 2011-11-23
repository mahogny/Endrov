/***
 * Copyright (C) 2010 David Johansson & Arvid Johansson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareFrivolous;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

/**
 * @author David Johansson, Arvid Johansson
 */
public class FrivolousFourier
	{
	private int w, h;
	private FloatFFT_2D fourierTransform;

	public FrivolousFourier(int w, int h)
		{
		this.w = w;
		this.h = h;
		fourierTransform = new FloatFFT_2D(w, h);
		}

	public FrivolousComplexArray forward(FrivolousComplexArray complex, boolean real)
		{
		float[] tmp;
		if (real)
			{
			tmp = FrivolousComplexLib.getRealArrayFirstHalf(complex);
			fourierTransform.realForwardFull(tmp);
			}
		else
			{
			tmp = FrivolousComplexLib.getCombinedArray(complex);
			fourierTransform.complexForward(tmp);
			}
		return FrivolousComplexLib.getComplexArray(tmp,w,h);
		}

	public FrivolousComplexArray backward(FrivolousComplexArray complex)
		{
		float[] tmp = FrivolousComplexLib.getCombinedArray(complex);
		fourierTransform.complexInverse(tmp, true);
		return FrivolousComplexLib.getComplexArray(tmp,w,h);
		}

	protected static void shiftQuadrants(int w, int h, float[] x)
	{
	// TODO: göra en bättre shiftning?
	int k1P, k2P;
	float temp;
	int wHalf = w/2;
	int hHalf = h/2;
	
	for (int k2 = 0; k2<hHalf; k2++)
		{
		k2P = k2+hHalf;
		for (int k1 = 0; k1<w; k1++)
			{
			temp = x[k1+w*k2];
			x[k1+w*k2] = x[k1+w*k2P];
			x[k1+w*k2P] = temp;
			}
		}
	for (int k1 = 0; k1<wHalf; k1++)
		{
		k1P = k1+wHalf;
		for (int k2 = 0; k2<h; k2++)
			{
			temp = x[k1+w*k2];
			x[k1+w*k2] = x[k1P+w*k2];
			x[k1P+w*k2] = temp;
			}
		}
	}
	
	protected static void shiftQuadrants(int inputWidth, int inputHeight, float[] input, int outputWidth, int outputHeight, float[] output )
	{
		//First quadrant
		for(int i = inputWidth>>1;i<inputWidth;i++)
			for(int j=inputHeight>>1;j<inputHeight;j++)
				output[(i-(inputWidth>>1))+(j-(inputHeight>>1))*outputWidth] =
					input[i+j*inputWidth];

		//TODO: Second quadrant
		for(int i = 0;i<inputWidth>>1;i++)
			for(int j=inputHeight>>1;j<inputHeight;j++)
				output[(i+outputWidth-(inputWidth>>1))+(j-(inputHeight>>1))*outputWidth] = input[i+j*inputWidth];
		
		//TODO: Third quadrant
		for(int i = inputWidth>>1;i<inputWidth;i++)
			for(int j=0;j<inputHeight>>1;j++)
				output[(i-(inputWidth>>1))+(j+outputWidth-(inputHeight>>1))*outputWidth] = input[i+j*inputWidth];
		
		//TODO: Fourth quadrant
		for(int i = 0;i<inputWidth>>1;i++)
			for(int j=0;j<inputHeight>>1;j++)
				output[(i+outputWidth-(inputWidth>>1))+(j+outputWidth-(inputHeight>>1))*outputWidth] = input[i+j*inputWidth];
	}
}