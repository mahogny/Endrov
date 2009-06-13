package endrov.flowFlooding;


import java.awt.Color;
import java.util.Collections;
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
 * Flow unit: Flood select
 * @author Johan Henriksson
 *
 */
public class FlowUnitFloodSelectSigma extends FlowUnitBasic
	{
	public static final String showName="Flood Select Within Sigma";
	private static final String metaType="floodSelectSigma";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitFloodSelectSigma.class, null,
				"Select region around point with the color in range [mu-f sigma, mu+f sigma]"));
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
		types.put("pos", FlowType.TVECTOR3I); //TODO more than one
		types.put("f", FlowType.TNUMBER);
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
		Number f=(Number)flow.getInputValue(this, exec, "f");
		checkNotNull(image,pos,f);
		
		//TODO
		lastOutput.put("region", new EvOpFloodSelectSigma(Collections.singleton(pos),f.doubleValue()).exec1Untyped(image));
		}

	
	}
