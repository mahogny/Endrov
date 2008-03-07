package evplugin.recording;

/**
 * Channel for a microsope. Since many programs allow or prefer that you set the settings once
 * and for all, the API has this construct. Those who do not need it should use MicroscopeChannnelData.
 * 
 * @author Johan Henriksson
 */
public abstract class MicroscopeChannel
	{
	public abstract void setExposureTime(double msec);
	public abstract void setFilter(int id, int num);
	public abstract void setBinning(int num);
	public abstract void setStack(double dz, int numslices);
	}
