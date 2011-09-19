/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowFourier;


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
import endrov.util.Maybe;

/**
 * Flow unit: Inverse fourier transform 2D
 * @author Johan Henriksson
 *
 */
public class FlowUnitInverseFourier2D extends FlowUnitBasic
	{
	public static final String showName="Fourier inverse transform 2D";
	private static final String metaType="transformInverseFourier2D";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitInverseFourier2D.class, null,
				"Inverse fourier transform, slice by slice"));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return null;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("inReal", FlowType.ANYIMAGE);
		types.put("inImag", FlowType.ANYIMAGE);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("outReal", FlowType.ANYIMAGE); //TODO same type as "image"
		types.put("outImag", FlowType.ANYIMAGE); //TODO same type as "image"
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		AnyEvImage inReal=(AnyEvImage)flow.getInputValue(this, exec, "inReal");
		Maybe<AnyEvImage> inImag=flow.getInputValueMaybe(this, exec, "inImag", AnyEvImage.class);
//		Object inImag=flow.getInputValue(this, exec, "inImag");
		
	//	checkNotNull(inReal);
		
		if(!inImag.hasValue())
			{
			AnyEvImage[] outs=new EvOpFourierRealInverseFull2D(true).execUntyped(exec.ph, inReal);
			lastOutput.put("outReal", outs[0]);
			lastOutput.put("outImag", outs[1]); //Could use half-inverse and generate special image?
			}
		else
			{
			AnyEvImage[] outs=new EvOpFourierComplexInverse2D(true).execUntyped(exec.ph, inReal, inImag.get());
			lastOutput.put("outReal", outs[0]);
			lastOutput.put("outImag", outs[1]);
			}
		}

	
	}
