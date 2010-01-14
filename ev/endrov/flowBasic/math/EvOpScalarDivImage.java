/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.math;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * a / B
 * @author Johan Henriksson
 *
 */
public class EvOpScalarDivImage extends EvOpSlice1
	{
	private final Number a;
	public EvOpScalarDivImage(Number a)
		{
		this.a = a;
		}
	public EvPixels exec1(EvPixels... p)
		{
		return apply(a, p[0]);
		}
	
	
	static EvPixels apply(Number aVal, EvPixels b)
		{
		if(aVal instanceof Integer && b.getType()==EvPixelsType.INT)
			{
			// Should use the common higher type here
			b = b.getReadOnly(EvPixelsType.INT);
			int a=aVal.intValue();
			
			int w = b.getWidth();
			int h = b.getHeight();
			EvPixels out = new EvPixels(b.getType(), w, h);
			int[] bPixels = b.getArrayInt();
			int[] outPixels = out.getArrayInt();
	
			for (int i = 0; i<bPixels.length; i++)
				outPixels[i] = a/bPixels[i];
	
			return out;
			}
		else
			{
			// Should use the common higher type here
			b = b.getReadOnly(EvPixelsType.DOUBLE);
			double a=aVal.doubleValue();
			
			int w = b.getWidth();
			int h = b.getHeight();
			EvPixels out = new EvPixels(b.getType(), w, h);
			double[] bPixels = b.getArrayDouble();
			double[] outPixels = out.getArrayDouble();
	
			for (int i = 0; i<bPixels.length; i++)
				outPixels[i] = a/bPixels[i];
	
			return out;
			}
		}
	}