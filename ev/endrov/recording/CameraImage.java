/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;

import java.awt.image.BufferedImage;

import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;

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
	public String forceFormat;
	
	
	public CameraImage(int w, int h, int bytesPerPixel, Object pixels, int numComponents, String forceFormat)
		{
		this.w = w;
		this.h = h;
		this.bytesPerPixel = bytesPerPixel;
		this.pixels = pixels;
		this.numComponents = numComponents;
		this.forceFormat = forceFormat;
		}
	
	public CameraImage(EvPixels p)
		{
		this.w=p.getWidth();
		this.h=p.getHeight();
		this.numComponents = 1;
		if(p.getType()==EvPixelsType.INT)
			{
			this.bytesPerPixel = 4;
			this.pixels = p.getArrayInt();
			}
		else if(p.getType()==EvPixelsType.SHORT)
			{
			this.bytesPerPixel = 2;
			this.pixels = p.getArrayShort();
			}
		else if(p.getType()==EvPixelsType.UBYTE)
			{
			this.bytesPerPixel = 1;
			this.pixels = p.getArrayUnsignedByte();
			}
		else
			throw new RuntimeException("Unsupported pixel format");
		
		}

	public String toString()
		{
		return ""+w+" x "+h;
		}
	
	
	private EvPixels[] fixFormat(EvPixels[] parr)
		{
		for(int i=0;i<parr.length;i++)
			{
			EvPixelsType curType=parr[i].getType();
			EvPixels p=parr[i];
			if(forceFormat==null || forceFormat.equals("None"))
				{
				//Do nothing
				}
			else if(forceFormat.equals("8-bit int"))
				{
				if(curType!=EvPixelsType.UBYTE)
					{
					p=p.convertToInt(false);
					int arr[]=p.getArrayInt();
	
					//Clamp
					for(int j=0;j<arr.length;j++)
						if(arr[j]>255)
							arr[j]=255;
					
					p=p.convertToUByte(true);
					}
				}
			else if(forceFormat.equals("16-bit int"))
				{
				if(curType!=EvPixelsType.SHORT)
					{
					p=p.convertToInt(false);
					int arr[]=p.getArrayInt();
	
					//Clamp
					for(int j=0;j<arr.length;j++)
						if(arr[j]>32767)
							arr[j]=32767;
					
					p=p.convertToShort(true);
					}
				}
			else if(forceFormat.equals("32-bit int"))
				{
				if(curType!=EvPixelsType.INT)
					p=p.convertToInt(true);
				}
			else
				throw new RuntimeException("Unknown format ("+forceFormat+")");
			parr[i]=p;
			}
		return parr;
		}
	
	/**
	 * Get pixel data from camera
	 */
	public EvPixels[] getPixels()
		{
		if(pixels instanceof BufferedImage)
			{
			return fixFormat(new EvPixels[]{new EvPixels((BufferedImage)pixels)});
			}
		else
			{
			
			if(numComponents==1)
				{
				if(pixels instanceof byte[])
				//if(bytesPerPixel==1)
					return fixFormat(new EvPixels[]{EvPixels.createFromUByte(w, h, (byte[])pixels)});
				else if(pixels instanceof short[])
				//else if(bytesPerPixel==2)
					return fixFormat(new EvPixels[]{EvPixels.createFromShort(w, h, (short[])pixels)});
				else if(pixels instanceof int[])
				//else if(bytesPerPixel==4)
					return fixFormat(new EvPixels[]{EvPixels.createFromInt(w, h, (int[])pixels)});
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
						
						return fixFormat(new EvPixels[]{
								EvPixels.createFromInt(w, h, (int[])r),
								EvPixels.createFromInt(w, h, (int[])g),
								EvPixels.createFromInt(w, h, (int[])b)
							});
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

	/*
	public int getBytesPerPixel()
		{
		return bytesPerPixel;
		}
*/
	public int getNumComponents()
		{
		return numComponents;
		}
	
	
	
	
	
	}
