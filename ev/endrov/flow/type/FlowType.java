package endrov.flow.type;

import java.util.*;

import endrov.data.EvData;
import endrov.imageset.Imageset;

/**
 * The type of the data between two FlowUnits. Support for type inference etc 
 * @author Johan Henriksson
 *
 */
public class FlowType
	{
	boolean isUnknown;
	public Set<Class<?>> type=new HashSet<Class<?>>();
	//good enough? what about List<....>? java removes <>
	
	public FlowType(){}
	public FlowType(Class<?> c)
		{
		type.add(c);
		}
	
	public boolean isUnknown()
		{
		return isUnknown;
		}
	
	private static List<Class<?>> getRelatedClasses(Class<?> c)
		{
		LinkedList<Class<?>> list=new LinkedList<Class<?>>();
		for(Class<?> x:c.getInterfaces())
			list.add(x);
		Class<?> sup=c;
//		System.out.println("here "+sup);
		while(sup!=Object.class)
			{
			list.add(sup);
//			System.out.println("-"+sup);
			sup=sup.getSuperclass();
			}
//			System.out.println("here2 "+sup);
		list.add(Object.class);
		return list;
		}
	
	public FlowType intersect(FlowType t)
		{
		FlowType out=new FlowType();

		System.out.println("intersect");
		
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
					System.out.println(common);
					System.out.println(common2);

					
					common.retainAll(common2);

					//Now remove all classes which are parent of another
					Set<Class<?>> toremove=new HashSet<Class<?>>();
					for(Class<?> x:common)
						for(Class<?> y:common)
							if(x==y.getSuperclass())
								toremove.add(x);
					common.removeAll(toremove);
					System.out.println("remove "+toremove);

					//Eliminate all interfaces that follow from any class definition
					
					//TODO
					
					
					
					
					}
					
				System.out.println(common);
				
				

				
				
				out.type.addAll(common);
				}
	
		
			
		return out;
		}
	
	public boolean supports(Class<?> c)
		{
		for(Class<?> oc:type)
			if(c.isInstance(oc))
				return true;
		return false;
		}
	
	
	public static void main(String[] arg)
		{
		FlowType a=new FlowType(Imageset.class);
		FlowType b=new FlowType(EvData.class);
		
		a.intersect(b);
//		System.out.println(a.intersect(b));
		
		
		}
	
	}
