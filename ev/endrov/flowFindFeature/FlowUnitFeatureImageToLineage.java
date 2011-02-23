/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowFindFeature;


import java.awt.Color;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom.Element;


import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.EvChannel;
import endrov.nuc.NucLineage;

/**
 * Flow unit: Find local maximas
 * @author Johan Henriksson
 *
 */
public class FlowUnitFeatureImageToLineage extends FlowUnitBasic
	{
	public static final String showName="Feature image to lineage";
	private static final String metaType="featureImageToLineage";
	
	public static final ImageIcon icon=null;//new ImageIcon(CategoryInfo.class.getResource("jhFlowCategoryFindMaximas.png"));

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitFeatureImageToLineage.class, icon,
				"Turn features in an image to a lineage object"));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return icon;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("features", FlowType.TEVCHANNEL);
		types.put("priority", FlowType.TEVCHANNEL);
		types.put("thresholdradius", FlowType.TDOUBLE);
		types.put("outputradius", FlowType.TDOUBLE);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", new FlowType(NucLineage.class)); 
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		EvChannel chFeatures=(EvChannel)flow.getInputValue(this, exec, "features");
		EvChannel chPriority=(EvChannel)flow.getInputValue(this, exec, "priority");
		Double thresholdRadius=(Double)flow.getInputValue(this, exec, "thresholdradius");
		Double outputRadius=(Double)flow.getInputValue(this, exec, "outputradius");

		if(thresholdRadius==null)
			thresholdRadius=0.0;
		if(outputRadius==null)
			outputRadius=1.0;

		lastOutput.put("out", EvOpFeatureImageToLineage.featureChannelToLineage(chFeatures, chPriority, outputRadius, thresholdRadius));
		}

	
	
	
	}
