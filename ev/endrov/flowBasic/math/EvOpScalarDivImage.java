package endrov.flowBasic.math;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * a / B
 * @author Johan Henriksson
 *
 */
public class EvOpScalarDivImage extends EvOpSlice1
	{
	private Number a;
	public EvOpScalarDivImage(Number a)
		{
		this.a = a;
		}
	public EvPixels exec1(EvPixels... p)
		{
		return div(a.intValue(), p[0]);
		}
	
	
	static EvPixels div(int a, EvPixels b)
		{
		// Should use the common higher type here
		b = b.convertTo(EvPixels.TYPE_INT, true);

		int w = b.getWidth();
		int h = b.getHeight();
		EvPixels out = new EvPixels(b.getType(), w, h);
		int[] bPixels = b.getArrayInt();
		int[] outPixels = out.getArrayInt();

		for (int i = 0; i<bPixels.length; i++)
			outPixels[i] = a/bPixels[i];

		return out;
		}
	}