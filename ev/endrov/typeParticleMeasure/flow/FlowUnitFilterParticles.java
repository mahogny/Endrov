/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeParticleMeasure.flow;


import java.awt.Color;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.typeParticleMeasure.ParticleMeasure;
import endrov.util.mathExpr.MathExpr;

/**
 * Flow unit: Filter particle measure according to some mathematical expression
 * @author Johan Henriksson
 *
 */
public class FlowUnitFilterParticles extends FlowUnitBasic
	{
	public static final String showName="Filter particles";
	private static final String metaType="filterParticleMeasure";
	
	public static final ImageIcon icon=CategoryInfo.icon;

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitFilterParticles.class, icon,
				"Filter particles according to some mathematical expression"));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return icon;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("pm", ParticleMeasure.FLOWTYPE);
		types.put("expression", FlowType.TMATHEXPRESSION);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", ParticleMeasure.FLOWTYPE); 
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		ParticleMeasure pm=(ParticleMeasure)flow.getInputValue(this, exec, "pm");

		MathExpr expr=(MathExpr)flow.getInputValue(this, exec, "expression");
		
		lastOutput.put("out", new EvOpFilterParticleMeasure(expr).exec(pm));
		}

	
	}
