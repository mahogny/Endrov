package endrov.flow;

import java.util.*;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.util.Maybe;



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
	
	public static void addUnitType(FlowUnitDeclaration dec)
		{
		unitDeclarations.add(dec);
		}
	
	private static final String metaType="flow";
	
	public static void initPlugin() {}
	static
		{
		
		/*
		addUnitType(new FlowUnitDeclarationTrivial("Math","*",metaType){
		public FlowUnit createInstance(){return null;}});
		addUnitType(new FlowUnitDeclarationTrivial("Math","x^y",metaType){
		public FlowUnit createInstance(){return null;}});
		addUnitType(new FlowUnitDeclarationTrivial("Math","x²",metaType){
		public FlowUnit createInstance(){return null;}});
		addUnitType(new FlowUnitDeclarationTrivial("Math","Mod",metaType){
		public FlowUnit createInstance(){return null;}});
		addUnitType(new FlowUnitDeclarationTrivial("Math","Expression",metaType){
		public FlowUnit createInstance(){return null;}});
		addUnitType(new FlowUnitDeclarationTrivial("Math","Floor",metaType){
		public FlowUnit createInstance(){return null;}});
		addUnitType(new FlowUnitDeclarationTrivial("Math","Ceil",metaType){
		public FlowUnit createInstance(){return null;}});
		addUnitType(new FlowUnitDeclarationTrivial("Math","ToInteger,metaType"){
		public FlowUnit createInstance(){return null;}});
		addUnitType(new FlowUnitDeclarationTrivial("Math","ToDouble",metaType){
		public FlowUnit createInstance(){return null;}});*/

		/*
		addUnitType(new FlowUnitDeclarationTrivial("Stats","Average",metaType){
		public FlowUnit createInstance(){return null;}});
		addUnitType(new FlowUnitDeclarationTrivial("Stats","Variance",metaType){
		public FlowUnit createInstance(){return null;}});
		addUnitType(new FlowUnitDeclarationTrivial("Stats","Median",metaType){
		public FlowUnit createInstance(){return null;}});
		addUnitType(new FlowUnitDeclarationTrivial("Stats","t-test",metaType){
		public FlowUnit createInstance(){return null;}});*/

		/*
		addUnitType(new FlowUnitDeclarationTrivial("I/O","Read CSV",metaType){
		public FlowUnit createInstance(){return null;}});
		addUnitType(new FlowUnitDeclarationTrivial("I/O","Read XML",metaType){
		public FlowUnit createInstance(){return null;}});

		addUnitType(new FlowUnitDeclarationTrivial("Im.Enhance","Contrast",metaType){
		public FlowUnit createInstance(){return null;}});
		addUnitType(new FlowUnitDeclarationTrivial("Im.Math","Convolve",metaType){
		public FlowUnit createInstance(){return null;}});*/
		
		/*addUnitType(new FlowUnitDeclarationTrivial("Collection","GetByKey",metaType){
		public FlowUnit createInstance(){return null;}});
		addUnitType(new FlowUnitDeclarationTrivial("Collection","GetBy#",metaType){
		public FlowUnit createInstance(){return null;}});
		addUnitType(new FlowUnitDeclarationTrivial("Collection","Fold1",metaType){
		public FlowUnit createInstance(){return null;}});*/
		
		

		/*addUnitType(new FlowUnitDeclarationTrivial("Line","TotalLength",metaType){
			public FlowUnit createInstance(){return null;}});*/



		EvData.supportedMetadataFormats.put(metaType,Flow.class);
		}

	
	/******************************************************************************************************
	 *            Class: XML Reader and writer of this type of meta object                                *
	 *****************************************************************************************************/
	
	public void loadMetadata(Element e)
		{
		Map<String, FlowUnitDeclaration> map=new HashMap<String, FlowUnitDeclaration>();
		for(FlowUnitDeclaration dec:unitDeclarations)
			map.put(dec.metadata,dec);

		units.clear();
		conns.clear();
		
		//Load all units
		Map<Integer,FlowUnit> numMap=new HashMap<Integer, FlowUnit>();
		int nexti=0;
		for(Object subo:e.getChildren())
			{
			Element sube=(Element)subo;
			if(sube.getName().equals("unit"))
				{
				//TODO handle unknown units
				String unitname=sube.getAttributeValue("unitname");
				FlowUnitDeclaration dec=map.get(unitname);
				FlowUnit unit=dec.createInstance();
				unit.x=Integer.parseInt(sube.getAttributeValue("unitx"));
				unit.y=Integer.parseInt(sube.getAttributeValue("unity"));
				numMap.put(nexti++, unit);
				unit.fromXML(sube);
				units.add(unit);
				}
			else if(sube.getName().equals("conn"))
				{
				FlowUnit fromUnit=numMap.get(Integer.parseInt(sube.getAttributeValue("fromUnit")));
				FlowUnit toUnit=numMap.get(Integer.parseInt(sube.getAttributeValue("toUnit")));
				String fromArg=sube.getAttributeValue("fromArg");
				String toArg=sube.getAttributeValue("toArg");
				FlowConn c=new FlowConn(fromUnit,fromArg,toUnit,toArg);
				conns.add(c);
				}
			}
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
			String uname=u.toXML(ne);
			ne.setAttribute("unitname",uname);
			ne.setAttribute("unitx",""+u.x);
			ne.setAttribute("unity",""+u.y);
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
	
	/**
	 * Get value that is on one input port
	 */
	public Object getInputValue(FlowUnit u, FlowExec exec, String arg) throws Exception
		{
		for(FlowConn c:conns)
			if(c.toUnit==u && c.toArg.equals(arg))
				{
				Map<String,Object> lastOutput=exec.getLastOutput(c.fromUnit);
				return lastOutput.get(c.fromArg);
				}
		throw new Exception("Input not connected - "+arg);
		}

	/**
	 * Get value that is on one input port. Verify that the value is ok
	 */
	@SuppressWarnings("unchecked")
	public <E> E getInputValue(FlowUnit u, FlowExec exec, String arg, Class<E> cl, boolean canBeNull) throws Exception
		{
		Object o=getInputValue(u, exec, arg);
		if(canBeNull && o==null)
			return null;
		else
			{
			if(cl.isInstance(o)) //False if o is null
				return (E)o;
			else
				throw new BadTypeFlowException("Unsupported type, input value "+arg);
			}
			
		}
	
	/**
	 * Get value that is on one input port
	 */
	public Maybe<Object> getInputValueMaybe(FlowUnit u, FlowExec exec, String arg) throws Exception
		{
		for(FlowConn c:conns)
			if(c.toUnit==u && c.toArg.equals(arg))
				{
				Map<String,Object> lastOutput=exec.getLastOutput(c.fromUnit);
				return Maybe.just(lastOutput.get(c.fromArg));
				}
		return new Maybe<Object>();
		}

	/**
	 * Get unit connected to one input connector
	 */
	public FlowUnit getInputUnit(FlowUnit u, String arg) throws Exception
		{
		for(FlowConn c:conns)
			if(c.toUnit==u && c.toArg.equals(arg))
				return c.fromUnit;
		return null;
//		throw new Exception("Input not connected - "+arg);
		}
	
	/**
	 * Return all flows that goes to one unit
	 */
	public List<FlowConn> getFlowsToUnit(FlowUnit unit)
		{
		LinkedList<FlowConn> newc=new LinkedList<FlowConn>();
		for(FlowConn c:conns)
			if(c.toUnit==unit)
				newc.add(c);
		return newc;
		}
	
	public String getMetaTypeDesc()
		{
		return "Flow";
		}

	public void buildMetamenu(JMenu menu)
		{
		}

	/**
	 * Remove one unit from the flow and dissociate all connections
	 */
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

	/**
	 * Remove multiple units
	 */
	public void removeUnits(Collection<FlowUnit> us)
		{
		for(FlowUnit u:us)
			removeUnit(u);
		}
	
	
	}
