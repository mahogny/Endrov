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
 * abs(nabla_xy)^2
 * @author Johan Henriksson
 *
 */
public class EvOpImageAbsGradXY2 extends EvOpSlice1
	{
	public EvPixels exec1(EvPixels... p)
		{
		return EvOpImageAbsGradXY2.apply(p[0]);
		}

	static EvPixels apply(EvPixels a)
		{
		//Should use the common higher type here
		a=a.getReadOnly(EvPixelsType.DOUBLE);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		double[] aPixels=a.getArrayDouble();
		double[] outPixels=out.getArrayDouble();
		
		for(int y=0;y<h-1;y++)
			for(int x=0;x<w-1;x++)
				{
				int index=y*w+x;
				int indexRight=index+1;
				int indexBelow=index+w;
				double mid=aPixels[index];
				double gradX=aPixels[indexRight]-mid;
				double gradY=aPixels[indexBelow]-mid;
				outPixels[index]=gradX*gradX+gradY*gradY;
				}
		
		return out;
		}
	}