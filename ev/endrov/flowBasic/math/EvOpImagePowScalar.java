/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.math;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.ProgressHandle;

/**
 * A^b
 * @author Johan Henriksson
 *
 */
public class EvOpImagePowScalar extends EvOpSlice1
	{
	private final Number b;
	
	public EvOpImagePowScalar(Number b)
		{
		this.b = b;
		}

	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return EvOpImagePowScalar.apply(ph, p[0], b);
		}

	public static EvPixels apply(ProgressHandle progh, EvPixels a, Number bVal)
		{
		if(bVal.doubleValue()==2)
			{
			//Should use the common higher type here
			a=a.getReadOnly(EvPixelsType.DOUBLE);
			
			int w=a.getWidth();
			int h=a.getHeight();
			EvPixels out=new EvPixels(a.getType(),w,h);
			double[] aPixels=a.getArrayDouble();
			double[] outPixels=out.getArrayDouble();
			
			for(int i=0;i<aPixels.length;i++)
				{
				double val=aPixels[i];
				outPixels[i]=val*val;
				}
			
			return out;			
			}
		else
			{
			//Should use the common higher type here
			a=a.getReadOnly(EvPixelsType.DOUBLE);
			double b=bVal.doubleValue();
			
			int w=a.getWidth();
			int h=a.getHeight();
			EvPixels out=new EvPixels(a.getType(),w,h);
			double[] aPixels=a.getArrayDouble();
			double[] outPixels=out.getArrayDouble();
			
			for(int i=0;i<aPixels.length;i++)
				outPixels[i]=Math.pow(aPixels[i],b);
			
			return out;
			}
		}
	}