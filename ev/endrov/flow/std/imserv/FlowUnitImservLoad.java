package endrov.flow.std.imserv;

import java.awt.Color;
import java.util.Map;
import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;

/**
 * The need of this class can be discussed. imserv registers general loaders, can use the generic load function
 * @author tbudev3
 *
 */
public class FlowUnitImservLoad extends FlowUnitBasic
	{
	private static final String metaType="imserv.load";

	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("ImServ","Load",metaType,FlowUnitImservLoad.class, null,"Load data from ImServ"));
		}

	
	public String getBasicShowName(){return "ImServ Load";}
	public ImageIcon getIcon(){return null;}

	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public Color getBackground(){return FlowUnitImserv.bgColor;}

	
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("imserv", null);
		types.put("name", null);
		}
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("data", null);
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
	{
//	Map<String,Object> lastOutput=exec.getLastOutput(this);
	//TODO flowunit
	}

	
	}
