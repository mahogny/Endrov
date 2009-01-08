package endrov.flow.std;

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

public class FlowUnitImserv extends FlowUnitBasic
	{
	private static final String metaType="imserv.imserv";

	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("ImServ","ImServ",metaType,FlowUnitImserv.class, null));
		}
	
	public String getBasicShowName()
		{
		return "ImServ";
		}
	public ImageIcon getIcon(){return null;}

	public static Color bgColor=new Color(200,255,200);
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}

	
	public Color getBackground()
		{
		return bgColor;
		}

	
	
	/** Get types of flows in */
	public SortedMap<String, FlowType> getTypesIn()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("url", null);
		return types;
		}
	/** Get types of flows out */
	public SortedMap<String, FlowType> getTypesOut()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("imserv", null);
		return types;
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
	{
//	Map<String,Object> lastOutput=exec.getLastOutput(this);
	//TODO flowunit
	}

	
	}
