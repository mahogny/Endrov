package endrov.flow.std.collection;

import java.awt.Color;
import java.util.Collection;
import java.util.Map;
import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
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
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,"Size",metaType,FlowUnitSize.class, icon,"Size of string or list"));
		}
	
	public String getBasicShowName(){return "Size";}
	public ImageIcon getIcon(){return icon;}
	public Color getBackground(){return CategoryInfo.bgColor;}

	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types)
		{
		types.put("in", null);
		}
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types)
		{
		types.put("size", FlowType.TINTEGER);
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
