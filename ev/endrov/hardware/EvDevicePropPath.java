package endrov.hardware;


/**
 * Path to device and property
 * @author Johan Henriksson
 *
 */
public class EvDevicePropPath implements Comparable<EvDevicePropPath>
	{
	public EvDevicePath device;
	public String property;
	
	public EvDevicePropPath(EvDevicePath p, String propName)
		{
		this.device = p;
		this.property = propName;
		}

	public int compareTo(EvDevicePropPath o)
		{
		int c=device.compareTo(o.device);
		if(c!=0)
			return c;
		else
			return property.compareTo(o.property);
		}
	
	@Override
	public String toString()
		{
		return device.toString()+"#"+property;
		}
	
	@Override
	public boolean equals(Object obj)
		{
		if(obj instanceof EvDevicePropPath)
			{
			EvDevicePropPath o=(EvDevicePropPath)obj;
			return device.equals(o.device) && property.equals(o.property);
			}
		else
			return false;
		}
	}