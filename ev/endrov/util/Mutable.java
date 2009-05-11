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
