package endrov.flow.basic;

import java.awt.Color;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.flow.FlowUnitBasic;
import endrov.flow.type.FlowType;

public class FlowUnitHeadTail extends FlowUnitBasic
	{
	public String getBasicName()
		{
		return "HeadTail";
		}
	
	public Color getBackground()
		{
		return new Color(200,255,200);
		}

	
	
	/** Get types of flows in */
	public SortedMap<String, FlowType> getTypesIn()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("in", null);
		return types;
		}
	/** Get types of flows out */
	public SortedMap<String, FlowType> getTypesOut()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("head", null);
		types.put("tail", null);
		return types;
		}
	
	}
