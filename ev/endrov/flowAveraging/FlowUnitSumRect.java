package endrov.flowAveraging;


import java.awt.Color;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.AnyEvImage;

/**
 * Flow unit: moving sum
 * @author Johan Henriksson
 *
 */
public class FlowUnitSumRect extends FlowUnitBasic
	{
	public static final String showName="Moving sum (rect)";
	private static final String metaType="sumRect";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitSumRect.class, null,
				"Local sum of square region moving over image"));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return null;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("image", FlowType.ANYIMAGE);
		types.put("pw", FlowType.TNUMBER);
		types.put("ph", FlowType.TNUMBER);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", FlowType.ANYIMAGE); //TODO same type as "image"
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		AnyEvImage a=(AnyEvImage)flow.getInputValue(this, exec, "image");
		Number pw=(Number)flow.getInputValue(this, exec, "pw");
		Number ph=(Number)flow.getInputValue(this, exec, "ph");

		lastOutput.put("out", new EvOpSumRect(pw,ph).exec1Untyped(a));
		}

	
	}
