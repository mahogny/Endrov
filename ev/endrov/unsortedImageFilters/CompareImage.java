package endrov.unsortedImageFilters;

import endrov.imageset.EvPixels;


/**
 * Compare images or values. Can be used for thresholding.
 * 
 * Problem: Not obvious what to output. might need additional type parameter.
 * 
 * @author Johan Henriksson
 *
 */
public class CompareImage
	{
	
	
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
			outPixels[i]=bool2int(aPixels[i]>bPixels[i]);
		
		return out;
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
