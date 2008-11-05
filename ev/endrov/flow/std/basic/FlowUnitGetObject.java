package endrov.flow.std.basic;

import java.awt.Color;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;

public class FlowUnitGetObject extends FlowUnitBasic
	{
	public String getBasicName()
		{
		return "GetObjects";
		}
	
	public Color getBackground()
		{
		return new Color(200,255,200);
		}

	
	
	/** Get types of flows in */
	public SortedMap<String, FlowType> getTypesIn()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("data", null);
		types.put("objectClass", null);
		return types;
		}
	/** Get types of flows out */
	public SortedMap<String, FlowType> getTypesOut()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("objects", null);
		return types;
		}
	
	}
