package endrov.flow.std.math;

import java.awt.Color;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;

public class FlowUnitDiv extends FlowUnitBasic
	{
	public String getBasicName()
		{
		return "A/B";
		}
	
	public Color getBackground()
		{
		return new Color(200,255,200);
		}

	
	
	/** Get types of flows in */
	public SortedMap<String, FlowType> getTypesIn()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("A", null);
		types.put("B", null);
		return types;
		}
	/** Get types of flows out */
	public SortedMap<String, FlowType> getTypesOut()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("C", null);
		return types;
		}
	
	}
