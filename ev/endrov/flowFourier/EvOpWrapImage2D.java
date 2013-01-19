/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowFourier;

import endrov.flow.EvOpSlice1;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.util.ProgressHandle;

/**
 * Rotate image
 * 
 * Complexity O(w*h)
 */
public class EvOpWrapImage2D extends EvOpSlice1
	{
	Number rotx, roty;
	
	public EvOpWrapImage2D(Number rotx, Number roty)
		{
		this.rotx = rotx;
		this.roty = roty;
		}

	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return apply(ph, p[0],intValue(rotx), intValue(roty));
		}
	
	private static Integer intValue(Number n)
		{
		if(n!=null)
			return n.intValue();
		else
			return null;
		}
	
	/**
	 * Rotate image. If rotation is null, then rotate half-way
	 */
	public static EvPixels apply(ProgressHandle ph, EvPixels in, Integer px2, Integer py2)
		{
		int w=in.getWidth();
		int h=in.getHeight();
		
		int px;
		if(px2==null)
			px=w/2;
		else
			px=px2;

		int py;
		if(py2==null)
			py=h/2;
		else
			py=py2;
		
		in=in.getReadOnly(EvPixelsType.DOUBLE);
		double[] inPixels=in.getArrayDouble();
		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,w,h);
		double[] outPixels=out.getArrayDouble();
		
		for(int ay=0;ay<h;ay++)
			{
			int fromlinei=w*ay;
			int tolinei=w*((py+ay)%h);
			for(int ax=0;ax<w;ax++)
				{
				int fromi=fromlinei+ax;
				int toi=tolinei+(px+ax)%w;
				outPixels[toi]=inPixels[fromi];
				}
			}
		return out;
		}
	}