package endrov.flow.std.objects;


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
import endrov.imageset.EvChannel;
import endrov.imageset.EvStack;
import endrov.util.EvDecimal;

/**
 * Flow unit: turn image or stack into channel
 * @author Johan Henriksson
 *
 */
public class FlowUnitWrapInChannel extends FlowUnitBasic
	{
	public static final String showName="Wrap in channel";
	private static final String metaType="wrapInChannel";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitWrapInChannel.class, null,
				"Put stack or pixels into a channel so it can be displayed"));
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

		if(in instanceof EvStack)
			{
			EvChannel chan=new EvChannel();
			chan.imageLoader.put(new EvDecimal(0), (EvStack)in);
			lastOutput.put("out", chan);
			}
		}

	
	}
