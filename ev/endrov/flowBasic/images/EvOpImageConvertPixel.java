/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.images;

import endrov.flow.EvOpSlice1;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.util.ProgressHandle;

/**
 * Convert pixel type to a given type
 * @author Johan Henriksson
 *
 */
public class EvOpImageConvertPixel extends EvOpSlice1
	{
	private final EvPixelsType type;
	public EvOpImageConvertPixel(EvPixelsType type)
		{
		this.type=type;
		}
	
	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return EvOpImageConvertPixel.apply(p[0],type);
		}

	public static EvPixels apply(EvPixels a, EvPixelsType type)
		{
		return a.getReadOnly(type);
		}
	}