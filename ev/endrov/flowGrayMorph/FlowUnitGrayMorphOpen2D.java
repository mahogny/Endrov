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
 * Flow unit: open 2D
 * @author Johan Henriksson
 *
 */
public class FlowUnitGrayMorphOpen2D extends FlowUnitBasic
	{
	public static final String showName="Gray Open 2D";
	private static final String metaType="grayDilate2d";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitGrayMorphOpen2D.class, null,
				"Gray morphological open operation, slice by slice"));
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

		lastOutput.put("out", new EvOpGrayMorphOpen2D(pw.intValue(),ph.intValue(),kernel).exec1Untyped(a));
		}

	
	}
