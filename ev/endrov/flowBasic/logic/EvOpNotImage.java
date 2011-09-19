/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.logic;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.ProgressHandle;

/**
 * NOT a
 * @author Johan Henriksson
 */
public class EvOpNotImage extends EvOpSlice1
	{
	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return not(p[0]);
		}
	
	private static EvPixels not(EvPixels a)
		{
		//Should use the common higher type here
		a=a.getReadOnly(EvPixelsType.INT);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]!=0 ? 0 : 1;
		return out;
		}
	}