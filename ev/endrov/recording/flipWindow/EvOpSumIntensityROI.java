/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.flipWindow;

import java.util.TreeMap;

import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvStack;
import endrov.roi.LineIterator;
import endrov.roi.ROI;
import endrov.util.EvDecimal;

/**
 * Calculate values from e.g. a FLIP experiment
 * 
 */
public class EvOpSumIntensityROI 
	{
	public TreeMap<Double,Double> recoveryCurve=new TreeMap<Double, Double>();

	
	public EvOpSumIntensityROI(EvChannel ch, ROI roi)
		{
		recoveryCurve=new TreeMap<Double, Double>();
		
		//Collect curve
		for(EvDecimal f:ch.getFrames())
			{
			double sum=levelFromStack(ch.getStack(f), roi, "foo", f);
			recoveryCurve.put(f.doubleValue(), sum);
			}
		}

	/**
	 * Get the value from one stack
	 */
	private static double levelFromStack(EvStack in, ROI roi, String channel, EvDecimal frame)
		{
		double sum=0;
		for(int z=0;z<in.getDepth();z++)
			{
			double zpos=in.transformImageWorldZ(z);//in.resZ*z;
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