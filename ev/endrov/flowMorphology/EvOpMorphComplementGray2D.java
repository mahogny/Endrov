/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMorphology;

import endrov.flow.EvOpSlice1;
import endrov.flowBasic.EvImageUtil;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * Image^c, according to gray morphology i.e. maxValue-intensity[i]
 * <br/>
 * O(w*h)
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpMorphComplementGray2D extends EvOpSlice1
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
		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,w,h);
		double[] inPixels=in.getArrayDouble();
		double[] outPixels=out.getArrayDouble();
		
		double maxval=EvImageUtil.maxValue(in);

		for(int i=0;i<inPixels.length;i++)
			outPixels[i]=maxval-inPixels[i];
			
		return out;
		}


	}
