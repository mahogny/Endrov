package endrov.flow;

import java.util.*;


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
	
	public FlowType(){}
	
	public FlowType(Class<?> c)
		{
		type.add(c);
		}
	
	/*public boolean isUnknown()
		{
		return isUnknown;
		}*/
	
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
	
	public boolean supports(Class<?> c)
		{
		for(Class<?> oc:type)
			if(c.isInstance(oc)) //TODO wrong, this is not how isinstance works
				return true;
		return false;
		}
	
	
	public static final FlowType TINTEGER=new FlowType(Integer.class);
	public static final FlowType TDOUBLE=new FlowType(Double.class);
	public static final FlowType TSTRING=new FlowType(String.class);
	public static final FlowType TBOOLEAN=new FlowType(Boolean.class);
	
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
