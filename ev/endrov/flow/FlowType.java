/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flow;

import java.io.File;
import java.util.*;

import endrov.core.EvSQLConnection;
import endrov.roi.ROI;
import endrov.typeImageset.EvChannel;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvStack;
import endrov.util.math.Vector2i;
import endrov.util.math.Vector3i;
import endrov.util.mathExpr.MathExpr;


/**
 * The type of the data between two FlowUnits. Support for type inference etc 
 * @author Johan Henriksson
 *
 */
public class FlowType
	{
	//private boolean isUnknown;
	public Set<Class<?>> type=new HashSet<Class<?>>();
	//good enough? what about List<....>? java removes <>. how to restore? manually annotate?
	public static FlowType TCONNECTION=new FlowType(EvSQLConnection.class);
	
	public FlowType(Class<?>... c)
		{
		for(Class<?> cc:c)
			type.add(cc);
		if(type.isEmpty())
			type.add(Object.class);
		}

	public FlowType(Collection<Class<?>> c)
		{
		type.addAll(c);
		if(type.isEmpty())
			type.add(Object.class);
		}
	
	public boolean isEmptyType()
		{
		return type.isEmpty();
		}
	
	
	
	private static List<Class<?>> getRelatedClasses(Class<?> c)
		{
		LinkedList<Class<?>> list=new LinkedList<Class<?>>();
		for(Class<?> x:c.getInterfaces())
			list.add(x);
		Class<?> sup=c;
		while(sup!=Object.class)
			{
			list.add(sup);
			sup=sup.getSuperclass();
			}
		list.add(Object.class);
		return list;
		}
	
	public FlowType intersect(FlowType t)
		{
		FlowType out=new FlowType();
		
		//First preserve what can be preserved, both lists
		for(Class<?> oa:type)
			for(Class<?> ob:t.type)
				{
				Set<Class<?>> common=new HashSet<Class<?>>();
				if(oa==ob)
					//Common case made fast
					//Also, getClasses does not apply to primitive types nor arrays so this fallback is required
					common.add(oa);
				else
					{
					Set<Class<?>> common2=new HashSet<Class<?>>();
					for(Class<?> x:getRelatedClasses(oa))
						common.add(x);
					for(Class<?> x:getRelatedClasses(ob))
						common2.add(x);
//					System.out.println(common);
//					System.out.println(common2);
					common.retainAll(common2);
//					System.out.println(common);
					
					
					//Remove all classes which are parent of another.
					//Eliminate all interfaces that follow from a class definition
					Set<Class<?>> toremove=new HashSet<Class<?>>();
					for(Class<?> x:common)
						for(Class<?> y:common)
							if(x==y.getSuperclass() || Arrays.asList(y.getInterfaces()).contains(x))
								toremove.add(x);
					common.removeAll(toremove);
					//System.out.println("remove "+toremove);
					
					//Comparable is saved. this might be a problem as it really is Comparable<...> but this
					//type information is lost
					
					}
				
				
				
				//System.out.println(common);
				
				

				
				
				out.type.addAll(common);
				}
	
		
			
		return out;
		}
	
	/**
	 * Check if the given class is accepted for this type
	 */
	public boolean supports(Class<?> c)
		{
		for(Class<?> t:type)
			if(t.isAssignableFrom(c))
				return true;
		return false;
		}
	
	
	public static final FlowType TFILE=new FlowType(File.class);
	public static final FlowType TINTEGER=new FlowType(Integer.class);
	public static final FlowType TDOUBLE=new FlowType(Double.class);
	public static final FlowType TSTRING=new FlowType(String.class);
	public static final FlowType TBOOLEAN=new FlowType(Boolean.class);
	public static final FlowType TNUMBER=new FlowType(Number.class);
	public static final FlowType TEVPIXELS=new FlowType(EvPixels.class);
	public static final FlowType TEVSTACK=new FlowType(EvStack.class);
	public static final FlowType TEVCHANNEL=new FlowType(EvChannel.class);
	public static final FlowType TROI=new FlowType(ROI.class);
	public static final FlowType TVECTOR2I=new FlowType(Vector2i.class);
	public static final FlowType TVECTOR3I=new FlowType(Vector3i.class);
	public static final FlowType TANY=new FlowType();
	//	public static final FlowType ANYIMAGE=new FlowType(AnyEvImage.class);
	public static final FlowType ANYIMAGE=new FlowType(EvChannel.class, EvStack.class, EvPixels.class);

	public static final FlowType TMATHEXPRESSION=new FlowType(MathExpr.class);

	/**
	 * Suggested units for a given type
	 */
	private static Map<Class<?>, Set<FlowUnitDeclaration>> suggestCreateUnitInput=new HashMap<Class<?>, Set<FlowUnitDeclaration>>();
	private static Map<Class<?>, Set<FlowUnitDeclaration>> suggestCreateUnitOutput=new HashMap<Class<?>, Set<FlowUnitDeclaration>>();
	
	/**
	 * Register a unit that should be suggested to be automatically created in the right-click list on an input connection
	 */
	public static synchronized void registerSuggestCreateUnitInput(Class<?> type, FlowUnitDeclaration decl)
		{
		registerSuggestCreateUnit(suggestCreateUnitInput, type, decl);
		}
	/**
	 * Register a unit that should be suggested to be automatically created in the right-click list on an output connection
	 */
	public static synchronized void registerSuggestCreateUnitOutput(Class<?> type, FlowUnitDeclaration decl)
		{
		registerSuggestCreateUnit(suggestCreateUnitOutput, type, decl);
		}
	private static synchronized void registerSuggestCreateUnit(Map<Class<?>, Set<FlowUnitDeclaration>> suggestCreateUnit,
			Class<?> type, FlowUnitDeclaration unit)
		{
		synchronized (suggestCreateUnit)
			{
			Set<FlowUnitDeclaration> set=suggestCreateUnit.get(type);
			if(set==null)
				suggestCreateUnit.put(type, set=new HashSet<FlowUnitDeclaration>());
			set.add(unit);
			}
		}
	
	
	/**
	 * Get suggested units to create for a given input type
	 */
	public static Collection<FlowUnitDeclaration> getSuggestCreateUnitInput(FlowType type)
		{
		return getSuggestCreateUnit(suggestCreateUnitInput, type);
		}
	/**
	 * Get suggested units to create for a given output type
	 */
	public static Collection<FlowUnitDeclaration> getSuggestCreateUnitOutput(FlowType type)
		{
		if(type==null)
			{
			System.out.println("type is null!");
			return Collections.emptySet();
			}
//		return getSuggestCreateUnit(suggestCreateUnitOutput, type);
		synchronized (suggestCreateUnitOutput)
			{
			Set<FlowUnitDeclaration> set=new HashSet<FlowUnitDeclaration>();
			System.out.println(type);
			for(Map.Entry<Class<?>, Set<FlowUnitDeclaration>> e:suggestCreateUnitOutput.entrySet())
				{
				boolean supported=false;
				for(Class<?> c:type.type)
					if(e.getKey().isAssignableFrom(c))
						supported=true;
				if(supported)
					set.addAll(e.getValue());
				}
			ArrayList<FlowUnitDeclaration> sortedset=new ArrayList<FlowUnitDeclaration>(set);
			Collections.sort(sortedset, new Comparator<FlowUnitDeclaration>(){
				public int compare(FlowUnitDeclaration arg0, FlowUnitDeclaration arg1)
					{
					return arg0.name.compareTo(arg1.name);
					}
			});
			return sortedset;
			}
		}
	private static Collection<FlowUnitDeclaration> getSuggestCreateUnit(
			Map<Class<?>, Set<FlowUnitDeclaration>> suggestCreateUnit, FlowType type)
		{
		if(type==null)
			{
			System.out.println("type is null!");
			return Collections.emptySet();
			}
		synchronized (suggestCreateUnit)
			{
			Set<FlowUnitDeclaration> set=new HashSet<FlowUnitDeclaration>();
			System.out.println(type);
			for(Map.Entry<Class<?>, Set<FlowUnitDeclaration>> e:suggestCreateUnit.entrySet())
				if(type.supports(e.getKey()))
					set.addAll(e.getValue());
			ArrayList<FlowUnitDeclaration> sortedset=new ArrayList<FlowUnitDeclaration>(set);
			Collections.sort(sortedset, new Comparator<FlowUnitDeclaration>(){
				public int compare(FlowUnitDeclaration arg0, FlowUnitDeclaration arg1)
					{
					return arg0.name.compareTo(arg1.name);
					}
			});
			return sortedset;
			}
		}
	
	
	/**
	 * Any of these types
	 */
	public FlowType or(FlowType b)
		{
		LinkedList<Class<?>> list=new LinkedList<Class<?>>();
		list.addAll(type);
		list.addAll(b.type);
		return new FlowType(list);
		}
	
	public String toString()
		{
		StringBuffer sb=new StringBuffer();
		boolean first=true;
		for(Class<?> c:type)
			{
			if(!first)
				sb.append(" or ");
			first=false;
			sb.append(c.getSimpleName());
			}
		return sb.toString();
		}
	
	public static void main(String[] arg)
		{
		//FlowType a=new FlowType(Imageset.class);
		//FlowType b=new FlowType(EvData.class);
		
		FlowType a=new FlowType(Double.class);
		FlowType b=new FlowType(Integer.class);
		
		
		
		a.intersect(b);
//		System.out.println(a.intersect(b));
		
		
		}
	
	
	
	}
