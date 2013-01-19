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
import endrov.typeImageset.AnyEvImage;

/**
 * Flow unit: hit-miss 2D
 * @author Johan Henriksson
 *
 */
public class FlowUnitMorphHitmissBinary2D extends FlowUnitBasic
	{
	public static final String showName="Hit-miss (binary) 2D";
	private static final String metaType="morphologyHitmissBinary2d";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitMorphHitmissBinary2D.class, CategoryInfo.icon,
				"Binary morphological hit-miss operation, slice by slice"));
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

		lastOutput.put("out", new EvOpBinMorphHitmiss2D(kernelHit,kernelMiss).exec1Untyped(exec.ph, a));
		}

	
	}
