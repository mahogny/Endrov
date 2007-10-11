package evplugin.roi;


import evplugin.metadata.*;

public abstract class ROI extends MetaObject
	{
	public static final int LINE_ITERATOR=1;
	public static final int PIXEL_ITERATOR=2;
	
	
	public String getMetaTypeDesc()
		{
		return "ROI";
		}

	
	
	
	}
