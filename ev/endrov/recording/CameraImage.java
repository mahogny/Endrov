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
	public Object pixels; //byte[] or short[]
	public int numComponents;
	
	
	public CameraImage(int w, int h, int bytesPerPixel, Object pixels,
			int numComponents)
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
	 * Make AWT image out of input
	 * @deprecated
	 */
	/*
	public BufferedImage getAWT()
		{
		//MM DOES NOT SUPPORT COLOR!!!
		
		BufferedImage im=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r=im.getRaster();
		for(int y=0;y<h;y++)
			{
			//I'm sure this can be made faster, low-level if not otherwise
			int p[]=new int[w];
			if(pixels instanceof BufferedImage)
				{
				return (BufferedImage)pixels;
				}
			else if(bytesPerPixel==1)
				{
				int off=y*w;
				byte[] in=(byte[])pixels;
				for(int x=0;x<w;x++)
					p[x]=in[x+off];
				}
			else if(bytesPerPixel==2)
				{
				int off=y*w;
				short[] in=(short[])pixels;
				for(int x=0;x<w;x++)
					p[x]=in[x+off];
				}
			r.setPixels(0, y, w, 1, p);
			}
		
		
		return im;
		}
	*/
	
	
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
			
			System.out.println("# component "+numComponents);
			
			if(numComponents==1)
				{
				
				if(bytesPerPixel==1)
					{
					return new EvPixels[]{EvPixels.createFromUByte(w, h, (byte[])pixels)};
					}
				else if(bytesPerPixel==2)
					{
					return new EvPixels[]{EvPixels.createFromShort(w, h, (short[])pixels)};
//					return EvPixels.createFromShort(w, h, CastArray.toShort((byte[])pixels));
					}
				else if(bytesPerPixel==4)
					{
					return new EvPixels[]{EvPixels.createFromInt(w, h, (int[])pixels)};
					}
				
				}
			else
				{
				
				
				if(bytesPerPixel==4)
					{
					if(numComponents==3)
						{
						int[] p=(int[])pixels;
						int[] r=new int[p.length];
						int[] g=new int[p.length];
						int[] b=new int[p.length];
						
						for(int i=0;i<p.length;i++)
							{
							r[i]=(p[i] & 0xFF);
							/*
							g[i]=(p[i] & 0xFF00)>>8;
							b[i]=(p[i] & 0xFF0000)>>16; //TODO: negative values?
							if(b[i]<0)
								b[i]+=128;*/
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
		
		System.out.println("Uncovered pixel type "+bytesPerPixel);
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
