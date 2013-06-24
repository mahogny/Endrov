/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowProjection;

import java.util.Iterator;

import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;
import endrov.flow.EvOpStack1;
import endrov.imglib.EvStackAdapter;
import endrov.typeImageset.EvImagePlane;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvStack;
import endrov.util.ProgressHandle;
import endrov.util.math.EvMathUtil;

/**
 * Pick the plane with the best focus
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpProjectBestFocusPlane extends EvOpStack1
	{
	
	@Override
	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		return project(ph, p[0]);
		}

	
	
	private <T extends RealType> double calcVarianceForPlane(EvPixels p)
		{
		//p.convertToDouble(true).getArrayDouble()
		
		EvStack st=new EvStack();
		st.putPlane(0, new EvImagePlane(p));
		
		ImgPlus<T> imp=(ImgPlus<T>)EvStackAdapter.wrapEvStack(st);
		Img<T> image=imp.getImg();

		double v=computeVariance(image);
		
		return v;
		}
	
	/**
	 * Project into a single-image stack
	 * 
	 * Possible improvement: keep track of where to get best pixels in a map. Then check this map
	 * at the end. Use pixels from the locally most common choice to avoid getting pixels almost randomly.
	 * This map can be given back separately to check the result.
	 * 
	 */
	public EvStack project(ProgressHandle progh, EvStack in)
		{
		int bestI=0;
		double bestVar=calcVarianceForPlane(in.getPlane(0).getPixels(progh));
		for(int i=1;i<in.getDepth();i++)
			{
			//int bestI=0;
			double v=calcVarianceForPlane(in.getPlane(i).getPixels(progh));
			if(v>bestVar)
				{
				bestVar=v;
				bestI=i;
				}
			}

		EvStack out=new EvStack();
		out.copyMetaFrom(in);
		out.putPlane(0, in.getPlane(bestI).makeShadowCopy());
		
		return out;
		}
	
	
	
	
	
	/**
	 * Compute the min and max for any {@link Iterable}, like an {@link Img}.
	 *
	 */
	public < T extends Comparable< T > & RealType< T > > double computeVariance(
		final Iterable< T > input)
	{
		// create a cursor for the image (the order does not matter)
		final Iterator< T > iterator = input.iterator();

		// initialize min and max with the first image value
		T type = iterator.next();

		
		double sum=0;
		double sum2=0;
		int n=0;
				
//		type.getRealDouble();
		
//		min.set( type );
	//	max.set( type );

		// loop over the rest of the data and determine min and max value
		while ( iterator.hasNext() )
		{
			// we need this type more than once
			type = iterator.next();

			double d=type.getRealDouble();
			sum+=d;
			sum2+=d*d;
			
		}
		
		return EvMathUtil.unbiasedVariance(sum, sum2, n);
		
	}

	

	
	}
