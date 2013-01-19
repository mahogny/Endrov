/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowGenerateImage;


import java.awt.Color;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.util.math.Vector3i;

/**
 * Flow unit: generate gaussian in 3d
 * @author Johan Henriksson
 *
 */
public class FlowUnitGenGaussian3D extends FlowUnitBasic
	{
	public static final String showName="Generate gaussian 3D";
	private static final String metaType="genGaussian3D";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitGenGaussian3D.class, null,
				"Generate a gaussian distribution kernel suitable for convolution"));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return null;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("sigmaX", FlowType.TNUMBER);
		types.put("sigmaY", FlowType.TNUMBER);
		types.put("sigmaZ", FlowType.TNUMBER);
		types.put("dim", FlowType.TVECTOR3I);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", FlowType.TEVSTACK);
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		//Object a=flow.getInputValue(this, exec, "image");
		Number sigmaX=(Number)flow.getInputValue(this, exec, "sigmaX");
		Number sigmaY=(Number)flow.getInputValue(this, exec, "sigmaY");
		Number sigmaZ=(Number)flow.getInputValue(this, exec, "sigmaZ");
		Vector3i dim=(Vector3i)flow.getInputValue(this, exec, "dim");


		lastOutput.put("out", GenerateSpecialImage.genGaussian3D(exec.ph,sigmaX.doubleValue(), sigmaY.doubleValue(), sigmaZ.doubleValue(),
				dim.x,dim.y,dim.z));
		}

	
	}
