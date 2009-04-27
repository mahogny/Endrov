package endrov.util;

import java.util.Iterator;

/**
 * Persistent singly linked list. Persistent means that the list is never modified.
 * This only holds true for the list itself; the elements it points to can be modified
 * by the user.
 * 
 * The empty list is the value null.
 * 
 * @author Johan Henriksson
 */
public class PersistentSLinkedList<E> //implements Iterable<E>
	{
	private E e;
	public PersistentSLinkedList<E> next;

	/**
	 * Number of elements
	 * O(n)
	 */
	public static <E> int size(PersistentSLinkedList<E> node)
		{
		int count=0;
		while(node!=null)
			{
			count++;
			node=node.next;
			}
		return count;
		}
	
	/**
	 * Return the first element
	 * O(1)
	 */
	public E head()
		{
		return e;
		}
	
	/**
	 * Return list of following elements
	 * O(1)
	 */
	public PersistentSLinkedList<E> tail()
		{
		return next;
		}
	
	/**
	 * Construct list with single element
	 * O(1)
	 */
	public PersistentSLinkedList(E e)
		{
		this.e=e;
		}
	
	/**
	 * Construct a:b
	 * O(1)
	 */
	public PersistentSLinkedList(E a, PersistentSLinkedList<E> next)
		{
		this.e=a;
		this.next=next;
		}

	/**
	 * Construct a:b
	 * O(1)
	 */
	public static <E> PersistentSLinkedList<E> cons(E a, PersistentSLinkedList<E> b)
		{
		return new PersistentSLinkedList<E>(a,b);
		}

	/**
	 * Iterate over list. Creating iterator is O(1)
	 */
	public Iterator<E> iterator()
		{
		final PersistentSLinkedList<E> This=this;
		return new Iterator<E>(){
			private PersistentSLinkedList<E> list=This;
			public boolean hasNext()
				{
				return list!=null;
				}

			public E next()
				{
				E e=list.e;
				list=list.next;
				return e;
				}

			public void remove()
				{
				}
			};
		}
	
	
	
	
	}
