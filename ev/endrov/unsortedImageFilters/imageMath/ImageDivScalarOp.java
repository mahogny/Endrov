package endrov.unsortedImageFilters.imageMath;

import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.newcore.SliceOp;

/**
 * A / b
 * 
 * @author Johan Henriksson
 */
public class ImageDivScalarOp extends SliceOp
	{
	private Number b;

	public ImageDivScalarOp(Number b)
		{
		this.b = b;
		}

	public EvPixels exec(EvPixels... p)
		{
		// if(b instanceof Integer)
		return ImageDivScalarOp.div(p[0], b.intValue());
		// else
		// return div(p[0], b.doubleValue());
		}

	static EvPixels div(EvPixels a, int b)
		{
		// Should use the common higher type here
		a = a.convertTo(EvPixels.TYPE_INT, true);

		int w = a.getWidth();
		int h = a.getHeight();
		EvPixels out = new EvPixels(a.getType(), w, h);
		int[] aPixels = a.getArrayInt();
		int[] outPixels = out.getArrayInt();

		for (int i = 0; i<aPixels.length; i++)
			outPixels[i] = aPixels[i]/b;

		return out;
		}
	}
