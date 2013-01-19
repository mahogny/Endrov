/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowProjection;

import endrov.flow.EvOpStack1;
import endrov.typeImageset.EvImagePlane;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.typeImageset.EvStack;
import endrov.util.ProgressHandle;

/**
 * Projections along Z that can combine 2 images at a time
 * 
 * TODO with little work, could also project other axis (but slowly)
 * 
 * @author Johan Henriksson
 *
 */
public abstract class EvOpProjectZ extends EvOpStack1
	{
	
	
	
	@Override
	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		return project(ph, p[0]);
		}

	
	/**
	 * Function to combine 2 image planes
	 */
	protected abstract EvPixels combine(ProgressHandle ph, EvPixels a, EvPixels b);
	
	/**
	 * Project into a single-image stack
	 */
	public EvStack project(ProgressHandle ph, EvStack in)
		{
		EvStack out=new EvStack();
		out.copyMetaFrom(in);

		EvImagePlane proto=in.getFirstPlane();
		EvPixels ptot=new EvPixels(EvPixelsType.INT,proto.getPixels(ph).getWidth(),proto.getPixels(ph).getHeight());
		for(EvImagePlane plane:in.getImagePlanes())
			{
			//System.out.println("plane type "+plane.getValue().getPixels().getTypeString());
			ptot=combine(ph,ptot,plane.getPixels(ph));
			//System.out.println(">>>  "+plane.getValue().getPixels().asciiPart(120,50,80));
			//System.out.println("ptot "+ptot.asciiPart(100,20,40));
			}
		//Should not include 0-image
		
		
		EvImagePlane imout=new EvImagePlane();
		imout.setPixelsReference(ptot);
		//out.put(EvDecimal.ZERO,imout);
		
		int numZ=in.getDepth();
		for(int cz=0;cz<numZ;cz++)
			out.putPlane(cz,imout.makeShadowCopy());

//		for(Map.Entry<EvDecimal, EvImage> plane:in.entrySet())
//			out.put(plane.getKey(), imout); //incorrect TODO
//			out.put(plane.getKey(), imout.makeShadowCopy());
			
		return out;
		}
	}
