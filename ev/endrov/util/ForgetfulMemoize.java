/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util;

/**
 * Lazy evaluation and memoization and forgetting. Haskell semantics for java with a twist.
 * 
 * The idea is that cheap results can be thrown away, "forgetting". This has no effect on
 * pure functions except trading memory for CPU.
 * 
 * Forgetting is currently not implemented.
 * 
 * @author Johan Henriksson
 */
public abstract class ForgetfulMemoize<E>
	{
	private boolean evaluated=false;
	private E value;
	private boolean allowForget=true;
	
	/**
	 * Get value, evaluate if required. Evaluation occurs at most once
	 */
	public E get()
		{
		if(!evaluated)
			{
			//Here the operation would be timed. Time is the deletion cost. 
			
			
			
			value=eval();
			}
		return value;
		}

	public E getPermanent()
		{
		allowForget=false;
		return get();
		}
	
	public boolean isPermanent()
		{
		return !allowForget;
		}
	
	/**
	 * Evaluate value
	 */
	protected abstract E eval();
	}
