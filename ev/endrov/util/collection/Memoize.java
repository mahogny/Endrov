/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util.collection;

/**
 * Lazy evaluation and memoization. Haskell semantics for java.
 * @author Johan Henriksson
 */
public abstract class Memoize<E>
	{
	private boolean evaluated=false;
	private E value;
	
	/**
	 * Get value, evaluate if required. Evaluation occurs at most once
	 */
	public synchronized E get() //Can make a cheaper lock
		{
		if(!evaluated)
			{
			value=eval();
			evaluated=true;
			}
		return value;
		}

	/**
	 * Evaluate value
	 */
	protected abstract E eval();
	}
