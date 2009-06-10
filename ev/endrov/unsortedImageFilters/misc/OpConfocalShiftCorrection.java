/**
 * 
 */
package endrov.unsortedImageFilters.misc;

import endrov.flow.OpSlice1;
import endrov.imageset.EvPixels;

/**
 * Correct for horizontal shift when using a confocal microscope
 * 
 * @author Johan Henriksson
 */
public class OpConfocalShiftCorrection extends OpSlice1
	{
	//TODO interpolation for fractional shift not implemented
	private double shift;
	
	public OpConfocalShiftCorrection(double shift)
		{
		this.shift = shift;
		}
	
	public EvPixels exec1(EvPixels... p)
		{
		return shift(p[0], shift);
		}
	
	public static EvPixels shift(EvPixels a, double shift)
		{
		int b=(int)shift;
		
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_DOUBLE, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		double[] aPixels=a.getArrayDouble();
		double[] outPixels=out.getArrayDouble();
		
		if(b>0)
			for(int ay=0;ay<h;ay++)
				for(int ax=b;ax<w;ax++)
					{
					int pos=a.getPixelIndex(ax, ay);
					outPixels[pos-b]=aPixels[pos];
					}
		else
			for(int ay=0;ay<h;ay++)
				for(int ax=0;ax<w+b;ax++)
					{
					int pos=a.getPixelIndex(ax, ay);
					outPixels[pos-b]=aPixels[pos];
					}
			
		return out;
		}
}