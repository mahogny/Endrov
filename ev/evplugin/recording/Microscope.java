package evplugin.recording;

import java.awt.image.*;
import java.util.*;

/**
 * Definition of a microscope
 * @author Johan Henriksson
 */
public abstract class Microscope
	{
	public static final int EMISSION=0, EXCITATION=1;
	public static final int AXISX=0, AXISY=1, AXISPZ=2, AXISMZ=3;
	
	
	/** Get existing axis */
	public abstract int[] getAxisId();
	
	/** Read out value for all axis */
	public abstract double[] getAxis();
	
	/** Set value for all axis */
	public abstract void setAxis(double[] a);
	
	/** Get interval for axis, null if there is none */
	public abstract double[] getAxisRange(int id);
	
	/** Get or create a new channel */
	public abstract MicroscopeChannel getChannel(String ch);
	
	/** Set current channel */
	public abstract void setChannel(MicroscopeChannel ch);
	
	/** Get list of filters */
	public abstract Map<Integer, String> getFilters(int id);
	
	/** Capture a single image */
	public abstract BufferedImage captureImage();
	
	/** Capture an entire stack */
	public abstract BufferedImage[] captureStack();
	
	/** Get value of axis ID */
	public double getAxis(int id)
		{
		return getAxis()[id];
		}
	/** Set value of axis ID */
	public void setAxis(int id, double a)
		{
		double axis[]=getAxis();
		axis[id]=a;
		setAxis(axis);
		}

	/** Get filterID corresponding to name and type of filter */
	public Integer getFilterId(int id, String name)
		{
		Map<Integer, String> map=getFilters(id);
		for(int i:map.keySet())
			if(map.get(i).equals(name))
				return i;
		return null;
		}
	
	}
