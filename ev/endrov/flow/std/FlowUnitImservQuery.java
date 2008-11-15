package endrov.flow.std;

import java.awt.Color;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclarationTrivial;

public class FlowUnitImservQuery extends FlowUnitBasic
	{
	private static final String metaType="imservQuery";

	public static void initPlugin() {}
	static
		{
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("ImServ","Query",metaType){
		public FlowUnit createInstance(){return new FlowUnitImservQuery();}});
		}
	
	public String getBasicShowName()
		{
		return "ImServ Query";
		}
	public ImageIcon getIcon(){return null;}

	
	public String storeXML(Element e)
		{
		return metaType;
		}

	public Color getBackground()
		{
		return FlowUnitImserv.bgColor;
		}

	
	
	/** Get types of flows in */
	public SortedMap<String, FlowType> getTypesIn()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("imserv", null);
		types.put("query", null);
		return types;
		}
	/** Get types of flows out */
	public SortedMap<String, FlowType> getTypesOut()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("nameList", null);
		return types;
		}
	
	public void evaluate(Flow flow) throws Exception
	{
	//TODO flowunit
	}

	
	}
