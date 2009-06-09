package endrov.flow.std.math;

import endrov.flow.OpSlice;
import endrov.imageset.EvPixels;

/**
 * A - B
 * @author Johan Henriksson
 *
 */
public class OpImageSubImage extends OpSlice
	{
	public EvPixels exec(EvPixels... p)
		{
		return OpImageSubImage.minus(p[0], p[1]);
		}

	static EvPixels minus(EvPixels a, EvPixels b)
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
		outPixels[i]=aPixels[i]-bPixels[i];
	
	return out;
	}
	}