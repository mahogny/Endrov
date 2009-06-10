package endrov.flow.std.logic;

import endrov.flow.OpSlice1;
import endrov.imageset.EvPixels;

/**
 * a XOR b
 * @author Johan Henriksson
 */
public class OpXorImage extends OpSlice1
	{
	public EvPixels exec1(EvPixels... p)
		{
		return xor(p[0], p[1]);
		}
	
	private static EvPixels xor(EvPixels a, EvPixels b)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_INT, true);
		b=b.convertTo(EvPixels.TYPE_INT, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] bPixels=b.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=(aPixels[i]!=0) ^ (bPixels[i]!=0) ? 1 : 0;
		return out;
		}
	}