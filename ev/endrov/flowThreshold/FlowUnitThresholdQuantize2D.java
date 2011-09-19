/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
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
 * Flow unit: Quantize colors
 * @author Johan Henriksson
 *
 */
public class FlowUnitThresholdQuantize2D extends FlowUnitBasic
	{
	public static final String showName="Quantize";
	private static final String metaType="quantizeColors2d";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitThresholdQuantize2D.class, CategoryInfo.icon,
				"Reduce number of colors by quantizing, slice by slice. Speeds up histogram-based algorithms"));
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
		types.put("numLevels", FlowType.TINTEGER);
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
		Integer numLevels=(Integer)flow.getInputValue(this, exec, "numLevels");
		
		AnyEvImage out=new EvOpQuantize2D(numLevels).exec1Untyped(exec.ph, a);
		lastOutput.put("out", out);
		}

	
	}
