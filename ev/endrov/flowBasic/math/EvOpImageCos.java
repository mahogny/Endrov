package endrov.flowBasic.math;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * cos(A)
 * @author Johan Henriksson
 *
 */
public class EvOpImageCos extends EvOpSlice1
	{
	public EvPixels exec1(EvPixels... p)
		{
		return EvOpImageCos.log(p[0]);
		}

	/**
	 * log(A)
	 */
	static EvPixels log(EvPixels a)
		{
		//Should use the common higher type here
		a=a.getReadOnly(EvPixelsType.DOUBLE);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		double[] aPixels=a.getArrayDouble();
		double[] outPixels=out.getArrayDouble();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=Math.cos(aPixels[i]);
		
		return out;
		}
	}