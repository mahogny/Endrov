package endrov.util;

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
	public E get()
		{
		if(!evaluated)
			value=eval();
		return value;
		}

	/**
	 * Evaluate value
	 */
	protected abstract E eval();
	}
