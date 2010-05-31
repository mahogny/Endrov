/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.frapWindow;

import java.util.Map;
import java.util.TreeMap;

import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvStack;
import endrov.roi.LineIterator;
import endrov.roi.ROI;
import endrov.util.EvDecimal;

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

	
	public EvOpCalcFRAP(EvChannel ch, ROI roi, Number before, Number after, String channelName)
		{
		//Collect curve
		initialConcentration=0;
		int framesBeforeCount=0;
		for(EvDecimal f:ch.imageLoader.keySet())
			if(f.doubleValue()<=before.doubleValue())
				{
				initialConcentration+=levelFromStack(ch.imageLoader.get(f), roi, channelName, f);
				framesBeforeCount++;
				}
			else if(f.doubleValue()>after.doubleValue())
				{
				double sum=levelFromStack(ch.imageLoader.get(f), roi, channelName, f);
				recoveryCurve.put(f.doubleValue(), sum);
				}
		initialConcentration/=framesBeforeCount;
		

		//Fit parameters
		double minAfterFit=recoveryCurve.get(recoveryCurve.firstKey());
		double maxAfterFit=recoveryCurve.get(recoveryCurve.lastKey());

		//Mobile fraction
		mobileFraction=(maxAfterFit-minAfterFit)/(initialConcentration-minAfterFit);
		
		//Life-time
		//y=a(1-exp(-t/tau)) + c
		//From here I derived a condition on the half recovered concentration
		double halfValue=(maxAfterFit+minAfterFit)/2;
		double halfPos=0;
		for(Map.Entry<Double, Double> e:recoveryCurve.entrySet())
			if(e.getValue()>halfValue)
				{
				halfPos=e.getKey();
				break;
				}
		lifetime=-halfPos/Math.log(0.5);
		
		
		}
	
	
	

	/**
	 * Get the value from one stack
	 */
	private static double levelFromStack(EvStack in, ROI roi, String channel, EvDecimal frame)
		{
		double sum=0;
		for(int z=0;z<in.getDepth();z++)
			{
			EvDecimal zpos=in.resZ.multiply(z);

			//System.out.println("doing z "+z+"  "+zpos+"   --  "+frame+"   ch:"+channel);
			EvImage evim=in.getInt(z);
			LineIterator it=roi.getLineIterator(in, evim, channel, frame, zpos);
			while(it.next())
				{
				double[] arr=evim.getPixels().convertToDouble(true).getArrayDouble();
				int w=in.getWidth();
				for(LineIterator.LineRange range:it.ranges)
					for(int i=it.y*w+range.start;i<it.y*w+range.end;i++)
						sum+=arr[i];
				}
			}
		return sum;
		}
	
	
	}