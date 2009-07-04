package endrov.flowThreshold;


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
 * Flow unit: black tophat 2D
 * @author Johan Henriksson
 *
 */
public class FlowUnitThresholdOtsu2D extends FlowUnitBasic
	{
	public static final String showName="Otsu threshold";
	private static final String metaType="thresholdOtsu";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitThresholdOtsu2D.class, CategoryInfo.icon,
				"Find optimal threshold which minimizes signal and background variance, slice by slice"));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return CategoryInfo.icon;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("image", FlowType.ANYIMAGE);
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
		checkNotNull(a);
		System.out.println("??????here");
		
		AnyEvImage out=new EvOpThresholdOtsu2D().exec1Untyped(a);
		System.out.println("/////"+out);
		lastOutput.put("out", out);
		}

	
	}
