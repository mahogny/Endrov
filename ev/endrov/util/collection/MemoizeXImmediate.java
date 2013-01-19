/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util.collection;

import endrov.util.ProgressHandle;


/**
 * Trivial memoized value: Just returns a specified value
 * @author Johan Henriksson
 */
public class MemoizeXImmediate<E> extends MemoizeX<E>
	{
	private E e;
	public MemoizeXImmediate(E e)
		{
		this.e=e;
		}
	
	protected E eval(ProgressHandle c)
		{
		return e;
		}
	
	}
