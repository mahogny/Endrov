/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowThreshold;



/**
 * Otsu thresholding
 * <br/>
 * http://en.wikipedia.org/wiki/Otsu's_method
 * <br/>
 * Complexity O(w*h+numColorUsed*log(numColorUsed))
 */
public class EvOpThresholdOtsu2D extends EvOpThresholdFukunaga2D
	{
	public EvOpThresholdOtsu2D()
		{
		super(2);
		}

	}
