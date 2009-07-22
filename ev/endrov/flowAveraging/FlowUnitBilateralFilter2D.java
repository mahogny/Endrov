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
 * Flow unit: bilateral filter
 * @author Johan Henriksson
 *
 */
public class FlowUnitBilateralFilter2D extends FlowUnitBasic
	{
	public static final String showName="Bilateral filter";
	private static final String metaType="filterBilateral2D";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitBilateralFilter2D.class, null,
				"Local average, but only average using pixels within threshold of current pixel value. This improves edge conservation"));
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
		types.put("threshold", FlowType.TNUMBER);
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
		Number threshold=(Number)flow.getInputValue(this, exec, "threshold");
		
		checkNotNull(a,pw,ph,threshold);

		lastOutput.put("out", new EvOpBilateralFilter2D(pw,ph,threshold).exec1Untyped(a));
		}

	
	}
