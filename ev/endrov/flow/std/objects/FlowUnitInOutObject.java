package endrov.flow.std.objects;

import java.awt.Color;
import java.util.Map;
import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.data.EvContainer;
import endrov.data.EvObject;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.util.Maybe;

/**
 * Get all objects of a type from a container
 * @author Johan Henriksson
 *
 */
public class FlowUnitInOutObject extends FlowUnitBasic
	{
	private static final String metaType="ioevobject";
	private static final String showName="EvObject ref";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitInOutObject.class, null,"Store/load object in a container. Container defaults to parent of this flow."));		
		}

	
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return null;}
	public Color getBackground(){return CategoryInfo.bgColor;}

	
	
	/** Get types of flows in */
	public void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		}
	/** Get types of flows out */
	public void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("container", new FlowType(EvContainer.class));
		types.put("name", new FlowType(String.class));
		types.put("object", new FlowType(EvObject.class));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Maybe<Object> con=flow.getInputValueMaybe(this, exec, "container");
		EvContainer into=flow;
		if(con.hasValue())
			into=(EvContainer)con.get();
		String name=(String)flow.getInputValue(this, exec, "name");
		EvObject ob=(EvObject)flow.getInputValue(this, exec, "object");
		into.metaObject.put(name,ob);
		}
	
	}
