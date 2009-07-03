package endrov.flowBasic.rec;

import java.awt.Color;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;
import endrov.flowBasic.collection.CategoryInfo;

/**
 * Recording method: Capture a stack. A slice is a trivial stack, should maybe sort it under here
 * @author Johan Henriksson
 *
 */
public class RecmetStack extends FlowUnitRecmet
	{
	private static final String metaType="recmetStack";
	//need any 2 of: numSlice,sliceThickness,totalLength
	
	//need: startpos, sort by chan or z?
	
	
	
	
	
	
	
	
	public Integer shouldConnectNum(){return 0;}

	
//private static ImageIcon icon=new ImageIcon(FlowUnitConcat.class.getResource("jhConcat.png"));

public static void initPlugin() {}
static
	{
	Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,"Capture stack",metaType,RecmetStack.class, null,
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
