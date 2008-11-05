package endrov.flow.std.basic;

import java.awt.Color;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;

/**
 * Flow unit - Custom code (script). User can specify a function to apply on the data
 * @author Johan Henriksson
 *
 */
public class FlowUnitScript extends FlowUnitBasic
	{
	public String getBasicName()
		{
		return "Script";
		}
	
	public Color getBackground()
		{
		return new Color(200,255,255);
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
		types.put("out", null);
		return types;
		}
	
	
	}
