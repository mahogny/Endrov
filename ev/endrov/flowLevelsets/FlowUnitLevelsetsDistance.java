/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowLevelsets;


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
 * Flow unit: binarize
 * @author Johan Henriksson
 *
 */
public class FlowUnitLevelsetsDistance extends FlowUnitBasic
	{
	public static final String showName="Levelsets distance";
	private static final String metaType="levelsetsDistance";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitLevelsetsDistance.class, CategoryInfo.icon,
				"Levelsets to find distance to each pixel"));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return CategoryInfo.icon;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("speed", FlowType.ANYIMAGE);
		types.put("origin", FlowType.ANYIMAGE);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("distance", FlowType.ANYIMAGE); //TODO same type as "image"
		types.put("voronoi", FlowType.ANYIMAGE); //TODO same type as "image"
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		AnyEvImage speed=(AnyEvImage)flow.getInputValue(this, exec, "speed");
		AnyEvImage origin=(AnyEvImage)flow.getInputValue(this, exec, "origin");

		Object[] ret=new EvOpLevelsetsFastMarching3D().execUntyped(speed,origin);
		
		lastOutput.put("distance", ret[0]);
		lastOutput.put("voronoi", ret[1]);
		}

	
	}
