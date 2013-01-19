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
 * Flow unit: Distance and voronoi 3D
 * @author Johan Henriksson
 *
 */
public class FlowUnitMorphDistanceVoronoi2D extends FlowUnitBasic
	{
	public static final String showName="Distance/Voronoi 3D";
	private static final String metaType="morphologyDistanceVoronoi3d";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitMorphDistanceVoronoi2D.class, CategoryInfo.icon,
				"Closest distance and voronoi, entire stack"));
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
		types.put("alsoDiagonal", FlowType.TBOOLEAN);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("distance", FlowType.ANYIMAGE); //TODO same type as "image"
		types.put("closest", FlowType.ANYIMAGE); //TODO same type as "image"
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		AnyEvImage a=(AnyEvImage)flow.getInputValue(this, exec, "image");
		boolean alsoDiagonal=(Boolean)flow.getInputValue(this, exec, "alsoDiagonal");

		Object[] ret=new EvOpMorphDistanceVoronoi3D(alsoDiagonal).execUntyped(exec.ph, a);
		
		lastOutput.put("distance", ret[0]);
		lastOutput.put("closest", ret[1]);
		}

	
	}
