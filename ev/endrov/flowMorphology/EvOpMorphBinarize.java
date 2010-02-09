/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMorphology;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * Turn all non-zero intensities to 1, 0 to 0
 * @author Johan Henriksson
 */
public class EvOpMorphBinarize extends EvOpSlice1
	{
	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return complement(p[0]);
		}


	public static EvPixels complement(EvPixels in)
		{
		in=in.getReadOnly(EvPixelsType.DOUBLE);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(EvPixelsType.INT,w,h);
		double[] inPixels=in.getArrayDouble();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<inPixels.length;i++)
			outPixels[i]=inPixels[i]!=0 ? 1 : 0;
			
		return out;
		}


	}