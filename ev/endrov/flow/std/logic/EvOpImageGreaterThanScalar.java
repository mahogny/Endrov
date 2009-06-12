package endrov.flow.std.logic;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * A>b
 * 
 * TODO what type to output? type parameter?
 */
public class EvOpImageGreaterThanScalar extends EvOpSlice1
	{
	private Number tb;
	
	public EvOpImageGreaterThanScalar(Number b)
		{
		this.tb = b;
		}


	public EvPixels exec1(EvPixels... p)
		{
		return greater(p[0],tb.intValue());
		}
	
	public static EvPixels greater(EvPixels a, int b)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_INT, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=bool2int(aPixels[i]>b);
		
		return out;
		}
	
	private static int bool2int(boolean b)
		{
		return b ? 1 : 0;
		}


	}