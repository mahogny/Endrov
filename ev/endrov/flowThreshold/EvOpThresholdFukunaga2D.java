/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowThreshold;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.util.ProgressHandle;


/**
 * Fukunaga thresholding
 * <br/>
 * This is the straight extension of Otsu to multiple classes
 * <br/>
 * Complexity O((w*h)^(numberOfClasses-1)+numColorUsed*log(numColorUsed))
 */
public class EvOpThresholdFukunaga2D extends Threshold2D
	{
	private final int numClasses;
	
	public EvOpThresholdFukunaga2D(int numClasses)
		{
		super(MASK);
		this.numClasses=numClasses;
		}

	protected double[] getThreshold(ProgressHandle progh, EvPixels in)
		{
		return findThreshold(progh, in, numClasses);
		}
	
	public static double[] findThreshold(ProgressHandle progh, EvPixels in, int numClasses)
		{
		
		in=in.getReadOnly(EvPixelsType.DOUBLE);
		int numPixels=in.getWidth()*in.getHeight();

		System.out.println("starting calc "+numPixels);

		//TODO this is slow. use a specialized counting map!
		in=in.getReadOnly(EvPixelsType.DOUBLE);
		double[] inPixels=in.getArrayDouble();
		Map<Double,Integer> histFirst=new HashMap<Double, Integer>(); 
		for(double p:inPixels)
			{
			Integer cnt=histFirst.get(p);
			if(cnt==null)
				cnt=1;
			else
				cnt++;
			histFirst.put(p,cnt);
			}

		//Sort histogram
		TreeMap<Double,Integer> histSorted=new TreeMap<Double,Integer>(histFirst);
		
		//Calculate cumulative sums over probabilities
		double curCumsumHist=0;
		double curCumsumHistTimesValue=0;
		double[] cumsumKey=new double[histSorted.size()+1];
		double[] cumsumHist=new double[histSorted.size()+1];
		double[] cumsumHistTimesValue=new double[histSorted.size()+1];
		int curNumEntry=0;
		double factor=1.0/numPixels; //It should be over the probabilities of values
		for(Map.Entry<Double, Integer> e:histSorted.entrySet())
			{
			double key=e.getKey();
			double value=e.getValue()*factor;
			cumsumKey[curNumEntry]=key;
			
			curCumsumHist+=value;
			curCumsumHistTimesValue+=value*key;
			
			curNumEntry++;
			cumsumHist[curNumEntry]=curCumsumHist;
			cumsumHistTimesValue[curNumEntry]=curCumsumHistTimesValue;
			//TODO choice of positions, what makes sense? should the first position contain 0?
			}
		int end=cumsumKey.length-1;
		cumsumKey[end]=cumsumKey[end-1]+1000;

		/** thresholds are >= */

		System.out.println("starting "+cumsumKey.length);
		
		if(numClasses<2)
			return new double[]{cumsumKey[0]};
		else
			{
			Best best=new Best();
			best.thres=new double[numClasses-1];
			best.sigma=Double.MIN_VALUE;
			double mg=cumsumHistTimesValue[cumsumHist.length-1];
			recurse(cumsumKey, cumsumHist, cumsumHistTimesValue, new int[numClasses-1], 0, best, 0, mg);
			System.out.println("Best thresholds "+Arrays.toString(best.thres));
			return best.thres;
			}
		}
	
	private static class Best
		{
		private double[] thres;
		private double sigma;
		}
	
	private static void recurse(
			double[] cumsumKey,double[] cumsumHist,double[] cumsumHistTimesValue,
			int[] curThres,int curThresIndex,
			Best best,double partSigma,double mg)
		{
		//Find where to start from. Region length always >=1
		int lastIndex;
		if(curThresIndex==0)
			lastIndex=0;
		else
			lastIndex=curThres[curThresIndex-1];
		
		if(curThresIndex<curThres.length-1)
			{
			for(int index=lastIndex+1;index<cumsumKey.length;index++)
				{
				curThres[curThresIndex]=index;
				
				//Sum up sigma
				double Pi=cumsumHist[index]-cumsumHist[lastIndex];
				double mi=(cumsumHistTimesValue[index]-cumsumHistTimesValue[lastIndex])/Pi;
				double diff=mi-mg;
				double partSigma2=partSigma + Pi*diff*diff;
				
				//Loop over the next threshold
				recurse(cumsumKey, cumsumHist, cumsumHistTimesValue, curThres, curThresIndex+1, best, partSigma2, mg);
				}
			}
		else
			{
			//Sun Java does not do tail-call optimization. For efficiency the inner check is
			//done in a loop. All other loops should be orders of magnitudes less frequent.
			//Do not loop all the way to the end. The last interval must be length >=0
			for(int index=lastIndex+1;index<cumsumKey.length-2;index++)
				{
				//Sum up sigma
				double Pi=cumsumHist[index]-cumsumHist[lastIndex];
				double mi=(cumsumHistTimesValue[index]-cumsumHistTimesValue[lastIndex])/Pi;
				double diffi=mi-mg;

				//Also add the last interval, whatever is left over
				int end=cumsumHist.length-1;
				double Plast=cumsumHist[end]-cumsumHist[index+1];
				double mlast=(cumsumHistTimesValue[end]-cumsumHistTimesValue[index+1])/Plast;
				double diffLast=mlast-mg;
				
				double sigma=partSigma + Pi*diffi*diffi + Plast*diffLast*diffLast;
				
				//Check if these are the best thresholds so far
				if(sigma>best.sigma)
					{
					best.sigma=sigma;
					curThres[curThresIndex]=index;
					for(int i=0;i<curThres.length;i++)
						best.thres[i]=cumsumKey[curThres[i]];
					}
				
				}
			}
		
		
		}
	}
