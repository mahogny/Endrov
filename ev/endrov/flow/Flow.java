package endrov.flow;

import java.util.*;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.data.EvObjectType;
import endrov.flow.std.FlowUnitImserv;
import endrov.flow.std.FlowUnitImservLoad;
import endrov.flow.std.FlowUnitImservQuery;
import endrov.flow.std.basic.FlowUnitGetObject;
import endrov.flow.std.basic.FlowUnitIf;
import endrov.flow.std.basic.FlowUnitInput;
import endrov.flow.std.basic.FlowUnitOutput;
import endrov.flow.std.basic.FlowUnitScript;
import endrov.flow.std.collection.FlowUnitConcat;
import endrov.flow.std.collection.FlowUnitHeadTail;
import endrov.flow.std.collection.FlowUnitMap;
import endrov.flow.std.collection.FlowUnitSize;
import endrov.flow.std.constants.FlowUnitConstBoolean;
import endrov.flow.std.constants.FlowUnitConstDouble;
import endrov.flow.std.constants.FlowUnitConstInteger;
import endrov.flow.std.constants.FlowUnitConstString;
import endrov.flow.std.math.FlowUnitAdd;
import endrov.flow.std.math.FlowUnitDiv;
import endrov.flow.std.math.FlowUnitSub;

/**
 * Flow object - organisation of filters and work process
 * 
 * @author Johan Henriksson
 *
 */
public class Flow extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	public static Vector<FlowUnitDeclaration> unitDeclarations=new Vector<FlowUnitDeclaration>();
	
	private static final String metaType="flow";
	
	public static void initPlugin() {}
	static
		{
		//TODO readXML in these'
		//TODO strategy for placing components
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Basic","If"){
		public FlowUnit createInstance(){return new FlowUnitIf();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Basic","Script"){
		public FlowUnit createInstance(){return new FlowUnitScript();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Basic","GetObject"){
		public FlowUnit createInstance(){return new FlowUnitGetObject();}});		
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Basic","Input"){
		public FlowUnit createInstance(){return new FlowUnitInput("foo");}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Basic","Output"){
		public FlowUnit createInstance(){return new FlowUnitOutput("foo");}});


		Flow.unitDeclarations.add(new FlowUnitDeclaration("Const","String"){
		public FlowUnit createInstance(){return new FlowUnitConstString("foo");}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Const","Double"){
		public FlowUnit createInstance(){return new FlowUnitConstDouble(666.0);}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Const","Boolean"){
		public FlowUnit createInstance(){return new FlowUnitConstBoolean(true);}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Const","Integer"){
		public FlowUnit createInstance(){return new FlowUnitConstInteger(123);}});

		
		
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","/"){
		public FlowUnit createInstance(){return new FlowUnitDiv();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","*"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","+"){
		public FlowUnit createInstance(){return new FlowUnitAdd();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","-"){
		public FlowUnit createInstance(){return new FlowUnitSub();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","x^y"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","x²"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","Mod"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","Expression"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","Floor"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","Ceil"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","ToInteger"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","ToDouble"){
		public FlowUnit createInstance(){return null;}});

		Flow.unitDeclarations.add(new FlowUnitDeclaration("Stats","Average"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Stats","Variance"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Stats","Median"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Stats","t-test"){
		public FlowUnit createInstance(){return null;}});

		Flow.unitDeclarations.add(new FlowUnitDeclaration("I/O","Read CSV"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("I/O","Read XML"){
		public FlowUnit createInstance(){return null;}});

		Flow.unitDeclarations.add(new FlowUnitDeclaration("Im.Enhance","Contrast"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Im.Math","Convolve"){
		public FlowUnit createInstance(){return null;}});
		
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Collection","Size"){
		public FlowUnit createInstance(){return new FlowUnitSize();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Collection","GetByKey"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Collection","GetBy#"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Collection","Map"){
		public FlowUnit createInstance(){return new FlowUnitMap();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Collection","Fold1"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Collection","HeadTail"){
		public FlowUnit createInstance(){return new FlowUnitHeadTail();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Collection","Concat"){
		public FlowUnit createInstance(){return new FlowUnitConcat();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Collection","Merge"){
		public FlowUnit createInstance(){return new FlowUnitHeadTail();}});
		
		

		Flow.unitDeclarations.add(new FlowUnitDeclaration("Line","TotalLength"){
			public FlowUnit createInstance(){return null;}});

		Flow.unitDeclarations.add(new FlowUnitDeclaration("ImServ","ImServ"){
		public FlowUnit createInstance(){return new FlowUnitImserv();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("ImServ","Load"){
		public FlowUnit createInstance(){return new FlowUnitImservLoad();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("ImServ","Query"){
		public FlowUnit createInstance(){return new FlowUnitImservQuery();}});


		EvData.extensions.put(metaType,new FlowObjectType());
		}

	
	/******************************************************************************************************
	 *            Class: XML Reader and writer of this type of meta object                                *
	 *****************************************************************************************************/
	
	public static class FlowObjectType implements EvObjectType
		{
		public EvObject extractObjects(Element e)
			{
			return extractFlowXML(e);
			}
		}
	public static EvObject extractFlowXML(Element e)
		{
		Flow flow=new Flow();
		//TODO
		return flow;
		//String filterName=e.getAttributeValue("filtername");
		//return filterInfo.get(filterName).readXML(e);
		}

	public void saveMetadata(Element e)
		{
		e.setName(metaType);
		Map<FlowUnit, Integer> numMap=new HashMap<FlowUnit, Integer>();
		int nexti=0;
		//Save all units
		for(FlowUnit u:units)
			{
			numMap.put(u,nexti++);
			Element ne=new Element("unit");
			//u.storeXML(ne);
			
			}
		
		//Save all connections
		for(FlowConn c:conns)
			{
			Element ne=new Element("conn");
			ne.setAttribute("fromUnit", ""+numMap.get(c.fromUnit));
			ne.setAttribute("toUnit", ""+numMap.get(c.toUnit));
			ne.setAttribute("fromArg",c.fromArg);
			ne.setAttribute("toArg",c.toArg);
			e.addContent(ne);
			}
		
		}

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	
	public List<FlowUnit> units=new Vector<FlowUnit>();
	public List<FlowConn> conns=new Vector<FlowConn>();
	
	public Object getInputValue(FlowUnit u, String arg) throws Exception
		{
		for(FlowConn c:conns)
			if(c.toUnit==u && c.toArg.equals(arg))
				return c.fromUnit.lastOutput.get(c.fromArg);
		throw new Exception("Input not connected - "+arg);
		}
	
	public FlowUnit getInputUnit(FlowUnit u, String arg) throws Exception
		{
		for(FlowConn c:conns)
			if(c.toUnit==u && c.toArg.equals(arg))
				return c.fromUnit;
		throw new Exception("Input not connected - "+arg);
		}
	
	public String getMetaTypeDesc()
		{
		return "Flow";
		//return "Flow ("+getFilterName()+")";
		}

	public void buildMetamenu(JMenu menu)
		{
		}

	
	public void removeUnit(FlowUnit u)
		{
		units.remove(u);
		List<FlowConn> toRemove=new LinkedList<FlowConn>();
		for(FlowConn c:conns)
			if(c.toUnit==u || c.fromUnit==u)
				toRemove.add(c);
		conns.removeAll(toRemove);
		//TODO mark as updated
		}
	
	public void removeUnits(Collection<FlowUnit> us)
		{
		for(FlowUnit u:us)
			removeUnit(u);
		}
	
	
	}
