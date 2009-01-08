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
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;

public class FlowUnitConcat extends FlowUnitBasic
	{
	private static final String metaType="concat";

	
	private static ImageIcon icon=new ImageIcon(FlowUnitConcat.class.getResource("jhConcat.png"));

	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Collection","Concat",metaType,FlowUnitConcat.class, icon));
		}

	
	public String getBasicShowName()
		{
		return "Concat";
		}
	public ImageIcon getIcon(){return icon;}

	public Color getBackground()
		{
		return new Color(200,255,200);
		}

	public String toXML(Element e)
		{
		return metaType;
		}

	public void fromXML(Element e)
		{
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
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");
		if(a instanceof String && b instanceof String)
			lastOutput.put("C", ((String)a)+((String)b));
		else
			throw new BadTypeFlowException("Unsupported collection type "+a.getClass()+" & "+b.getClass());
		}

	
	}
