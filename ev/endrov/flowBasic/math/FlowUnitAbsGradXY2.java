package endrov.flowBasic.math;


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
 * Flow unit: abs(nabla_xy)^2
 * @author Johan Henriksson
 *
 */
public class FlowUnitAbsGradXY2 extends FlowUnitBasic
	{
	public static final String showName="|∇xy|²";
	private static final String metaType="absGradXY2";
	
	public static final ImageIcon icon=null;
	//new ImageIcon(CategoryInfo.class.getResource("jhFlowCategoryFindMaximas.png"));

	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitAbsGradXY2.class, icon,
				"Indicator of steep slope"));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return icon;}
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
		
		AnyEvImage a=(AnyEvImage)flow.getInputValue(this, exec, "image");

		lastOutput.put("out", new EvOpImageAbsGradXY2().exec1Untyped(a));
		}

	}
