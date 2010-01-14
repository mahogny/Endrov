/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.unsortedImageFilters;


import java.awt.Color;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.flowBasic.images.CategoryInfo;
import endrov.flowBasic.images.FlowUnitChannelDim2D;
import endrov.imageset.AnyEvImage;

/**
 * Flow unit: Copy resolution (XYZ) from one channel to another
 * @author Johan Henriksson
 *
 */
public class TODOFlowUnitCopyResolution extends FlowUnitBasic
	{
	public static final String showName="Copy resolution";
	private static final String metaType="copyResolution";
	
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,TODOFlowUnitCopyResolution.class, FlowUnitChannelDim2D.icon,
		"Copy resolution (XYZ) from source to destination. Might make viewing of generated images easier");
		Flow.addUnitType(decl);
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return FlowUnitChannelDim2D.icon;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("source", FlowType.ANYIMAGE);
		types.put("destination", FlowType.ANYIMAGE);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", FlowType.ANYIMAGE); //TODO same as destination 
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		AnyEvImage src=(AnyEvImage)flow.getInputValue(this, exec, "source");
		AnyEvImage dest=(AnyEvImage)flow.getInputValue(this, exec, "destination");

		/*
		if(in instanceof EvPixels)
			{
			EvPixels s=(EvPixels)in;
			lastOutput.put("dim", new Vector3i(s.getWidth(),s.getHeight(),1));
			}
		else if(in instanceof EvStack)
			{
			EvStack s=(EvStack)in;
			lastOutput.put("dim", new Vector3i(s.getWidth(),s.getHeight(),s.getDepth()));
			}
		else if(in instanceof EvChannel)
			{
			EvChannel c=(EvChannel)in;
			EvStack s=c.getFirstStack();
			lastOutput.put("dim", new Vector3i(s.getWidth(),s.getHeight(),s.getDepth()));
			}
		*/
		}

	
	}
