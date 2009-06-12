package endrov.unsortedImageFilters.misc;


import java.awt.Color;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;

/**
 * Flow unit: shift every second line to correct for confocal scanning
 * @author Johan Henriksson
 *
 */
public class FlowUnitConfocalShiftCorrection extends FlowUnitBasic
	{
	public static final String showName="Average Z";
	private static final String metaType="projectionAvgZ";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitConfocalShiftCorrection.class, null,
				"Calculate average in Z-direction"));
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
		types.put("shift", FlowType.TNUMBER);
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
		Object a=flow.getInputValue(this, exec, "image");
		Number b=(Number)flow.getInputValue(this, exec, "shift");
		checkNotNull(a);
		lastOutput.put("out", new EvOpConfocalShiftCorrection(b).exec1Untyped(a));
		}

	
	}
