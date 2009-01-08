package endrov.flow.std.collection;

import java.awt.Color;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.basicWindow.FlowExec;
import endrov.flow.Flow;
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
		Flow.addUnitType(new FlowUnitDeclaration("Collection","HeadTail",metaType,FlowUnitHeadTail.class, icon));
		}
	
	public String getBasicShowName()
		{
		return "HeadTail";
		}
	public ImageIcon getIcon(){return icon;}

	public Color getBackground()
		{
		return new Color(200,255,200);
		}

	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}

	
	/** Get types of flows in */
	public SortedMap<String, FlowType> getTypesIn()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("in", null);
		return types;
		}
	/** Get types of flows out */
	public SortedMap<String, FlowType> getTypesOut()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("head", null);
		types.put("tail", null);
		return types;
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
	{
//	Map<String,Object> lastOutput=exec.getLastOutput(this);
	//TODO flowunit
	}

	
	}
