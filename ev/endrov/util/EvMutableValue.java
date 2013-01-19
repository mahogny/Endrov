/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util;

//TODO: add ev observers

/**
 * Mutable value - simply a container of a single value, that can be changed at any time
 * 
 * @author Johan Henriksson
 */
public class EvMutableValue<E>
	{
	static final long serialVersionUID=0;
	private E e;

	
	public EvMutableValue()
		{
		}
	
	public EvMutableValue(E e)
		{
		this.e=e;
		}

	
	public void setValue(E e)
		{
		this.e=e;
		}
	public E getValue()
		{
		return e;
		}
	
	
	}
