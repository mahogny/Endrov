package endrov.unsortedImageFilters;

import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.newcore.SliceOp;

/**
 * Logic operations on images
 * @author Johan Henriksson
 *
 */
public class ImageLogic
	{
	private static EvPixels and(EvPixels a, EvPixels b)
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
			outPixels[i]=aPixels[i]>0 && bPixels[i]>0 ? 1 : 0;
		return out;
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
			outPixels[i]=aPixels[i]>0 || bPixels[i]>0 ? 1 : 0;
		return out;
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
			outPixels[i]=(aPixels[i]>0) ^ (bPixels[i]>0) ? 1 : 0;
		return out;
		}
	
	private static EvPixels not(EvPixels a)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_INT, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]>0 ? 0 : 1;
		return out;
		}
	
	
	
	
	/**
	 * a AND b
	 * @author Johan Henriksson
	 */
	public static class AndImageOp extends SliceOp
		{
		public EvPixels exec(EvPixels... p)
			{
			return and(p[0], p[1]);
			}
		}
	
	
	/**
	 * a OR b
	 * @author Johan Henriksson
	 */
	public static class OrImageOp extends SliceOp
		{
		public EvPixels exec(EvPixels... p)
			{
			return or(p[0], p[1]);
			}
		}	
	
	/**
	 * a XOR b
	 * @author Johan Henriksson
	 */
	public static class XorImageOp extends SliceOp
		{
		public EvPixels exec(EvPixels... p)
			{
			return xor(p[0], p[1]);
			}
		}	
	
	
	/**
	 * NOT a
	 * @author Johan Henriksson
	 */
	public static class NotImageOp extends SliceOp
		{
		public EvPixels exec(EvPixels... p)
			{
			return not(p[0]);
			}
		}
	
	
	
	
	}
