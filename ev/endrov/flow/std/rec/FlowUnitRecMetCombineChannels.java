package endrov.flow.std.rec;

import java.awt.Color;
import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.std.collection.CategoryInfo;

/**
 * Recording method: combine channels
 * @author Johan Henriksson
 */
public class FlowUnitRecMetCombineChannels extends FlowUnitRecMet
	{
	private static final String metaType="recmetCombineChannels";

	
	public Integer canConnectNum(){return null;}

	
//	private static ImageIcon icon=new ImageIcon(FlowUnitConcat.class.getResource("jhConcat.png"));

	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,"Combine Channels",metaType,FlowUnitRecMetCombineChannels.class, null,
				"Combine several recording channels into one stack"));
		}

	
	public String getBasicShowName(){return "Combine channels";}
	public ImageIcon getIcon(){return null;}
	public Color getBackground(){return CategoryInfo.bgColor;}

	public String toXML(Element e)
		{
		return metaType;
		}
	public void fromXML(Element e)
		{
		}

	
	

	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		//TODO
		/*
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");
		if(a instanceof String && b instanceof String)
			lastOutput.put("C", ((String)a)+((String)b));
		else
			throw new BadTypeFlowException("Unsupported collection type "+a.getClass()+" & "+b.getClass());*/
		}

	}
