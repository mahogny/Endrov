/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.math;

import endrov.flow.EvOpSlice1;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.util.ProgressHandle;

/**
 * A>b
 * 
 * TODO what type to output? type parameter?
 */
public class EvOpImageGreaterThanScalar extends EvOpSlice1
	{
	private Number tb;
	
	public EvOpImageGreaterThanScalar(Number b)
		{
		this.tb = b;
		}


	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return greater(p[0],tb);
		}
	
	public static EvPixels greater(EvPixels a, Number bb)
		{
		if(a.getType()==EvPixelsType.INT)
			{
			int b=bb.intValue();
			
			//Should use the common higher type here
			a=a.getReadOnly(EvPixelsType.INT);
			
			int w=a.getWidth();
			int h=a.getHeight();
			EvPixels out=new EvPixels(EvPixelsType.INT,w,h);
			int[] aPixels=a.getArrayInt();
			int[] outPixels=out.getArrayInt();
			
			for(int i=0;i<aPixels.length;i++)
				outPixels[i]=bool2int(aPixels[i]>b);
			return out;
			}
		else
			{
			double b=bb.doubleValue();
			//Should use the common higher type here
			a=a.getReadOnly(EvPixelsType.DOUBLE);
			
			int w=a.getWidth();
			int h=a.getHeight();
			EvPixels out=new EvPixels(EvPixelsType.INT,w,h);
			double[] aPixels=a.getArrayDouble();
			int[] outPixels=out.getArrayInt();
			
			for(int i=0;i<aPixels.length;i++)
				outPixels[i]=bool2int(aPixels[i]>b);
			return out;
			}
			
		}
	
	private static int bool2int(boolean b)
		{
		return b ? 1 : 0;
		}


	}