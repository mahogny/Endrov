package endrov.unsortedImageFilters;

import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.newcore.SliceOp;

/**
 * Correct for horizontal shift when using a confocal microscope
 * 
 * @author Johan Henriksson
 */
public class ConfocalShiftCorrection 
	{

	
	public static class ConfocalShiftCorrectionOp extends SliceOp
		{
		//TODO interpolation for fractional shift not implemented
		double shift;
		public ConfocalShiftCorrectionOp(double shift)
			{
			this.shift = shift;
			}
		
		public EvPixels exec(EvPixels... p)
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
	}
