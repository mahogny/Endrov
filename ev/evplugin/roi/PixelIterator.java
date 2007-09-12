package evplugin.roi;

public abstract class PixelIterator
	{
	public int frame;
	public int x,y,z;
	public String channel;
	
	public abstract boolean next();
	
	
	}
