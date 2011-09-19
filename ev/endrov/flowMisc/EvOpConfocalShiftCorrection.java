/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
/**
 * 
 */
package endrov.flowMisc;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.ProgressHandle;

/**
 * Correct for horizontal shift when using a confocal microscope
 * 
 * @author Johan Henriksson
 */
public class EvOpConfocalShiftCorrection extends EvOpSlice1
	{
	//TODO interpolation for fractional shift not implemented
	private final Number shift;
	
	public EvOpConfocalShiftCorrection(Number shift)
		{
		this.shift = shift;
		}
	
	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return shift(ph, p[0], shift);
		}
	
	public static EvPixels shift(ProgressHandle ph, EvPixels a, Number shift)
		{
		int b=shift.intValue();
		
		//Should use the common higher type here
		a=a.getReadOnly(EvPixelsType.DOUBLE);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		double[] aPixels=a.getArrayDouble();
		double[] outPixels=out.getArrayDouble();
		
		if(b>0)
			for(int ay=0;ay<h;ay++)
				{
				if(ay%2==0)
					for(int ax=b;ax<w;ax++)
						{
						int pos=a.getPixelIndex(ax, ay);
						outPixels[pos-b]=aPixels[pos];
						}
				else
					{
					int i=a.getPixelIndex(0, ay);
					System.arraycopy(aPixels,i,outPixels,i,w);
					}
				}
		else
			for(int ay=0;ay<h;ay++)
				{
				if(ay%2==0)
				for(int ax=0;ax<w+b;ax++)
					{
					int pos=a.getPixelIndex(ax, ay);
					outPixels[pos-b]=aPixels[pos];
					}
				else
					{
					int i=a.getPixelIndex(0, ay);
					System.arraycopy(aPixels,i,outPixels,i,w);
					}
				}
			
		return out;
		}
}