package endrov.flow.basic;

import java.awt.Color;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;

public class FlowUnitImservQuery extends FlowUnitBasic
	{
	public String getBasicName()
		{
		return "ImServ Query";
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
		types.put("query", null);
		return types;
		}
	/** Get types of flows out */
	public SortedMap<String, FlowType> getTypesOut()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("nameList", null);
		return types;
		}
	
	}
