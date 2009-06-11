package endrov.unsortedImageFilters.threshold;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.Histogram;

/**
 * Maximum entropy thresholding
 * TODO Give reference!
 * TODO test
 * 
 * Complexity O(w*h+numColorUsed*log(numColorUsed))
 */
public class EvOpMaxEntropyThreshold2D extends EvOpThreshold2D
	{
	public double getThreshold(EvPixels in)
		{
		return findThresholdMaxEntropy(in);
		}
	public static double findThresholdMaxEntropy(EvPixels in)
		{
		int numPixels=in.getWidth()*in.getHeight();
		
		SortedMap<Integer,Integer> hist=new TreeMap<Integer,Integer>(Histogram.intHistogram(in));
		SortedMap<Integer,Integer> cumHist=Histogram.makeHistCumulative(hist);
		
		
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
		double maxVal=Double.MIN_VALUE;
		double maxThres=0;
		while(it.hasNext())
			{
			Map.Entry<Integer, Integer> curEntry=it.next();
			int curNum=itCount.next();
			
			double curThres=curEntry.getKey();
			double curCumS=curEntry.getValue();
			
			double a=curCumS/curNum + Math.log(curNum);
			int numPixB=numPixels-curNum;
			double b=(totalS-curCumS)/numPixB + Math.log(numPixB); 
			
			double curVal=a+b;
			if(curVal>maxVal)
				{
				maxVal=curVal;
				maxThres=curThres;
				}
			System.out.println(curVal);
			}
		
		return maxThres;
		}
	}