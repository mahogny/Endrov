package endrov.flow.std.collection;

import java.awt.Color;
import java.util.Collection;
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

/**
 * Size of a collection, string etc
 * @author Johan Henriksson
 *
 */
public class FlowUnitSize extends FlowUnitBasic
	{
	private static final String metaType="size";

	
	private static ImageIcon icon=new ImageIcon(FlowUnitSize.class.getResource("jhSize.png"));

	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Collection","Size",metaType,FlowUnitSize.class, icon));
		}
	
	public String getBasicShowName()
		{
		return "Size";
		}
	public ImageIcon getIcon(){return icon;}

	public Color getBackground()
		{
		return new Color(200,255,200);
		}

	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	
	
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
		types.put("size", FlowType.TINTEGER);
		return types;
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "in");
		if(a instanceof String)
			lastOutput.put("size", ((String)a).length());
		if(a instanceof Collection<?>)
			lastOutput.put("size", ((Collection<?>)a).size());
		else
			throw new BadTypeFlowException("Unsupported collection type "+a.getClass());
		}

	
	}
