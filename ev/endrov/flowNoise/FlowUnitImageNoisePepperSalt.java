package endrov.flowNoise;


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

/**
 * Flow unit: Apply pepper and salt noise
 * @author Johan Henriksson
 *
 */
public class FlowUnitImageNoisePepperSalt extends FlowUnitBasic
	{
	public static final String showName="Pepper'n'salt image noise";
	private static final String metaType="imageNoisePepperSalt";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitImageNoisePepperSalt.class, CategoryInfo.icon,
				"Apply salt and pepper noise - pixels fully black, fully white, or ok. 0<=P[pepper]+P[salt]<=1"));
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
		types.put("P[salt]", FlowType.TNUMBER);
		types.put("P[pepper]", FlowType.TNUMBER);
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
		AnyEvImage image=(AnyEvImage)flow.getInputValue(this, exec, "image");
		Number pSalt=(Number)flow.getInputValue(this, exec, "P[salt]");
		Number pPepper=(Number)flow.getInputValue(this, exec, "P[pepper]");
		
		lastOutput.put("out", new EvOpImageNoisePepperSalt(pPepper, pSalt).exec1Untyped(image));
		}

	
	}
