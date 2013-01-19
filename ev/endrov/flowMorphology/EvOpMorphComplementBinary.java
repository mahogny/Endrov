/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMorphology;

import endrov.flow.EvOpSlice1;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.util.ProgressHandle;

/**
 * Image^c
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpMorphComplementBinary extends EvOpSlice1
	{
	@Override
	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return apply(p[0]);
		}


	public static EvPixels apply(EvPixels in)
		{
		in=in.getReadOnly(EvPixelsType.INT);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] inPixels=in.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<inPixels.length;i++)
			outPixels[i]=inPixels[i]!=0 ? 0 : 1;
			
		return out;
		}


	}
