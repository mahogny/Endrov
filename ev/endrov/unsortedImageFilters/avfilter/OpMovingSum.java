package endrov.unsortedImageFilters.avfilter;

import endrov.flow.OpSlice1;
import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.CumSumArea;

/**
 * Moving sum. Sum is taken over an area of size (2pw+1)x(2ph+1). r=0 hence corresponds
 * to the identity operation.
 * 
 * Complexity O(w*h)
 */
public class OpMovingSum extends OpSlice1
	{
	Number pw, ph;
	
	public OpMovingSum(Number pw, Number ph)
		{
		this.pw = pw;
		this.ph = ph;
		}

	public EvPixels exec1(EvPixels... p)
		{
		return AveragingFilter.movingSumQuad(CumSumArea.cumsum(p[0]), pw.intValue(), ph.intValue());
		}
	
	}