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
import endrov.typeImageset.EvPixels;
import endrov.util.ProgressHandle;

/**
 * Maximum entropy thresholding
 * TODO Give reference!
 * TODO test
 * 
 * Complexity O(w*h+numColorUsed*log(numColorUsed))
 */
public class EvOpThresholdMaxEntropy2D extends Threshold2D
	{
	public EvOpThresholdMaxEntropy2D(int mode)
		{
		super(mode);
		}
	
	public double[] getThreshold(ProgressHandle progh, EvPixels in)
		{
		return new double[]{findThresholdMaxEntropy(progh, in)};
		}
	public static double findThresholdMaxEntropy(ProgressHandle progh, EvPixels in)
		{
		int numPixels=in.getWidth()*in.getHeight();
		
		SortedMap<Integer,Integer> hist=new TreeMap<Integer,Integer>(EvImageHistogram.intHistogram(in));
		SortedMap<Integer,Integer> cumHist=EvImageHistogram.makeHistCumulative(hist);
		
		
		/**
		 * S = -sum p*log(p)
		 * Goal: Maximize S_bg + S_sig
		 * 
		 * p=numPix/totPix =>
		 * S = - (1/totPix) sum numPix*log(numPix/totPix)
		 * S = - (1/totPix) sum numPix*log(numPix) + (1/totPix) sum numPix*log(totPix)
		 * S = - (1/totPix) sum numPix*log(numPix) + log(totPix)
		 * 
		 * this can be done as a cumulative sum: sum -numPix*log(numPix)
		 * totPix refers to the size of the two S and will be done for each value in the histogram
		 */

		//Create the cumsum for part of S
		SortedMap<Integer,Double> cumS=new TreeMap<Integer,Double>();
		double accum=0;
		for(Map.Entry<Integer, Integer> e:hist.entrySet())
			{
			int value=e.getValue();
			accum+=-value*Math.log(value);
			cumS.put(e.getKey(),accum);
			}
		
		//The total sum of S
		double totalS=cumS.get(cumS.lastKey());

		//Go through, find
		Iterator<Map.Entry<Integer, Integer>> it=hist.entrySet().iterator();
		Iterator<Integer> itCount=cumHist.values().iterator();
		double maxVal=-Double.MAX_VALUE;
		double maxThres=0;
		while(it.hasNext())
			{
			Map.Entry<Integer, Integer> curEntry=it.next();
			int curNum=itCount.next();
			
			double curThres=curEntry.getKey();
			double curCumS=curEntry.getValue();
			
			double a=curCumS/curNum + Math.log(curNum);
			int numPixB=numPixels-curNum;
			
			if(numPixB==0)
				continue;
			
			double b=(totalS-curCumS)/numPixB + Math.log(numPixB); 
			
			double curVal=a+b;
			if(curVal>maxVal)
				{
				maxVal=curVal;
				maxThres=curThres;
				}
			System.out.println(curVal);
			}
		System.out.println("threshold: "+maxThres);
		
		return maxThres;
		}
	}
