package endrov.unsortedImageFilters.imageMath;

import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.newcore.SliceOp;

/**
 * b - A
 * @author Johan Henriksson
 *
 */
public class ScalarSubImageOp extends SliceOp
	{
	private Number b;
	public ScalarSubImageOp(Number b)
		{
		this.b = b;
		}
	public EvPixels exec(EvPixels... p)
		{
		return ImageSubScalarOp.minus(b, p[0]);
		}
	}