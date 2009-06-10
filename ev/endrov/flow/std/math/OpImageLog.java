package endrov.flow.std.math;

import endrov.flow.OpSlice1;
import endrov.imageset.EvPixels;

/**
 * log(A)
 * @author Johan Henriksson
 *
 */
public class OpImageLog extends OpSlice1
	{
	public EvPixels exec1(EvPixels... p)
		{
		return OpImageLog.log(p[0]);
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
			outPixels[i]=Math.log(aPixels[i]);
		
		return out;
		}
	}