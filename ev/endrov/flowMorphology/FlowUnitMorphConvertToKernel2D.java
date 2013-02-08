/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
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
import endrov.typeImageset.EvPixels;

/**
 * Flow unit: create kernel from pixel plane
 * @author Johan Henriksson
 *
 */
public class FlowUnitMorphConvertToKernel2D extends FlowUnitBasic
	{
	public static final String showName="Convert to Kernel 2D";
	private static final String metaType="morphologyConvertToKernel2d";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitMorphConvertToKernel2D.class, CategoryInfo.icon,
				"Create kernel for binary morphology"));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return CategoryInfo.icon;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("image", FlowType.TEVPIXELS);
		types.put("centerx", FlowType.TNUMBER);
		types.put("centery", FlowType.TNUMBER);
		types.put("isBinary", FlowType.TBOOLEAN);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", MorphKernel.FLOWTYPE); //TODO same type as "image"
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		EvPixels a=(EvPixels)flow.getInputValue(this, exec, "image");
		Number centerX=(Number)flow.getInputValue(this, exec, "centerx");
		Number centerY=(Number)flow.getInputValue(this, exec, "centery");
		Boolean isBinary=(Boolean)flow.getInputValue(this, exec, "isBinary");
		
		if(isBinary)
			lastOutput.put("out", new MorphKernelGeneralBinary(a,centerX.intValue(),centerY.intValue()));
		else
			lastOutput.put("out", new MorphKernelGeneralGray(a,centerX.intValue(),centerY.intValue()));
		}

	public String getHelpArticle()
		{
		return "Flow Morphology";
		}
	
	}
