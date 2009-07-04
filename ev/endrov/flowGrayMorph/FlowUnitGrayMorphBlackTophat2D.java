package endrov.flowGrayMorph;


import java.awt.Color;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.EvPixels;

/**
 * Flow unit: black tophat 2D
 * @author Johan Henriksson
 *
 */
public class FlowUnitGrayMorphBlackTophat2D extends FlowUnitBasic
	{
	public static final String showName="Gray Black Tophat 2D";
	private static final String metaType="grayBlackTophat2d";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitGrayMorphBlackTophat2D.class, null,
				"Gray morphological black tophat operation, slice by slice"));
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
		types.put("pw", FlowType.TNUMBER);
		types.put("ph", FlowType.TNUMBER);
		types.put("kernel", FlowType.TEVPIXELS);
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
		
		Object a=flow.getInputValue(this, exec, "image");
		Number pw=(Number)flow.getInputValue(this, exec, "pw");
		Number ph=(Number)flow.getInputValue(this, exec, "ph");
		EvPixels kernel=(EvPixels)flow.getInputValue(this, exec, "kernel");
		
		checkNotNull(a,pw,ph,kernel);

		lastOutput.put("out", new EvOpGrayMorphBlackTophat2D(pw.intValue(),ph.intValue(),kernel).exec1Untyped(a));
		}

	
	}
