/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data;

import java.util.HashSet;

import endrov.basicWindow.EvColor;

/**
 * Common selection system. Does not attempt to update windows, this is the
 * duty of the callee.
 * 
 * @author Johan Henriksson
 *
 */
public class EvSelection
	{
	public static HashSet<EvSelectable> selected=new HashSet<EvSelectable>();
	

	/**
	 * Common operations on selectable objects. This class is the reason for a common
	 * selection system. It also exists to ensure some type safety. 
	 * 
	 * @author Johan Henriksson
	 *
	 */
	public interface EvSelectable
		{
		/**
		 * Set color of selected object
		 */
		public void setColor(EvColor c);
		
		//Delete?
		
		//Should container be selectable?
		}
	
	
	/**
	 * Get selected objects of a given type
	 */
	@SuppressWarnings("unchecked")
	public static <E> HashSet<E> getSelected(Class<E> e)
		{
		HashSet<E> hs=new HashSet<E>();
		for(Object o:selected)
			if(e.isInstance(o))
				hs.add((E)o);
		return hs;
		}
	
	public static boolean isSelected(Object o)
		{
		return selected.contains(o);
		}
	
	public static void unselect(Object o)
		{
		selected.remove(o);
		}
	
	public static void select(EvSelectable o)
		{
		selected.add(o);
		}
	
	public static void selectOnly(EvSelectable... o)
		{
		selected.clear();
		for(EvSelectable t:o)
			selected.add(t);
		}
	
	public static void unselectAll()
		{
		selected.clear();
		}
	
	
	}
