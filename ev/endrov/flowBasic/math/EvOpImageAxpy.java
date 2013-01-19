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
 * A*b+c
 * @author Johan Henriksson
 *
 */
public class EvOpImageAxpy extends EvOpSlice1
	{
	private final Number b;
	private final Number c;
	public EvOpImageAxpy(Number b, Number c)
		{
		this.b = b;
		this.c = c;
		}
	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return apply(p[0], b, c);
		}
	/**
	 * A*b+c
	 */
	static EvPixels apply(EvPixels a, Number b, Number c)
		{
		if(b instanceof Integer && c instanceof Integer)
			return apply(a,b.intValue(),c.intValue());
		else
			return apply(a,b.doubleValue(),c.doubleValue());
		}
	/**
	 * A*b+c
	 */
	static EvPixels apply(EvPixels a, double b, double c)
		{
		//Should use the common higher type here
		a=a.getReadOnly(EvPixelsType.DOUBLE);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		double[] aPixels=a.getArrayDouble();
		double[] outPixels=out.getArrayDouble();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]*b+c;
		
		return out;
		}
	/**
	 * A*b+c
	 */
	static EvPixels apply(EvPixels a, int b, int c)
		{
		//Should use the common higher type here
		a=a.getReadOnly(EvPixelsType.INT);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]*b+c;
		
		return out;
		}
	}