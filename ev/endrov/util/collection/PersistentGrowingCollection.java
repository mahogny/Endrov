/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util.collection;

import java.util.Iterator;


/**
 * Persistent growing collection of objects. Unlike a linked list, the elements are not ordered.
 * This makes is very cheap to implement O(1) addition of other collections.
 * Elements can exist multiple times.
 * 
 * @author Johan Henriksson
 *
 */
public class PersistentGrowingCollection<E> 
	{
	private PersistentGrowingCollection<E> a;
	private PersistentGrowingCollection<E> b;
	private Maybe<E> elem=new Maybe<E>();
	
	public PersistentGrowingCollection(E a)
		{
		elem=Maybe.just(a);
		}
	
	public PersistentGrowingCollection(E a, PersistentGrowingCollection<E> b)
		{
		elem=Maybe.just(a);
		this.a=b;
		}
	
	public PersistentGrowingCollection(PersistentGrowingCollection<E> a, PersistentGrowingCollection<E> b)
		{
		this.a=a;
		this.b=b;
		}
	
	public static <E> int size(PersistentGrowingCollection<E> c)
		{
		if(c==null)
			return 0;
		else
			{
			int one=c.elem.hasValue() ? 1 : 0;
			return size(c.a)+size(c.b)+one;
			}
	
		}

	public static <E> Iterator<E> iterator(final PersistentGrowingCollection<E> This)
		{
		return new Iterator<E>(){
		
			PersistentSLinkedList<PersistentGrowingCollection<E>> todo=This==null ? 
					null
					: 
				new PersistentSLinkedList<PersistentGrowingCollection<E>>(This);
			
			public boolean hasNext()
				{
				return todo!=null;
				}

			public E next()
				{
				PersistentGrowingCollection<E> e=todo.head();
				todo=todo.tail();
				if(e.a!=null)
					todo=new PersistentSLinkedList<PersistentGrowingCollection<E>>(e.a,todo);
				if(e.b!=null)
					todo=new PersistentSLinkedList<PersistentGrowingCollection<E>>(e.b,todo);

				if(e.elem.hasValue())
					return e.elem.get();
				else
					return next();
				}

			public void remove()
				{
				}
			};
		}
	
	/*
	public PersistentGrowingCollection<E> cons(E a)
		{
		return new PersistentGrowingCollection<E>(a,this);
		}*/
		
	
	}
