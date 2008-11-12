package endrov.flow.std.collection;

import java.awt.Color;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;

public class FlowUnitConcat extends FlowUnitBasic
	{
	public String getBasicShowName()
		{
		return "Concat";
		}
	public ImageIcon getIcon(){return null;}

	public Color getBackground()
		{
		return new Color(200,255,200);
		}

	private static final String metaType="concat";
	public String storeXML(Element e)
		{
		return metaType;
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
	
	public void evaluate(Flow flow) throws Exception
		{
		lastOutput.clear();
		Object a=flow.getInputValue(this, "A");
		Object b=flow.getInputValue(this, "B");
		if(a instanceof String && b instanceof String)
			lastOutput.put("C", ((String)a)+((String)b));
		else
			throw new BadTypeFlowException("Unsupported collection type "+a.getClass()+" & "+b.getClass());
		}

	
	}
