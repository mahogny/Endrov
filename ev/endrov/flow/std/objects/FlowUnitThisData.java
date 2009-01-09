package endrov.flow.std.objects;

import java.awt.Color;
import java.util.Map;
import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.data.EvData;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;

/**
 * get "this", the EvData object
 * @author Johan Henriksson
 *
 */
public class FlowUnitThisData extends FlowUnitBasic
	{
	private static final String metaType="getthisdata";
	private static final String showName="ThisData";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitThisData.class, null,"Get the EvData that has this flow"));		
		}
	
	
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return null;}	
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types)
		{
		}
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types)
		{
		types.put("data", new FlowType(EvData.class));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.put("data", exec.getData());
		}
	
	}
	
