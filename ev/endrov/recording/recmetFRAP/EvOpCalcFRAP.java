/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.recmetFRAP;

import java.util.Map;
import java.util.TreeMap;

import endrov.roi.LineIterator;
import endrov.roi.ROI;
import endrov.typeImageset.EvChannel;
import endrov.typeImageset.EvImagePlane;
import endrov.typeImageset.EvStack;
import endrov.util.ProgressHandle;
import endrov.util.math.EvDecimal;

/**
 * Calculate values from a FRAP experiment
 * 
 */
public class EvOpCalcFRAP 
	{
	public double lifetime;
	public double initialConcentration;
	double mobileFraction;
	public TreeMap<Double,Double> recoveryCurve=new TreeMap<Double, Double>();

	
	public EvOpCalcFRAP(ProgressHandle progh, EvChannel ch, ROI roi, Number before, Number after, String channelName)
		{
		TreeMap<Double,Double> startOfCurve=new TreeMap<Double, Double>();
		TreeMap<Double,Double> endOfCurve=new TreeMap<Double, Double>();
		
		//Collect curve
		initialConcentration=0;
		int framesBeforeCount=0;
		for(EvDecimal f:ch.getFrames())
			if(f.doubleValue()<=before.doubleValue())
				{
				double sum=levelFromStack(progh, ch.getStack(progh, f), roi, channelName, f);
				initialConcentration+=sum;
				framesBeforeCount++;
				startOfCurve.put(f.doubleValue(), sum);
				}
			else if(f.doubleValue()>after.doubleValue())
				{
				double sum=levelFromStack(progh, ch.getStack(progh, f), roi, channelName, f);
				endOfCurve.put(f.doubleValue(), sum);
				}
		initialConcentration/=framesBeforeCount;
		

		//Fit parameters
		double minAfterFit=endOfCurve.get(endOfCurve.firstKey());
		double maxAfterFit=endOfCurve.get(endOfCurve.lastKey());

		//Mobile fraction
		mobileFraction=(maxAfterFit-minAfterFit)/(initialConcentration-minAfterFit);
		
		System.out.println("mobile "+mobileFraction);
		
		//Life-time
		//y=a(1-exp(-t/tau)) + c
		//From here I derived a condition on the half recovered concentration
		double halfValue=(maxAfterFit+minAfterFit)/2;
		double halfPos=0;
		for(Map.Entry<Double, Double> e:endOfCurve.entrySet())
			if(e.getValue()>halfValue)
				{
				halfPos=e.getKey();
				break;
				}
		lifetime=-halfPos/Math.log(0.5);
		
		//Join with first values for presentation
//		recoveryCurve.putAll(startOfCurve);
		this.recoveryCurve.clear();
		for(Map.Entry<Double, Double> e:endOfCurve.entrySet())
			this.recoveryCurve.put(e.getKey(), e.getValue()-minAfterFit);
		for(Map.Entry<Double, Double> e:startOfCurve.entrySet())
			this.recoveryCurve.put(e.getKey(), e.getValue()-minAfterFit);
		
		
		System.out.println(endOfCurve);
		}
	
	
	

	/**
	 * Get the value from one stack
	 */
	private static double levelFromStack(ProgressHandle progh, EvStack in, ROI roi, String channel, EvDecimal frame)
		{
		double sum=0;
		for(int z=0;z<in.getDepth();z++)
			{
			//EvDecimal zpos=in.resZ.multiply(z);

			//System.out.println("doing z "+z+"  "+zpos+"   --  "+frame+"   ch:"+channel);
			EvImagePlane evim=in.getPlane(z);
			LineIterator it=roi.getLineIterator(progh, in, evim, channel, frame, in.transformImageWorldZ(z));
			while(it.next())
				{
				double[] arr=evim.getPixels(progh).convertToDouble(true).getArrayDouble();
				int w=in.getWidth();
				for(LineIterator.LineRange range:it.ranges)
					for(int i=it.y*w+range.start;i<it.y*w+range.end;i++)
						sum+=arr[i];
				}
			}
		return sum;
		}
	
	
	}