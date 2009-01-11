package endrov.flow.std.imageset;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;

/**
 * Filter: project Z, take maximum
 * @author Johan Henriksson
 *
 */
public class Filter2MaxZ extends FlowUnitFilter2
	{
	private static final String metaType="filterMaxZ";

	public String getBasicShowName(){return "MaxZ";}
	public ImageIcon getIcon(){return null;}
	
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		//possibility of concatenation here, if the right type is sent. a wrapper.
		}

	public void fromXML(Element e)
		{
		}
	public String toXML(Element e)
		{
		return metaType;
		}

	
	
	
	}
