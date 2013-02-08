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
import endrov.typeImageset.AnyEvImage;
import endrov.typeImageset.EvChannel;
import endrov.typeImageset.EvImagePlane;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvStack;
import endrov.util.math.EvDecimal;

/**
 * Flow unit: turn image or stack into channel
 * @author Johan Henriksson
 *
 */
public class FlowUnitWrapInChannel extends FlowUnitBasic
	{
	public static final String showName="Wrap in channel";
	private static final String metaType="wrapInChannel";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitWrapInChannel.class, CategoryInfo.icon,
		"Put stack or pixels into a channel so it can be displayed");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitOutput(EvPixels.class, decl);
		FlowType.registerSuggestCreateUnitOutput(EvStack.class, decl);
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
		
		//Object a=flow.getInputValue(this, exec, "image");
		AnyEvImage in=(AnyEvImage)flow.getInputValue(this, exec, "image");

		if(in instanceof EvPixels)
			{
			EvStack stack=new EvStack();
			stack.setTrivialResolution();
			stack.putPlane(0, new EvImagePlane((EvPixels)in));
			EvChannel chan=new EvChannel();
			chan.putStack(new EvDecimal(0), stack);
			lastOutput.put("out", chan);
			}
		else if(in instanceof EvStack)
			{
			EvChannel chan=new EvChannel();
			chan.putStack(new EvDecimal(0), (EvStack)in);
			lastOutput.put("out", chan);
			}
		else if(in instanceof EvChannel)
			lastOutput.put("out", in);
		}

	public String getHelpArticle()
		{
		return "Misc flow operations";
		}

	}
