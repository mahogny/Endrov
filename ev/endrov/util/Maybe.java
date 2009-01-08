package endrov.util;


/**
 * Wrapper around pointer that can be a value and can be null, but need to know in addition if
 * it is set. Corresponds to the Haskell Maybe monad.
 * @author Johan Henriksson
 *
 */
public class Maybe<E>
	{
	private final boolean hasValue;
	private E e;
	
	/**
	 * Construct a no-value
	 */
	public Maybe()
		{
		hasValue=false;
		}

	/**
	 * Construct a reference to a value
	 */
	public Maybe(E e)
		{
		hasValue=true;
		this.e=e;
		}

	/**
	 * Get the value
	 */
	public E get()
		{
		if(hasValue)
			return e;
		else
			throw new NullPointerException("Dereferencing Maybe without content");
		}

	/**
	 * Is there a value?
	 */
	public boolean hasValue()
		{
		return hasValue;
		}
	
	/**
	 * Construct value. Nicer to use than constructor since it infers type
	 */
	public static <E> Maybe<E> just(E e)
		{
		return new Maybe<E>(e);
		}
	
	/**
	 * String of value of <> if there is no value
	 */
	public String toString()
		{
		if(hasValue)
			return e.toString();
		else
			return "⊠"; //⊥ ⊠ <>";
		}
	}
