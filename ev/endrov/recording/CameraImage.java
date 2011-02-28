/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;

import java.awt.image.BufferedImage;

import endrov.imageset.EvPixels;

/**
 * Image from camera
 * 
 * should later be changed to an EV image
 * 
 * @author Johan Henriksson
 *
 */
public class CameraImage
	{
	public int w,h;
	public int bytesPerPixel;
	public Object pixels; //byte[] or short[] or int[]
	public int numComponents;
	
	
	public CameraImage(int w, int h, int bytesPerPixel, Object pixels, int numComponents)
		{
		this.w = w;
		this.h = h;
		this.bytesPerPixel = bytesPerPixel;
		this.pixels = pixels;
		this.numComponents = numComponents;
		}

	public String toString()
		{
		return ""+w+" x "+h;
		}
	
	
	/**
	 * Get pixel data from camera
	 */
	public EvPixels[] getPixels()
		{
		if(pixels instanceof BufferedImage)
			{
			return new EvPixels[]{new EvPixels((BufferedImage)pixels)};
			}
		else
			{
			
			if(numComponents==1)
				{
				if(pixels instanceof byte[])
				//if(bytesPerPixel==1)
					return new EvPixels[]{EvPixels.createFromUByte(w, h, (byte[])pixels)};
				else if(pixels instanceof short[])
				//else if(bytesPerPixel==2)
					return new EvPixels[]{EvPixels.createFromShort(w, h, (short[])pixels)};
				else if(pixels instanceof int[])
				//else if(bytesPerPixel==4)
					return new EvPixels[]{EvPixels.createFromInt(w, h, (int[])pixels)};
				}
			else
				{
				
				
				if(bytesPerPixel==4)
					{
					//Always delivered as a packed int?
					if(numComponents==3)   //always 3?
						{
						int[] p=(int[])pixels;
						int[] r=new int[p.length];
						int[] g=new int[p.length];
						int[] b=new int[p.length];
						
						for(int i=0;i<p.length;i++)
							{
							r[i]=(p[i] & 0xFF);
							g[i]=(p[i] & 0xFF00)>>8;
							b[i]=(p[i] & 0xFF0000)>>16; 
							if(b[i]<0) //TODO: negative values?
								b[i]+=128;
							}
						
						return new EvPixels[]{
								EvPixels.createFromInt(w, h, (int[])r),
								EvPixels.createFromInt(w, h, (int[])g),
								EvPixels.createFromInt(w, h, (int[])b)
							};
						}
					}
				
				
				}
			}
		
		System.out.println("Uncovered pixel type "+bytesPerPixel+" "+numComponents);
		return null;
		}

	public int getWidth()
		{
		return w;
		}

	public int getHeight()
		{
		return h;
		}

	public int getBytesPerPixel()
		{
		return bytesPerPixel;
		}

	public int getNumComponents()
		{
		return numComponents;
		}
	
	
	
	
	
	}
