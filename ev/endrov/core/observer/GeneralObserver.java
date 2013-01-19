/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.core.observer;

import java.util.*;

/**
 * Class that broadcast an event to several listeners 
 * 
 * @author Johan Henriksson
 * @param <E> Observer
 */
public class GeneralObserver<E> 
	{
	private WeakHashMap<E, Object> weak=new WeakHashMap<E, Object>();
	private HashSet<E> strong=new HashSet<E>();

	
	
	/**
	 * Add a listener as strong (reference will keep it in memory)
	 */
	public synchronized void addStrongListener(E e)
		{
		strong.add(e);
		}

	/**
	 * Add a listener as weak (reference will not stop listener from being GC:d)
	 */
	public synchronized void addWeakListener(E e)
		{
		weak.put(e,null);
		}

	/**
	 * Remove a listener
	 */
	public synchronized void remove(E e)
		{
		weak.remove(e);
		strong.remove(e);
		}
	
	/**
	 * Iterate through listeners
	 */
	public synchronized Set<E> getListeners()
		{
		Set<E> s=new HashSet<E>(weak.keySet());
		s.addAll(strong);
		//this should be a copy which is important; it cannot be modified during iteration.
		return s;
		}
	
	}
