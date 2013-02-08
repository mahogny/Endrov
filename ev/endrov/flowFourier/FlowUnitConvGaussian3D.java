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

/**
 * Flow unit: convolve by gaussian
 * @author Johan Henriksson
 *
 */
public class FlowUnitConvGaussian3D extends FlowUnitBasic
	{
	public static final String showName="Gaussian filter 3D";
	private static final String metaType="convGaussian3D";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitConvGaussian3D.class, null,
				"Gaussian filter (convolution): smoothens the image"));
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
		types.put("sigmaX", FlowType.TNUMBER);
		types.put("sigmaY", FlowType.TNUMBER);
		types.put("sigmaZ", FlowType.TNUMBER);
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
		Number sigmaX=(Number)flow.getInputValue(this, exec, "sigmaX");
		Number sigmaY=(Number)flow.getInputValue(this, exec, "sigmaY");
		Number sigmaZ=(Number)flow.getInputValue(this, exec, "sigmaZ");
		
		lastOutput.put("out", new EvOpConvGaussian3D(sigmaX,sigmaY,sigmaZ).exec1Untyped(exec.ph, a));
		}

	public String getHelpArticle()
		{
		return "Flow operations based on FFT";
		}

	}
