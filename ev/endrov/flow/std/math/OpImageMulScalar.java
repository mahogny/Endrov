package endrov.flow.std.math;

import endrov.flow.OpSlice;
import endrov.imageset.EvPixels;

/**
 * A * b
 * @author Johan Henriksson
 *
 */
public class OpImageMulScalar extends OpSlice
	{
	private Number b;
	public OpImageMulScalar(Number b)
		{
		this.b = b;
		}
	public EvPixels exec(EvPixels... p)
		{
		if(b instanceof Integer)
			return OpImageMulScalar.times(p[0], b.intValue()); //TODO
		else
			return OpImageMulScalar.times(p[0], b.doubleValue()); //TODO
		}
	static EvPixels times(EvPixels a, double b)
	{
	//Should use the common higher type here
	a=a.convertTo(EvPixels.TYPE_DOUBLE, true);
	
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
	a=a.convertTo(EvPixels.TYPE_INT, true);
	
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