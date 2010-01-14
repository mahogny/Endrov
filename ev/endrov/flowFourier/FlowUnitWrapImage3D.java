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
import endrov.imageset.AnyEvImage;

/**
 * Flow unit: rotate image around XYZ
 * @author Johan Henriksson
 *
 */
public class FlowUnitWrapImage3D extends FlowUnitBasic
	{
	public static final String showName="Wrap image 3D";
	private static final String metaType="wrapImage3D";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitWrapImage3D.class, null,
				"Moving pixels in XYZ direction, wrap those that fall out around on the other side. By default, half-way"));
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
		types.put("rotx", FlowType.TNUMBER);
		types.put("roty", FlowType.TNUMBER);
		types.put("rotz", FlowType.TNUMBER);
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
		
		Number rotx=(Number)flow.getInputValue(this, exec, "rotx", Number.class, true);
		Number roty=(Number)flow.getInputValue(this, exec, "roty", Number.class, true);
		Number rotz=(Number)flow.getInputValue(this, exec, "rotz", Number.class, true);

		lastOutput.put("out", new EvOpWrapImage3D(rotx,roty,rotz).exec1Untyped(a));
		}

	
	}
