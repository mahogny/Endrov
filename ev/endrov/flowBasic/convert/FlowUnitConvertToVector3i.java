/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.convert;


import java.awt.Color;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.util.Vector3i;

/**
 * Flow unit: Convert to Vector3i
 * @author Johan Henriksson
 *
 */
public class FlowUnitConvertToVector3i extends FlowUnitBasic
	{
	public static final String showName="To Vector3i";
	private static final String metaType="channelConvertToVector3i";
	
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitConvertToVector3i.class, CategoryInfo.icon,
		"Convert to Vector3i");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitInput(Vector3i.class, decl);
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return CategoryInfo.icon;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("x", FlowType.TINTEGER); 
		types.put("y", FlowType.TINTEGER); 
		types.put("z", FlowType.TINTEGER); 
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", FlowType.TVECTOR3I);
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		int x=(Integer)flow.getInputValue(this, exec, "x");
		int y=(Integer)flow.getInputValue(this, exec, "y");
		int z=(Integer)flow.getInputValue(this, exec, "z");
		
		lastOutput.put("out", new Vector3i(x,y,z));
		}

	
	}
