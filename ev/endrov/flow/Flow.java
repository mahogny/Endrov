package endrov.flow;

import java.util.*;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.data.EvObjectType;



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
		
		/*
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Math","*",metaType){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Math","x^y",metaType){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Math","x²",metaType){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Math","Mod",metaType){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Math","Expression",metaType){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Math","Floor",metaType){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Math","Ceil",metaType){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Math","ToInteger,metaType"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Math","ToDouble",metaType){
		public FlowUnit createInstance(){return null;}});*/

		/*
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Stats","Average",metaType){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Stats","Variance",metaType){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Stats","Median",metaType){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Stats","t-test",metaType){
		public FlowUnit createInstance(){return null;}});*/

		/*
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("I/O","Read CSV",metaType){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("I/O","Read XML",metaType){
		public FlowUnit createInstance(){return null;}});

		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Im.Enhance","Contrast",metaType){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Im.Math","Convolve",metaType){
		public FlowUnit createInstance(){return null;}});*/
		
		/*Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Collection","GetByKey",metaType){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Collection","GetBy#",metaType){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Collection","Fold1",metaType){
		public FlowUnit createInstance(){return null;}});*/
		
		

		/*Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Line","TotalLength",metaType){
			public FlowUnit createInstance(){return null;}});*/



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
			String uname=u.storeXML(ne);
			ne.setAttribute("unitname",uname);
			e.addContent(ne);
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
