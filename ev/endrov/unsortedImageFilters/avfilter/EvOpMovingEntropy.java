package endrov.unsortedImageFilters.avfilter;

import endrov.flow.EvOpSlice1;
import endrov.flow.std.math.EvOpImageLog;
import endrov.flow.std.math.EvOpImageMulScalar;
import endrov.imageset.EvPixels;

/**
 * Moving entropy. Entropy is taken over an area of size (2pw+1)x(2ph+1).
 * 
 * Entropy is defined as S=-sum_i P[i] log(i), where i is intensity
 * 
 * Complexity O(w*h)
 */
public class EvOpMovingEntropy extends EvOpSlice1
	{
	Number pw, ph;
	
	public EvOpMovingEntropy(Number pw, Number ph)
		{
		this.pw = pw;
		this.ph = ph;
		}

	public EvPixels exec1(EvPixels... p)
		{
		return new EvOpImageMulScalar(-1.0).exec1(new EvOpMovingAverage(pw,ph).exec(new EvOpImageLog().exec(p[0])));
		}
	}