package endrov.flow.std.collection;

import java.awt.Component;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.basicWindow.FlowExec;
import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitContainer;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;

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

	private static ImageIcon icon=new ImageIcon(FlowUnitMap.class.getResource("jhMap.png"));

	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Collection","Map",metaType,FlowUnitMap.class, icon));
		}
	
	
	public String getContainerName()
		{
		return "map";
		}

	public String toXML(Element e)
		{
		e.setAttribute("w",""+contw);
		e.setAttribute("h",""+conth);
		return metaType;
		}
	public void fromXML(Element e)
		{
		contw=Integer.parseInt(e.getAttributeValue("w"));
		conth=Integer.parseInt(e.getAttributeValue("h"));
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


	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
	//	Map<String,Object> lastOutput=exec.getLastOutput(this);
		//TODO flowunit
		}

	public Component getGUIcomponent(FlowPanel p){return null;}
	public int getGUIcomponentOffsetX(){return 0;}
	public int getGUIcomponentOffsetY(){return 0;}

	}
