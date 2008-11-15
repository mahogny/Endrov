package endrov.flow.std.collection;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitContainer;
import endrov.flow.FlowUnitDeclarationTrivial;

/**
 * Flow unit: Map
 * 
 * -> in in' ----- out' out ->
 * 
 * @author Johan Henriksson
 *
 */
public class FlowUnitMap extends FlowUnitContainer
	{
	private static final String metaType="map";

	public static void initPlugin() {}
	static
		{
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Collection","Map",metaType){
		public FlowUnit createInstance(){return new FlowUnitMap();}});
		}
	
	
	public String getContainerName()
		{
		return "map";
		}

	public String storeXML(Element e)
		{
		e.setAttribute("w",""+contw);
		e.setAttribute("h",""+conth);
		return metaType;
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

	public Set<String> getInsideConns()
		{
		HashSet<String> s=new HashSet<String>();
		s.add("in'");
		s.add("out'");
		return s;
		}
	
	
	public void editDialog(){}


	public void evaluate(Flow flow) throws Exception
	{
	//TODO flowunit
	}

	
	}
