package endrov.util.collection;

import java.util.LinkedList;
import java.util.WeakHashMap;

/**
 * List for implementing Least-Recently Used scheduling and similar
 * 
 * @author Johan Henriksson
 */
public class LRUlist<E>
	{

	private static class Entry<E>
		{
		private E e;
		
		public Entry(E e)
			{
			this.e = e;
			}

		@Override
		public boolean equals(Object obj)
			{
			if(obj instanceof Entry<?>)
				{
				Entry<?> o=(Entry<?>)obj;
				return o.e==e;
				}
			else
				return false;
			}
		}
	
	
	private LinkedList<Entry<E>> list=new LinkedList<Entry<E>>();
	private WeakHashMap<E,Entry<E>> elems=new WeakHashMap<E, Entry<E>>();
	
	//Currently not efficient O(n) 
	public void addFirst(E e)
		{
		Entry<E> prev=elems.get(e);
		if(prev!=null)
			{
			list.remove(prev);
			}
		else
			{
			prev=new Entry<E>(e);
			elems.put(e,prev);
			}
		list.addFirst(prev);
		}
	

	public E getFirst()
		{
		Entry<E> first=list.pollFirst();
		if(first!=null)
			{
			elems.remove(first);
			return first.e;
			}
		else
			return null;
		}
	
	public void remove(E e)
		{
		Entry<E> prev=elems.get(e);
		if(prev!=null)
			{
			elems.remove(e);
			list.remove(prev);
			}
		
		}
	
	}
