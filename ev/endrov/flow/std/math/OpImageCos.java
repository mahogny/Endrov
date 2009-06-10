package endrov.flow.std.math;

import endrov.flow.OpSlice1;
import endrov.imageset.EvPixels;

/**
 * cos(A)
 * @author Johan Henriksson
 *
 */
public class OpImageCos extends OpSlice1
	{
	public EvPixels exec1(EvPixels... p)
		{
		return OpImageCos.log(p[0]);
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
			outPixels[i]=Math.cos(aPixels[i]);
		
		return out;
		}
	}