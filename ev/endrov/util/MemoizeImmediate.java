package endrov.util;

/**
 * Trivial memoized value: Just returns a specified value
 * @author Johan Henriksson
 */
public class MemoizeImmediate<E> extends Memoize<E>
	{
	private E e;
	public MemoizeImmediate(E e)
		{
		this.e=e;
		}
	
	protected E eval()
		{
		return e;
		}
	
	}
