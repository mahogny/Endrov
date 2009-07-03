package endrov.flowBasic.images;


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
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.util.Vector2i;

/**
 * Flow unit: Get dimensions of any image object
 * @author Johan Henriksson
 *
 */
public class FlowUnitChannelDim2D extends FlowUnitBasic
	{
	public static final String showName="Channel dim 2D";
	private static final String metaType="channelDim2D";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitChannelDim2D.class, null,
				"Get width and height of channel, in pixels"));
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
		types.put("dim", FlowType.TVECTOR2I); 
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		AnyEvImage in=(AnyEvImage)flow.getInputValue(this, exec, "image");

		if(in instanceof EvPixels)
			{
			EvPixels s=(EvPixels)in;
			lastOutput.put("dim", new Vector2i(s.getWidth(),s.getHeight()));
			}
		else if(in instanceof EvStack)
			{
			EvStack s=(EvStack)in;
			lastOutput.put("dim", new Vector2i(s.getWidth(),s.getHeight()));
			}
		else if(in instanceof EvChannel)
			{
			EvChannel c=(EvChannel)in;
			EvStack s=c.getFirstStack();
			lastOutput.put("dim", new Vector2i(s.getWidth(),s.getHeight()));
			}
		}

	
	}
