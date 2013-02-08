/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowFlooding;


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
import endrov.util.math.Vector3i;

/**
 * Flow unit: Flood select colors in range
 * @author Johan Henriksson
 *
 */
public class FlowUnitFloodSelectColorRange3D extends FlowUnitBasic
	{
	public static final String showName="Flood Select Color Range 3D";
	private static final String metaType="floodSelectColorRange3D";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitFloodSelectColorRange3D.class, null,
				"Select region around point with color [startColor-rminus, startColor+rplus]"));
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
		types.put("pos", FlowType.TVECTOR3I);
		types.put("rplus", FlowType.TNUMBER);
		types.put("rminus", FlowType.TNUMBER);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("region", FlowType.ANYIMAGE); //TODO same type as "image"
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		AnyEvImage image=(AnyEvImage)flow.getInputValue(this, exec, "image");
		Vector3i pos=(Vector3i)flow.getInputValue(this, exec, "pos");
		Number rangeMinus=(Number)flow.getInputValue(this, exec, "rminus");
		Number rangePlus=(Number)flow.getInputValue(this, exec, "rplus");
		
		lastOutput.put("region", new EvOpFloodSelectColorRange3D(pos,rangeMinus,rangePlus).exec1Untyped(exec.ph, image));
		}


	public String getHelpArticle()
		{
		return "Misc flow operations";
		}
	}
