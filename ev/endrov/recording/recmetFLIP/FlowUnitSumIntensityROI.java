/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.recmetFLIP;


import java.awt.Color;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.recording.flow.CategoryInfo;
import endrov.roi.ROI;
import endrov.typeImageset.EvChannel;

/**
 * Flow unit: Calculate sum of intensity in ROI for each time point
 * @author Johan Henriksson
 *
 */
public class FlowUnitSumIntensityROI extends FlowUnitBasic
	{
	public static final String showName="Sum intensity";
	private static final String metaType="sumIntensity";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitSumIntensityROI.class, null,
				"Sum intensity in a ROI for each time point"));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return null;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("ch", FlowType.TEVCHANNEL);
		types.put("roi", FlowType.TROI);
		
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("series", new FlowType(double[][].class));
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		EvChannel ch=(EvChannel)flow.getInputValue(this, exec, "ch");
		ROI roi=(ROI)flow.getInputValue(this, exec, "roi");
		
		EvOpSumIntensityROI calc=new EvOpSumIntensityROI(exec.ph, ch,roi);
		
		double[][] series=new double[2][calc.recoveryCurve.size()];
		int i=0;
		for(Map.Entry<Double, Double> e:calc.recoveryCurve.entrySet())
			{
			series[0][i]=e.getKey();
			series[1][i]=e.getValue();
			System.out.println(">>>> "+series[0][i]+"   "+series[1][i]);
			i++;
			}
		
		lastOutput.put("series", series);
		}

	public String getHelpArticle()
		{
		return "Misc flow operations";
		}

	}
