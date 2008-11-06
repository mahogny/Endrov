package endrov.flow.std.collection;

import java.awt.Color;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;

public class FlowUnitSize extends FlowUnitBasic
	{
	public String getBasicName()
		{
		return "Size";
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
		types.put("size", null);
		return types;
		}
	
	public void evaluate(Flow flow) throws Exception
		{
		lastOutput.clear();
		Object a=flow.getInputValue(this, "in");
		if(a instanceof String)
			lastOutput.put("size", ((String)a).length());
		else
			throw new BadTypeFlowException("Unsupported collection type "+a.getClass());
		}

	
	}
