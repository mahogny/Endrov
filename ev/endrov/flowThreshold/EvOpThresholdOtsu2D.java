/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowThreshold;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.flowBasic.EvImageHistogram;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;


/**
 * Otsu thresholding
 * <br/>
 * http://en.wikipedia.org/wiki/Otsu's_method
 * <br/>
 * Complexity O(w*h+numColorUsed*log(numColorUsed))
 */
public class EvOpThresholdOtsu2D extends Threshold2D
	{
	public EvOpThresholdOtsu2D(int mode)
		{
		super(mode);
		}

	protected double[] getThreshold(EvPixels in)
		{
		return new double[]{EvOpThresholdOtsu2D.findOtsuThreshold(in)};
		}
	
	public static double findOtsuThreshold(EvPixels in)
		{
		in=in.getReadOnly(EvPixelsType.INT);
		
		int numPixels=in.getWidth()*in.getHeight();
		
		SortedMap<Integer,Integer> hist=new TreeMap<Integer,Integer>(EvImageHistogram.intHistogram(in));

		//The goal is to maximize sigma_b

		//Starting value, from lowest index
		int n_b=0;
		int n_o=numPixels;
		double mu_b=0;
		double mu_o=0;
		for(Map.Entry<Integer, Integer> entry:hist.entrySet())
			mu_o+=entry.getKey()*entry.getValue();
		mu_o/=numPixels;

		Iterator<Map.Entry<Integer, Integer>> it=hist.entrySet().iterator();
		Map.Entry<Integer, Integer> curEntry=it.next();
		
		double maxSigma_b=0; //There are no pixels less, multiplication by 0.
		int maxSigmaThres=curEntry.getKey();
		
		int T=curEntry.getKey();
		int n_T=curEntry.getValue();

		//Go through all other values. O(n) by reusing last value
		while(it.hasNext())
			{
			//I think algorithm can be made numerically more stable by using only cumsums. here the paper is followed in detail.
			
			//Jump to next threshold
			int n_b1=n_b+n_T;
			int n_o1=n_o-n_T;
			mu_b=(mu_b*n_b+n_T*T)/n_b1;
			mu_o=(mu_o*n_o-n_T*T)/n_o1;
			n_b=n_b1;
			n_o=n_o1;
			
			//Update iterator
			curEntry=it.next();
			T=curEntry.getKey();
			n_T=curEntry.getValue();

			//Calculate new sigma_b and compare
			double diff=mu_b-mu_o;
			double sigma_b=n_b*n_o*diff*diff;
			if(sigma_b>=maxSigma_b)
				{
				maxSigma_b=sigma_b;
				maxSigmaThres=T;
				}
			
			//System.out.println("nb"+n_b+"   no"+n_o+"  nT"+n_T);
			//System.out.println("sigmab "+sigma_b+"     thr "+T);
			}
		System.out.println("threshold: "+maxSigmaThres);
		
		return maxSigmaThres;
		}
	}
