package endrov.flow.std;

import java.awt.Color;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.ImageIcon;

import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;

public class FlowUnitImservLoad extends FlowUnitBasic
	{
	public String getBasicShowName()
		{
		return "ImServ Load";
		}
	public ImageIcon getIcon(){return null;}

	public Color getBackground()
		{
		return FlowUnitImserv.bgColor;
		}

	
	
	/** Get types of flows in */
	public SortedMap<String, FlowType> getTypesIn()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("imserv", null);
		types.put("name", null);
		return types;
		}
	/** Get types of flows out */
	public SortedMap<String, FlowType> getTypesOut()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("data", null);
		return types;
		}
	
	public void evaluate(Flow flow) throws Exception
	{
	//TODO flowunit
	}

	
	}
