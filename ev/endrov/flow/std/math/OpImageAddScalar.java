package endrov.flow.std.math;

import endrov.flow.OpSlice1;
import endrov.imageset.EvPixels;

/**
 * A + b
 * @author Johan Henriksson
 *
 */
public class OpImageAddScalar extends OpSlice1
	{
	private Number b;
	public OpImageAddScalar(Number b)
		{
		this.b = b;
		}
	public EvPixels exec1(EvPixels... p)
		{
		return OpImageAddScalar.plus(p[0], b);
		}
	/**
	 * TODO other types
	 */
	static EvPixels plus(EvPixels a, Number b)
		{
		return plus(a,b.intValue());
		}
	static EvPixels plus(EvPixels a, int b)
	{
	//Should use the common higher type here
	a=a.convertTo(EvPixels.TYPE_INT, true);
	
	int w=a.getWidth();
	int h=a.getHeight();
	EvPixels out=new EvPixels(a.getType(),w,h);
	int[] aPixels=a.getArrayInt();
	int[] outPixels=out.getArrayInt();
	
	for(int i=0;i<aPixels.length;i++)
		outPixels[i]=aPixels[i]+b;
	
	return out;
	}
	}