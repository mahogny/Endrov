package endrov.flowBinaryMorph;


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
 * Flow unit: white tophat 2D
 * @author Johan Henriksson
 *
 */
public class FlowUnitBinMorphThick2D extends FlowUnitBasic
	{
	public static final String showName="Binary Thicken 2D";
	private static final String metaType="binaryThick2d";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitBinMorphThick2D.class, null,
				"Binary morphological thickening operation, slice by slice"));
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
		types.put("kernelHit", BinMorphKernel.FLOWTYPE);
		types.put("kernelMiss", BinMorphKernel.FLOWTYPE);
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
		BinMorphKernel kernelHit=(BinMorphKernel)flow.getInputValue(this, exec, "kernelHit");
		BinMorphKernel kernelMiss=(BinMorphKernel)flow.getInputValue(this, exec, "kernelHit");
		checkNotNull(a,kernelHit,kernelMiss);

		lastOutput.put("out", new EvOpBinMorphThick2D(kernelHit,kernelMiss).exec1Untyped(a));
		}

	
	}
