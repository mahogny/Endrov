package endrov.flow.std.math;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * A * B
 * @author Johan Henriksson
 *
 */
public class EvOpImageMulImage extends EvOpSlice1
	{
	public EvPixels exec1(EvPixels... p)
		{
		return EvOpImageMulImage.times(p[0], p[1]);
		}

	/**
	 * Add images. Assumes same size and position
	 */
	static EvPixels times(EvPixels a, EvPixels b)
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
			outPixels[i]=aPixels[i]*bPixels[i];
		
		return out;
		}
	}