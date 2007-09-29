package evplugin.ev;

import java.util.*;

/**
 * General observer pattern
 * 
 * @author Johan Henriksson
 * @param <E> Observer
 */
public class GeneralObserver<E> implements Iterable<E>
	{
	private WeakHashMap<E, Object> weak=new WeakHashMap<E, Object>();
	private HashSet<E> strong=new HashSet<E>();

	/**
	 * Add a listener as strong (reference will keep it in memory)
	 */
	public synchronized void addStrong(E e)
		{
		strong.add(e);
		}

	/**
	 * Add a listener as weak (reference will not stop listener from being GC:d)
	 */
	public synchronized void addWeak(E e)
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
	public synchronized Iterator<E> iterator()
		{
		Set<E> s=weak.keySet();
		s.addAll(strong);
		//this should be a copy which is important; it cannot be modified during iteration.
		return s.iterator();
		}
	
	}
