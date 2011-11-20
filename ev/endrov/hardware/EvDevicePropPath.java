package endrov.hardware;


/**
 * Path to device and property
 * @author Johan Henriksson
 *
 */
public class EvDevicePropPath implements Comparable<EvDevicePropPath>
	{
	private EvDevicePath devicePath;
	private String property;
	
	
	
	public EvDevicePropPath(EvDevicePath p, String propName)
		{
		this.devicePath = p;
		this.property=propName;
		}

	public int compareTo(EvDevicePropPath o)
		{
		int c=devicePath.compareTo(o.devicePath);
		if(c!=0)
			return c;
		else
			return getProperty().compareTo(o.getProperty());
		}
	
	@Override
	public String toString()
		{
		return devicePath.toString()+"#"+getProperty();
		}
	
	@Override
	public boolean equals(Object obj)
		{
		if(obj instanceof EvDevicePropPath)
			{
			EvDevicePropPath o=(EvDevicePropPath)obj;
			return devicePath.equals(o.devicePath) && getProperty().equals(o.getProperty());
			}
		else
			return false;
		}


	public EvDevice getDevice()
		{
		return devicePath.getDevice();
		}

	public String getProperty()
		{
		return property;
		}
	}