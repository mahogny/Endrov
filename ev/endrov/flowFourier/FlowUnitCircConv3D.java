/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowFourier;


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
import endrov.typeImageset.EvStack;

/**
 * Flow unit: Circular convolution 3D
 * @author Johan Henriksson
 *
 */
public class FlowUnitCircConv3D extends FlowUnitBasic
	{
	public static final String showName="Circular convolution 3D";
	private static final String metaType="circConv3D";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitCircConv3D.class, null,
				"Circular convolution"));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return null;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("kernel", FlowType.TEVSTACK);
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
		EvStack kernel=(EvStack)flow.getInputValue(this, exec, "kernel");
		AnyEvImage image=(AnyEvImage)flow.getInputValue(this, exec, "image");
		
		lastOutput.put("out", new EvOpCircConv3D(kernel).exec1Untyped(exec.ph, image));
		}

	public String getHelpArticle()
		{
		return "Flow operations based on FFT";
		}

	}
