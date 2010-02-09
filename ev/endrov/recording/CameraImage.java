/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

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
	
	public String toString()
		{
		return ""+w+" x "+h;
		}
	
	/**
	 * Make AWT image out of input
	 * @deprecated
	 */
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
	
	/**
	 * Get pixel data from camera
	 */
	public EvPixels getPixels()
		{
		if(pixels instanceof BufferedImage)
			{
			return new EvPixels((BufferedImage)pixels);
			}
		else if(bytesPerPixel==1)
			{
			return EvPixels.createFromUByte(w, h, (byte[])pixels);
			}
		else if(bytesPerPixel==2)
			{
			return EvPixels.createFromShort(w, h, (short[])pixels);
//			return EvPixels.createFromShort(w, h, CastArray.toShort((byte[])pixels));
			}
		else
			{
			System.out.println("Uncovered pixel type "+bytesPerPixel);
			return null;
			}
		}
	
	
	}
