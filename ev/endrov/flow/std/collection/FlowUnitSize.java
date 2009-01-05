package endrov.flow.std.collection;

import java.awt.Color;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.basicWindow.FlowExec;
import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclarationTrivial;

public class FlowUnitSize extends FlowUnitBasic
	{
	private static final String metaType="size";

	public static void initPlugin() {}
	static
		{
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Collection","Size",metaType){
		public FlowUnit createInstance(){return new FlowUnitSize();}});
		}
	
	public String getBasicShowName()
		{
		return "Size";
		}
	public ImageIcon getIcon(){return null;}

	public Color getBackground()
		{
		return new Color(200,255,200);
		}

	
	public String storeXML(Element e)
		{
		return metaType;
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
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "in");
		if(a instanceof String)
			lastOutput.put("size", ((String)a).length());
		else
			throw new BadTypeFlowException("Unsupported collection type "+a.getClass());
		}

	
	}
