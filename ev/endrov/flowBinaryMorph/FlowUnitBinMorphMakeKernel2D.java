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
import endrov.imageset.EvPixels;

/**
 * Flow unit: create kernel from pixel plane
 * @author Johan Henriksson
 *
 */
public class FlowUnitBinMorphMakeKernel2D extends FlowUnitBasic
	{
	public static final String showName="Binary Make Kernel 2D";
	private static final String metaType="binaryMakeKernel2d";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitBinMorphMakeKernel2D.class, null,
				"Create kernel for binary morphology"));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return null;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("image", FlowType.TEVPIXELS);
		types.put("centerx", FlowType.TNUMBER);
		types.put("centery", FlowType.TNUMBER);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", BinMorphKernel.FLOWTYPE); //TODO same type as "image"
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		EvPixels a=(EvPixels)flow.getInputValue(this, exec, "image");
		Number centerX=(Number)flow.getInputValue(this, exec, "centerx");
		Number centerY=(Number)flow.getInputValue(this, exec, "centery");
		checkNotNull(a,centerX,centerY);
		
		lastOutput.put("out", new BinMorphKernel(a,centerX.intValue(),centerY.intValue()));
		}

	
	}
