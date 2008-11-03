package endrov.flow.basic;

import java.awt.Color;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.flow.FlowUnitBasic;
import endrov.flow.type.FlowType;

public class FlowUnitImservLoad extends FlowUnitBasic
	{
	public String getBasicName()
		{
		return "ImServ Load";
		}
	
	public Color getBackground()
		{
		return FlowUnitImserv.bgColor;
		}

	
	
	/** Get types of flows in */
	public SortedMap<String, FlowType> getTypesIn()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("imserv", null);
		types.put("name", null);
		return types;
		}
	/** Get types of flows out */
	public SortedMap<String, FlowType> getTypesOut()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("data", null);
		return types;
		}
	
	}
