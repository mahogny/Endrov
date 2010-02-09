/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowProjection;

import endrov.flowBasic.math.EvOpImageMaxImage;
import endrov.imageset.EvPixels;

/**
 * Projection: Max Z
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpProjectMaxZ extends EvOpProjectZ
	{
	@Override
	protected EvPixels combine(EvPixels a, EvPixels b)
		{
		return new EvOpImageMaxImage().exec1(a,b);
		}
	}
