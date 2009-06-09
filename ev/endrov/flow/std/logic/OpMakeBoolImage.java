package endrov.flow.std.logic;

import endrov.flow.OpSlice;
import endrov.imageset.EvPixels;

/**
 * Turn A into boolean image ie non-zero pixels are set to 1
 * @author Johan Henriksson
 */
public class OpMakeBoolImage extends OpSlice
	{
	public EvPixels exec(EvPixels... p)
		{
		return not(p[0]);
		}
	
	private static EvPixels not(EvPixels a)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_INT, true);
		//Know output range
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]!=0 ? 1 : 0;
		return out;
		}
	}