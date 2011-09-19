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
 * a OR b
 * @author Johan Henriksson
 */
public class EvOpOrImage extends EvOpSlice1
	{
	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return or(p[0], p[1]);
		}
	
	private static EvPixels or(EvPixels a, EvPixels b)
		{
		//Should use the common higher type here
		a=a.getReadOnly(EvPixelsType.INT);
		b=b.getReadOnly(EvPixelsType.INT);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] bPixels=b.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]!=0 || bPixels[i]!=0 ? 1 : 0;
		return out;
		}
	}