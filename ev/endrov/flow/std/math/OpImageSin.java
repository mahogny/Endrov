package endrov.flow.std.math;

import endrov.flow.OpSlice;
import endrov.imageset.EvPixels;

/**
 * sin(A)
 * @author Johan Henriksson
 *
 */
public class OpImageSin extends OpSlice
	{
	public EvPixels exec(EvPixels... p)
		{
		return OpImageSin.log(p[0]);
		}

	/**
	 * log(A)
	 */
	static EvPixels log(EvPixels a)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_DOUBLE, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		double[] aPixels=a.getArrayDouble();
		double[] outPixels=out.getArrayDouble();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=Math.sin(aPixels[i]);
		
		return out;
		}
	}