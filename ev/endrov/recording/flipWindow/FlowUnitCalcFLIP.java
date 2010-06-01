/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.flipWindow;


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
import endrov.roi.ROI;

/**
 * Flow unit: Calculate FRAP values
 * @author Johan Henriksson
 *
 */
public class FlowUnitCalcFLIP extends FlowUnitBasic
	{
	public static final String showName="Calculate FRAP";
	private static final String metaType="calcFRAP";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitCalcFLIP.class, null,
				"Calculate parameters from a FRAP experiment"));
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
		types.put("t1", FlowType.TNUMBER);
		types.put("t2", FlowType.TNUMBER);
		
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("lifetime", FlowType.TDOUBLE); 
		types.put("mobile", FlowType.TDOUBLE);
		types.put("series", new FlowType(double[][].class));
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		EvChannel ch=(EvChannel)flow.getInputValue(this, exec, "ch");
		ROI roi=(ROI)flow.getInputValue(this, exec, "roi");
		Number t1=(Number)flow.getInputValue(this, exec, "t1");
		Number t2=(Number)flow.getInputValue(this, exec, "t2");
		
		EvOpCalcFLIP calc=new EvOpCalcFLIP(ch,roi,t1,t2,"foo");
		
		double[][] series=new double[2][calc.recoveryCurve.size()];
		int i=0;
		for(Map.Entry<Double, Double> e:calc.recoveryCurve.entrySet())
			{
			series[0][i]=e.getKey();
			series[1][i]=e.getValue();
			System.out.println(">>>> "+series[0][i]+"   "+series[1][i]);
			i++;
			}
		
		lastOutput.put("lifetime", calc.lifetime);
		lastOutput.put("mobile", calc.mobileFraction);
		lastOutput.put("series", series);
		}

	
	}
