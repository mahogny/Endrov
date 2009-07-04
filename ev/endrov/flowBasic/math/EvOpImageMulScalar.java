package endrov.flowBasic.math;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * A * b
 * @author Johan Henriksson
 *
 */
public class EvOpImageMulScalar extends EvOpSlice1
	{
	private Number b;
	public EvOpImageMulScalar(Number b)
		{
		this.b = b;
		}
	public EvPixels exec1(EvPixels... p)
		{
		if(b instanceof Integer)
			return EvOpImageMulScalar.times(p[0], b.intValue()); //TODO
		else
			return EvOpImageMulScalar.times(p[0], b.doubleValue()); //TODO
		}
	static EvPixels times(EvPixels a, double b)
	{
	//Should use the common higher type here
	a=a.getReadOnly(EvPixelsType.DOUBLE);
	
	int w=a.getWidth();
	int h=a.getHeight();
	EvPixels out=new EvPixels(a.getType(),w,h);
	double[] aPixels=a.getArrayDouble();
	double[] outPixels=out.getArrayDouble();
	
	for(int i=0;i<aPixels.length;i++)
		outPixels[i]=aPixels[i]*b;
	System.out.println("outp "+outPixels[0]+"    b "+b);
	return out;
	}
	static EvPixels times(EvPixels a, int b)
	{
	//Should use the common higher type here
	a=a.getReadOnly(EvPixelsType.INT);
	
	int w=a.getWidth();
	int h=a.getHeight();
	EvPixels out=new EvPixels(a.getType(),w,h);
	int[] aPixels=a.getArrayInt();
	int[] outPixels=out.getArrayInt();
	
	for(int i=0;i<aPixels.length;i++)
		outPixels[i]=aPixels[i]*b;
	
	return out;
	}
	}