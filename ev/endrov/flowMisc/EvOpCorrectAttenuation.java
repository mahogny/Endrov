/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMisc;

import java.util.LinkedList;
import java.util.List;
import endrov.flow.EvOpStack1;
import endrov.flowBasic.EvImageUtil;
import endrov.flowBasic.math.EvOpImageAddScalar;
import endrov.typeImageset.EvImagePlane;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.typeImageset.EvStack;
import endrov.util.ProgressHandle;
import endrov.util.collection.Tuple;
import endrov.util.math.EvMathUtil;

/**
 * Correct for light attenuation
 * 
 * Complexity O(w*h*d)
 */
public class EvOpCorrectAttenuation extends EvOpStack1
	{
	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		return apply(ph, p[0]);
		}
	
	public static EvStack apply(ProgressHandle ph, EvStack in)
		{		
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels[] p=in.getPixels(ph);
		double[] avg=new double[p.length];
		List<Double> zList=new LinkedList<Double>();
		List<Double> intensityList=new LinkedList<Double>();
		for(int i=0;i<p.length;i++)
			{
			p[i]=p[i].getReadOnly(EvPixelsType.DOUBLE);
			avg[i]=EvImageUtil.sum(p[i])/(w*h);
			zList.add((double)i);
			intensityList.add(avg[i]);
			}
		Tuple<Double,Double> km=EvMathUtil.fitLinear1D(intensityList, zList);
		System.out.println("Fit: "+km);

		
		EvStack out=new EvStack();
		out.copyMetaFrom(in);

		for(int i=0;i<p.length;i++)
			{
			double correct=km.fst()*i;
			out.putPlane(i, new EvImagePlane(EvOpImageAddScalar.plus(p[i], -correct)));
			}
				
		return out;
		}
	
	
	}