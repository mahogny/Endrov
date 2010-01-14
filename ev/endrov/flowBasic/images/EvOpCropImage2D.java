/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.images;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;


/**
 * Crop image to fit within limits
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpCropImage2D extends EvOpSlice1
	{
	private int fromX, toX, fromY, toY;
	
	public EvOpCropImage2D(int fromX, int toX, int fromY, int toY)
		{
		this.fromX = fromX;
		this.toX = toX;
		this.fromY = fromY;
		this.toY = toY;
		}

	public EvPixels exec1(EvPixels... p)
		{
		return apply(p[0]);
		}
	
	public EvPixels apply(EvPixels p)
		{
		return crop(p,fromX,toX,fromY,toY);
		}
	
	
	
	
	/**
	 * Crop one single 2D plane. Takes pixels fromX <= x < toX.
	 * Area must be within bounds
	 */
	public static EvPixels crop(EvPixels p, int fromX, int toX, int fromY, int toY)
		{
		p=p.convertToDouble(true);
		int width=p.getWidth();
		
		int newWidth=toX-fromX;
		int newHeight=toY-fromY;
		EvPixels newPixels=new EvPixels(EvPixelsType.DOUBLE, newWidth, newHeight);
		
		double[] inarr=p.getArrayDouble();
		double[] outarr=newPixels.getArrayDouble();
		for(int ay=0;ay<newHeight;ay++)
			for(int ax=0;ax<newWidth;ax++)
				outarr[ay*newWidth+ax]=inarr[(ay+fromY)*width+(ax+fromX)];
		
		return newPixels;
		}
	

	
	
	}
