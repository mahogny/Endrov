package endrov.unsortedImageFilters.imageMath;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * max(A,B)
 */
public class EvOpMaxImageImage extends EvOpSlice1
	{
	public EvPixels exec1(EvPixels... p)
		{
		return greater(p[0],p[1]);
		}
	
	public static EvPixels greater(EvPixels a, EvPixels b)
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
			outPixels[i]=aPixels[i]>bPixels[i] ? aPixels[i] : bPixels[i];
		
		return out;
		}
	
	}