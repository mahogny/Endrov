/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowProjection;

import endrov.flow.EvOpStack1;
import endrov.flowBasic.math.EvOpImageAddImage;
import endrov.flowBasic.math.EvOpImageDivScalar;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.util.ProgressHandle;

/**
 * Projection: Average Z
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpAverageZ extends EvOpStack1
	{
	

	@Override
	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		return averageZ(ph, p[0]);
		}

	
	
	
	public static EvStack averageZ(ProgressHandle ph, EvStack in)
		{
		EvImage proto=in.getFirstImage();
		
		EvStack out=new EvStack();


		EvPixels ptot=new EvPixels(EvPixelsType.INT,proto.getPixels(ph).getWidth(),proto.getPixels(ph).getHeight());
		int numZ=in.getDepth();
		for(EvImage plane:in.getImages())
			ptot=new EvOpImageAddImage().exec1(ph,ptot,plane.getPixels(ph));
			//ImageMath.plus(ptot, plane.getValue().getPixels());

		ptot=new EvOpImageDivScalar(numZ).exec1(ph, ptot);
		//ptot=ImageMath.div(ptot,numZ);
		
		EvImage imout=new EvImage();
		out.getMetaFrom(in);
		imout.setPixelsReference(ptot);
		
		//Lazy stack op will use all planes!
		
		for(int cz=0;cz<numZ;cz++)
			out.putInt(cz,imout.makeShadowCopy());
		//for(Map.Entry<EvDecimal, EvImage> plane:in.entrySet())
//			out.put(plane.getKey(), imout.makeShadowCopy());
			
		return out;
		}

	}
