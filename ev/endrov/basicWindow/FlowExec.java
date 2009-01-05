package endrov.basicWindow;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import endrov.flow.FlowUnit;

/**
 * To allow multiple instances of a running Flow, the state has to be separated from the flow declaration
 * 
 * @author Johan Henriksson
 *
 */
public class FlowExec
	{

	private WeakHashMap<FlowUnit, Map<String,Object>> lastOutputMap=new WeakHashMap<FlowUnit, Map<String,Object>>();
	
	public Map<String,Object> getLastOutput(FlowUnit unit)
		{
		Map<String,Object> lastOutput=lastOutputMap.get(unit);
		if(lastOutput==null)
			lastOutputMap.put(unit, lastOutput=new HashMap<String, Object>());
		return lastOutput;
		}

	public String toString()
		{
		StringBuffer sb=new StringBuffer();
		for(Map.Entry<FlowUnit, Map<String,Object>> e:lastOutputMap.entrySet())
			{
			sb.append(" "+e.getKey()+"\n");
			for(Map.Entry<String, Object> bind:e.getValue().entrySet())
				sb.append("    "+bind.getKey()+"\t=>\t"+bind.getValue()+"\n");
			}
		return sb.toString();
		}
	
	
	
	
	
	}