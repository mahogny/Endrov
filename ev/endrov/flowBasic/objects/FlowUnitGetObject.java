package endrov.flowBasic.objects;

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
 * Get all objects of a type from a container
 * @author Johan Henriksson
 *
 */
public class FlowUnitGetObject extends FlowUnitBasic
	{
	private static final String metaType="getevobjectsoftype";
	private static final String showName="GetEvObjectsOfType";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitGetObject.class, null,"Get all objects of a type from a container"));		
		}

	
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return null;}
	public Color getBackground(){return CategoryInfo.bgColor;}

	
	
	/** Get types of flows in */
	public void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("container", new FlowType(EvContainer.class));
		types.put("class", new FlowType(Class.class));
		}
	/** Get types of flows out */
	public void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("objects", null);
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		EvContainer con=(EvContainer)flow.getInputValue(this, exec, "container");
		Class<?> cl=(Class<?>)flow.getInputValue(this, exec, "class");
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.put("objects",con.getObjects(cl));
		}
	
	}
