package endrov.flow.std.math;

import endrov.flow.OpSlice;
import endrov.imageset.EvPixels;

/**
 * b - A
 * @author Johan Henriksson
 *
 */
public class ScalarSubImageOp extends OpSlice
	{
	private Number b;
	public ScalarSubImageOp(Number b)
		{
		this.b = b;
		}
	public EvPixels exec(EvPixels... p)
		{
		return OpImageSubScalar.minus(b, p[0]);
		}
	}