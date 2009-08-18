package endrov.flowMorphology;


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
 * Flow unit: thinen 2D
 * @author Johan Henriksson
 *
 */
public class FlowUnitMorphThinBinary2D extends FlowUnitBasic
	{
	public static final String showName="Thinen (binary) 2D";
	private static final String metaType="binaryThin2d";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitMorphThinBinary2D.class, CategoryInfo.icon,
				"Binary morphological Thinening operation, slice by slice"));
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
		types.put("kernelHit", MorphKernel.FLOWTYPE);
		types.put("kernelMiss", MorphKernel.FLOWTYPE);
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
		MorphKernel kernelHit=(MorphKernel)flow.getInputValue(this, exec, "kernelHit");
		MorphKernel kernelMiss=(MorphKernel)flow.getInputValue(this, exec, "kernelHit");

		lastOutput.put("out", new EvOpBinMorphThin2D(kernelHit,kernelMiss).exec1Untyped(a));
		}

	
	}