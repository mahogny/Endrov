package endrov.flowBasic.convert;


import java.awt.Color;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.util.Vector2i;

/**
 * Flow unit: Convert from Vector2i
 * @author Johan Henriksson
 *
 */
public class FlowUnitConvertFromVector2i extends FlowUnitBasic
	{
	public static final String showName="From Vector2i";
	private static final String metaType="channelConvertFromVector2i";
	
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitConvertFromVector2i.class, null,
		"Convert from Vector2i");
		Flow.addUnitType(decl);
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return null;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("in", FlowType.TVECTOR2I);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("x", FlowType.TINTEGER); 
		types.put("y", FlowType.TINTEGER); 
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		Vector2i in=(Vector2i)flow.getInputValue(this, exec, "in");
		
		lastOutput.put("x", in.x);
		lastOutput.put("y", in.y);
		}

	
	}
