/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.colorspace;


import java.awt.Color;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.flowBasic.RendererFlowUtil;
import endrov.typeImageset.AnyEvImage;

/**
 * Flow unit: Convert to HSV
 * @author Johan Henriksson
 *
 */
public class FlowUnitColorRGBtoHSV extends FlowUnitBasic
	{
	public static final String showName="RGB to HSV";
	private static final String metaType="convertRGBtoHSV";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitColorRGBtoHSV.class, CategoryInfo.icon,
				"Convert RGB channels to HSV"));
		}
	

	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return CategoryInfo.icon;}
	public Color getBackground(){return RendererFlowUtil.colOperation;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("R", FlowType.ANYIMAGE);
		types.put("G", FlowType.ANYIMAGE);
		types.put("B", FlowType.ANYIMAGE);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("H", FlowType.ANYIMAGE); //TODO same type as "image"
		types.put("S", FlowType.ANYIMAGE); //TODO same type as "image"
		types.put("V", FlowType.ANYIMAGE); //TODO same type as "image"
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		AnyEvImage inR=(AnyEvImage)flow.getInputValue(this, exec, "R");
		AnyEvImage inG=(AnyEvImage)flow.getInputValue(this, exec, "G");
		AnyEvImage inB=(AnyEvImage)flow.getInputValue(this, exec, "B");
		
		AnyEvImage[] out=new EvOpColorRGBtoHSV().execUntyped(exec.ph, inR,inG,inB);
		lastOutput.put("H", out[0]);
		lastOutput.put("S", out[1]);
		lastOutput.put("V", out[2]);
		}

	public String getHelpArticle()
		{
		return "Colorspace operations";
		}

	}
