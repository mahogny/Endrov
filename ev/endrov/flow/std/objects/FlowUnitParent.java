package endrov.flow.std.objects;

import java.awt.Color;
import java.util.Map;
import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.data.EvContainer;
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
public class FlowUnitParent extends FlowUnitBasic
	{
	private static final String metaType="getthisparent";
	private static final String showName="Parent";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitParent.class, null,"Get the EvContainer that has this flow"));		
		}
	
	
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return null;}	
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		}
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("parent", new FlowType(EvContainer.class));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.put("parent", exec.getParent());
		}
	
	}
	
