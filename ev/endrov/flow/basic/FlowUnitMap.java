package endrov.flow.basic;

import java.util.Map;
import java.util.TreeMap;

import endrov.flow.FlowType;
import endrov.flow.FlowUnitContainer;

/**
 * Flow unit: Map
 * @author Johan Henriksson
 *
 */
public class FlowUnitMap extends FlowUnitContainer
	{

	public String getContainerName()
		{
		return "map";
		}

	
	/** Get types of flows in */
	public Map<String, FlowType> getTypesIn()
		{
		Map<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("in", null);
		types.put("out", null);
		return types;
		}
	/** Get types of flows out */
	public Map<String, FlowType> getTypesOut()
		{
		Map<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("in'", null);
		types.put("out'", null);
		return types;
		}

	
	
	}
