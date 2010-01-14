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
 * Flow unit: fukunaga thresholding
 * @author Johan Henriksson
 *
 */
public class FlowUnitThresholdFukunaga2D extends FlowUnitBasic
	{
	public static final String showName="Fukunaga threshold";
	private static final String metaType="thresholdFukunaga2d";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitThresholdFukunaga2D.class, CategoryInfo.icon,
				"Find optimal threshold which minimizes variance within classes, slice by slice"));
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
		types.put("numClasses", FlowType.TINTEGER);
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
		Integer numClasses=(Integer)flow.getInputValue(this, exec, "numClasses");
		
		AnyEvImage out=new EvOpThresholdFukunaga2D(Threshold2D.MASK, numClasses).exec1Untyped(a);
		lastOutput.put("out", out);
		}

	
	}
