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
 * Flow unit: Fourier transform 2D
 * @author Johan Henriksson
 *
 */
public class FlowUnitFourier2D extends FlowUnitBasic
	{
	public static final String showName="Fourier transform 2D";
	private static final String metaType="transformFourier2D";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitFourier2D.class, null,
				"Fourier transform, slice by slice"));
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
		Maybe<Object> inImag=flow.getInputValueMaybe(this, exec, "inImag");
		
		
		if(!inImag.hasValue())
			{
			AnyEvImage[] outs=new EvOpFourierRealForwardFull2D().execUntyped(inReal);
			lastOutput.put("outReal", outs[0]);
			lastOutput.put("outImag", outs[1]);
			}
		else
			{
			AnyEvImage[] outs=new EvOpFourierComplexForward2D().execUntyped(inReal, (AnyEvImage)inImag.get());
			lastOutput.put("outReal", outs[0]);
			lastOutput.put("outImag", outs[1]);
			}
		}

	
	}
