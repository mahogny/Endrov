/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowFourier;

import endrov.flow.EvOpStack1;
import endrov.imageset.EvImage;
import endrov.imageset.EvStack;
import endrov.util.ProgressHandle;

/**
 * Rotate image
 * 
 * Complexity O(w*h)
 */
public class EvOpWrapImage3D extends EvOpStack1
	{
	Number px, py, pz;
	
	public EvOpWrapImage3D(Number px, Number py, Number pz)
		{
		this.px = px;
		this.py = py;
		this.pz = pz;
		}

	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		return apply(ph, p[0],intValue(px), intValue(py), intValue(pz));
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
	public static EvStack apply(ProgressHandle ph, EvStack in, Integer px, Integer py, Integer pz)
		{		
		int d=in.getDepth();

		EvStack out=new EvStack();
		out.getMetaFrom(in);

		int thepz;
		if(pz==null)
			thepz=d/2;
		else
			thepz=pz;

		EvImage[] inIm=in.getImages();
		for(int az=0;az<inIm.length;az++)
			{
			int to=(az+thepz)%d;
			EvImage rot2d=new EvImage(EvOpWrapImage2D.apply(ph, inIm[az].getPixels(ph), px, py));
			out.putInt(to, rot2d);
			//az++;
			}
		return out;
		}
	}