package endrov.flow.std.math;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * b - A
 * @author Johan Henriksson
 *
 */
public class EvOpScalarSubImage extends EvOpSlice1
	{
	private Number b;
	public EvOpScalarSubImage(Number b)
		{
		this.b = b;
		}
	public EvPixels exec1(EvPixels... p)
		{
		return EvOpImageSubScalar.minus(b, p[0]);
		}
	}