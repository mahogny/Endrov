package endrov.flowFlooding;


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
import endrov.util.Vector3i;

/**
 * Flow unit: Flood select same color
 * @author Johan Henriksson
 *
 */
public class FlowUnitFloodSelectSameColor extends FlowUnitBasic
	{
	public static final String showName="Flood Select Same Color";
	private static final String metaType="floodSelect2D";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitFloodSelectSameColor.class, null,
				"Select region around point with the same color"));
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
		types.put("pos", FlowType.TVECTOR3I);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("region", FlowType.ANYIMAGE); //TODO same type as "image"
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		AnyEvImage image=(AnyEvImage)flow.getInputValue(this, exec, "image");
		Vector3i pos=(Vector3i)flow.getInputValue(this, exec, "pos");
		checkNotNull(image,pos);
		
		lastOutput.put("region", new EvOpFloodSelectSameColor(pos).exec1Untyped(image));
		}

	
	}
