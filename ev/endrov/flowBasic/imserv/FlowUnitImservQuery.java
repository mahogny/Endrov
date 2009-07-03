package endrov.flowBasic.imserv;

import java.awt.Color;
import java.util.Map;
import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;

public class FlowUnitImservQuery extends FlowUnitBasic
	{
	private static final String metaType="imservQuery";

	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("ImServ","Query",metaType,FlowUnitImservQuery.class, null,"Ask for matching entries in ImServ"));
		}
	
	public String getBasicShowName()
		{
		return "ImServ Query";
		}
	public ImageIcon getIcon(){return null;}

	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}

	public Color getBackground()
		{
		return FlowUnitImserv.bgColor;
		}

	
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("imserv", null);
		types.put("query", null);
		}
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("nameList", null);
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
	{
//	Map<String,Object> lastOutput=exec.getLastOutput(this);
	//TODO flowunit
	}

	
	}
