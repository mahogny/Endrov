package endrov.flow;

import java.util.*;

import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.util.Vector3i;


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
		//Class<?> classes[]=c.getClasses();
		//HashSet<Class<?>> classes2=new HashSet<Class<?>>();
		List<Class<?>> list=Arrays.asList(c.getClasses()); //getclasses might have problems
		for(Class<?> oc:type)
			if(list.contains(oc))
			//if(oc.c.isInstance(oc)) //TODO wrong, this is not how isinstance works
				return true;
		return false;
		}
	
	
	public static final FlowType TINTEGER=new FlowType(Integer.class);
	public static final FlowType TDOUBLE=new FlowType(Double.class);
	public static final FlowType TSTRING=new FlowType(String.class);
	public static final FlowType TBOOLEAN=new FlowType(Boolean.class);
	public static final FlowType TNUMBER=new FlowType(Number.class);
	public static final FlowType TEVPIXELS=new FlowType(EvPixels.class);
	public static final FlowType TVECTOR3I=new FlowType(Vector3i.class);
	public static final FlowType TANY=new FlowType();
	
//	public static final FlowType ANYIMAGE=new FlowType(AnyEvImage.class);
	public static final FlowType ANYIMAGE=new FlowType(EvChannel.class, EvStack.class, EvPixels.class);
	
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
