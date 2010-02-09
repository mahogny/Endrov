/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util;

/**
 * Turn an immutable class into a mutable class
 * 
 * @author Johan Henriksson
 */
public class Mutable<E>
	{
	private E e;
	
	public Mutable(E e)
		{
		this.e=e;
		}
	
	public E get()
		{
		return e;
		}
	
	public void set(E e)
		{
		this.e=e;
		}
	
	}
