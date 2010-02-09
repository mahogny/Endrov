/***
 * Copyright (C) 2010 David Johansson & Arvid Johansson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverFrivolous;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * Utility functions
 * @author David Johansson, Arvid Johansson
 *
 */
public class FrivolousUtility
	{

	public static final int COLOR_ALPHA = 3;
	public static final int COLOR_RED = 0;
	public static final int COLOR_GREEN = 1;
	public static final int COLOR_BLUE = 2;

	public static FrivolousComplexArray getComplexImage(String file_name, int color)
		{
		BufferedImage img = null;
		try
			{
			img = ImageIO.read(new File(file_name));
			}
		catch (IOException e)
			{
			new FileNotFoundException("File could not be opened.");
			}
		return getComplexImage(img, color);
		}

	public static FrivolousComplexArray getComplexImage(BufferedImage img, int color)
		{
		int w = img.getWidth();
		int h = img.getHeight();
		return new FrivolousComplexArray(getColorArray(img, w, h, color), null, w, h);
		}

	public static FrivolousComplexArray getComplexImage(BufferedImage img)
		{
		return getComplexImage(img, COLOR_RED);
		}

	public static int[] getIntColorArray(BufferedImage img, int color)
		{
		return getIntColorArray(img, img.getWidth(), img.getHeight(), color);
		}

	public static int[] getIntColorArray(BufferedImage img, int w, int h,
			int color)
		{
		int[] array = null;
		switch (color)
			{
			case COLOR_ALPHA:
				array = img.getAlphaRaster().getSamples(0, 0, w, h, 0, new int[w*h]);
				break;
			case COLOR_RED:
				array = img.getRaster().getSamples(0, 0, w, h, COLOR_RED, new int[w*h]);
				break;
			case COLOR_GREEN:
				array = img.getRaster().getSamples(0, 0, w, h, COLOR_GREEN,
						new int[w*h]);
				break;
			case COLOR_BLUE:
				array = img.getRaster()
						.getSamples(0, 0, w, h, COLOR_BLUE, new int[w*h]);
				break;
			default:
				throw new IllegalArgumentException("Wrong color value.");
			}
		return array;
		}

	public static float[] getColorArray(BufferedImage img, int color)
		{
		return getColorArray(img, img.getWidth(), img.getHeight(), color);
		}

	public static float[] getColorArray(BufferedImage img, int w, int h, int color)
		{
		float[] array = null;
		switch (color)
			{
			case COLOR_ALPHA:
				array = img.getAlphaRaster().getSamples(0, 0, w, h, 0, new float[w*h]);
				break;
			case COLOR_RED:
				array = img.getRaster().getSamples(0, 0, w, h, COLOR_RED,
						new float[w*h]);
				break;
			case COLOR_GREEN:
				array = img.getRaster().getSamples(0, 0, w, h, COLOR_GREEN,
						new float[w*h]);
				break;
			case COLOR_BLUE:
				array = img.getRaster().getSamples(0, 0, w, h, COLOR_BLUE,
						new float[w*h]);
				break;
			default:
				throw new IllegalArgumentException("Wrong color value.");
			}
		return array;
		}

	public static BufferedImage getImageFromComplex(FrivolousComplexArray complex,	boolean fromImaginary)
		{
		float[] imgPxls = (!fromImaginary ? complex.real : complex.imag);
		int w = complex.width;
		int h = complex.height;
		/*
		 * int[] rgb = new int[w*h]; for(int i=0;i<complex.length;i++){ int pxl =
		 * (int)imgPxls[i]; if(pxl>255) pxl = 255; else if(pxl<0) pxl = 0; rgb[i]=
		 * 0xFF<<24 | pxl<<16 | pxl<<8 | pxl; }
		 */

		BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
		result.getRaster().setPixels(0, 0, w, h, imgPxls);// setRGB(0,0,w,h,rgb,0,w);

		return result;
		}
	
	
	
	//private static double max_lambda = -1;
	private static Random rnd;

	public static FrivolousComplexArray addRealNoise(FrivolousComplexArray input, FrivolousSettings settings)
		{
		double lambda = settings.parameter_noise_lambda;
		rnd = new Random();

		float[] real = new float[input.length];

		for (int i = 0; i<input.length; i++)
			{
			real[i] = input.real[i]+(float) poisson(lambda);
			if (real[i]>255)
				real[i] = 255;
			else if (real[i]<0)
				real[i] = 0;
			}

		return new FrivolousComplexArray(real, input.imag, input.width, input.height);
		}

	
	
	private static double poisson(double lambda)
		{
		if (lambda<10)
			{
			/*
			 * Make the real poisson calculation (very time consuming for large lambda)
			 */
			double L = Math.exp(-lambda);
			int k = 0;
			double p = 1;
			do
				{
				p = p*Math.random();
				k++;
				} while (p>L);

			return k-1;
			}
		else
			{
			/*
			 * Approximating with normal distribution using built in Gaussian function when lambda > 10
			 */

			double n = rnd.nextGaussian();
			double p = n*Math.sqrt(lambda)+lambda;

			return p;
			}
		}
	
	
	
	
	}
