package endrov.flowBasic.math;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * A - b
 * @author Johan Henriksson
 *
 */
public class EvOpImageSubScalar extends EvOpSlice1
	{
	private Number b;
	public EvOpImageSubScalar(Number b)
		{
		this.b = b;
		}
	public EvPixels exec1(EvPixels... p)
		{
		return EvOpImageSubScalar.minus(p[0], b);
		}
	public static EvPixels minus(EvPixels a, Number bval)
		{
		if(a.getType()==EvPixels.TYPE_INT && bval instanceof Integer)
			{
			//Should use the common higher type here
			a=a.convertTo(EvPixels.TYPE_INT, true);
			
			int b=bval.intValue();
			
			int w=a.getWidth();
			int h=a.getHeight();
			EvPixels out=new EvPixels(a.getType(),w,h);
			int[] aPixels=a.getArrayInt();
			int[] outPixels=out.getArrayInt();
			
			for(int i=0;i<aPixels.length;i++)
				outPixels[i]=aPixels[i]-b;
			
			return out;
			}
		else
			{
			//Should use the common higher type here
			a=a.convertTo(EvPixels.TYPE_DOUBLE, true);
			
			double b=bval.doubleValue();
			
			int w=a.getWidth();
			int h=a.getHeight();
			EvPixels out=new EvPixels(a.getType(),w,h);
			double[] aPixels=a.getArrayDouble();
			double[] outPixels=out.getArrayDouble();
			
			for(int i=0;i<aPixels.length;i++)
				outPixels[i]=aPixels[i]-b;
			
			return out;
			}
		}
	
	public static EvPixels minus(Number aVal, EvPixels b)
		{
		if(b.getType()==EvPixels.TYPE_INT && aVal instanceof Integer)
			{
			//Should use the common higher type here
			b=b.convertTo(EvPixels.TYPE_INT, true);
			
			int a=aVal.intValue();
			
			int w=b.getWidth();
			int h=b.getHeight();
			EvPixels out=new EvPixels(b.getType(),w,h);
			int[] bPixels=b.getArrayInt();
			int[] outPixels=out.getArrayInt();
			
			for(int i=0;i<bPixels.length;i++)
				outPixels[i]=a-bPixels[i];
			
			return out;
			}
		else
			{
			//Should use the common higher type here
			b=b.convertTo(EvPixels.TYPE_DOUBLE, true);
			
			double a=aVal.doubleValue();
			
			int w=b.getWidth();
			int h=b.getHeight();
			EvPixels out=new EvPixels(b.getType(),w,h);
			double[] bPixels=b.getArrayDouble();
			double[] outPixels=out.getArrayDouble();
			
			for(int i=0;i<bPixels.length;i++)
				outPixels[i]=a-bPixels[i];
			
			return out;
			}
		}
	}