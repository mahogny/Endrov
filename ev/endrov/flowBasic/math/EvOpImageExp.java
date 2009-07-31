package endrov.flowBasic.math;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * exp(A)
 * @author Johan Henriksson
 *
 */
public class EvOpImageExp extends EvOpSlice1
	{
	public EvPixels exec1(EvPixels... p)
		{
		return EvOpImageExp.apply(p[0]);
		}

	static EvPixels apply(EvPixels a)
		{
		//Should use the common higher type here
		a=a.getReadOnly(EvPixelsType.DOUBLE);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		double[] aPixels=a.getArrayDouble();
		double[] outPixels=out.getArrayDouble();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=Math.exp(aPixels[i]);
		
		return out;
		}
	}