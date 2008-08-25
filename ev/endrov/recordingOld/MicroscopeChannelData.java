package endrov.recordingOld;

import java.util.*;

/**
 * Channel where settings are just dumped, they will not be set until exactly when
 * image is captured
 * 
 * @author Johan Henriksson
 */
public class MicroscopeChannelData extends MicroscopeChannel  
	{
	public String ostname;
	public double exptime;
	public Map<Integer, Integer> filter=new HashMap<Integer, Integer>();
	public int binning;
	public double dz;
	public int numslices;
	
	public void setExposureTime(double msec)
		{
		exptime=msec;
		}
	public void setFilter(int id, int num)
		{
		filter.put(id,num);
		}
	public void setBinning(int num)
		{
		binning=num;
		}
	public void setStack(double dz, int numslices)
		{
		this.dz=dz;
		this.numslices=numslices;
		}

	
	}
