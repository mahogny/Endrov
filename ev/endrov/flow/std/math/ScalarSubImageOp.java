package endrov.flow.std.math;

import endrov.flow.OpSlice1;
import endrov.imageset.EvPixels;

/**
 * b - A
 * @author Johan Henriksson
 *
 */
public class ScalarSubImageOp extends OpSlice1
	{
	private Number b;
	public ScalarSubImageOp(Number b)
		{
		this.b = b;
		}
	public EvPixels exec1(EvPixels... p)
		{
		return OpImageSubScalar.minus(b, p[0]);
		}
	}