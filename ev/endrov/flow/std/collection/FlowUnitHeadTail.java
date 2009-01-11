package endrov.flow.std.collection;

import java.awt.Color;
import java.util.Map;
import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;

public class FlowUnitHeadTail extends FlowUnitBasic
	{
	private static final String metaType="headtail";

	private static ImageIcon icon=new ImageIcon(FlowUnitHeadTail.class.getResource("jhHeadTail.png"));

	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,"HeadTail",metaType,FlowUnitHeadTail.class, icon,"Split list into the first element and the rest"));
		}
	
	public String getBasicShowName(){return "HeadTail";}
	public ImageIcon getIcon(){return icon;}
	public Color getBackground(){return CategoryInfo.bgColor;}

	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}

	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("in", null);
		}
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("head", null);
		types.put("tail", null);
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
	{
//	Map<String,Object> lastOutput=exec.getLastOutput(this);
	//TODO flowunit
	}

	
	}
