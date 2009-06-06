package endrov.unsortedImageFilters.imageLogic;

import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.newcore.SliceOp;

/**
 * a OR b
 * @author Johan Henriksson
 */
public class OrImageOp extends SliceOp
	{
	public EvPixels exec(EvPixels... p)
		{
		return or(p[0], p[1]);
		}
	
	private static EvPixels or(EvPixels a, EvPixels b)
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
			outPixels[i]=aPixels[i]!=0 || bPixels[i]!=0 ? 1 : 0;
		return out;
		}
	}