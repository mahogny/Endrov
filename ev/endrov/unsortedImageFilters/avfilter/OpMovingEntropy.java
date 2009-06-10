package endrov.unsortedImageFilters.avfilter;

import endrov.flow.OpSlice1;
import endrov.flow.std.math.OpImageLog;
import endrov.flow.std.math.OpImageMulScalar;
import endrov.imageset.EvPixels;

/**
 * Moving entropy. Entropy is taken over an area of size (2pw+1)x(2ph+1).
 * 
 * Entropy is defined as S=-sum_i P[i] log(i), where i is intensity
 * 
 * Complexity O(w*h)
 */
public class OpMovingEntropy extends OpSlice1
	{
	Number pw, ph;
	
	public OpMovingEntropy(Number pw, Number ph)
		{
		this.pw = pw;
		this.ph = ph;
		}

	public EvPixels exec1(EvPixels... p)
		{
		return new OpImageMulScalar(-1.0).exec1(new OpMovingAverage(pw,ph).exec(new OpImageLog().exec(p[0])));
		}
	}