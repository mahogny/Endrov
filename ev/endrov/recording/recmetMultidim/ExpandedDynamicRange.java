package endrov.recording.recmetMultidim;

import endrov.recording.widgets.RecSettingsChannel;
import endrov.typeImageset.EvStack;
import endrov.util.collection.EvListUtil;
import endrov.util.math.EvDecimal;

/**
 * Dynamic range expansion utility functions
 * 
 * @author Johan Henriksson
 */
public class ExpandedDynamicRange
	{
	/**
	 * Expand the dynamic range of a channel by continuously setting the exposure time to be optimal. This function
	 * should be calculated for a stack once it has completed. Will only have an effect if the setting is enabled 
	 */
	public static void expandDynamicRangeByExposureTime(RecSettingsChannel.OneChannel settings, EvStack stack) 
		{
		if(settings.adjustRangeByExposure)
			{
			
			//Calculate current highest value
			//If this is too slow, then it might be an idea to d
			Integer currentSignal=null;
			for(int az=0;az<stack.getDepth();az++)
				{
				int[] arr=stack.getPlane(0).getPixels().convertToInt(true).getArrayInt();
				int percPlane=EvListUtil.findPercentileInt(arr, 0.99);
				if(currentSignal==null || percPlane>currentSignal)
					currentSignal=percPlane;
				}
	
			
			
			//Assume linear model, signal = lightIntensity * exposureTime * C
			//Find optimal value, using equations
			//currentSignal = C*lightIntensity * exposureTimeCurrent
			//targetSignal  = C*lightIntensity * exposureTimeTarget
			
			double curExp=settings.exposure.doubleValue();
			double targetSignal=settings.adjustRangeTargetSignal;
			double targetExp=curExp*targetSignal;
	
			settings.exposure=new EvDecimal(targetExp);
			
			//TODO should store the last exposure time in the channel
			}
		}
	
	
	
	}
