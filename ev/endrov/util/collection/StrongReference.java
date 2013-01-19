package endrov.util.collection;


/**
 * In analogy with other references - Just used as a singleton container
 * @author Johan Henriksson
 *
 */
public class StrongReference<E>
	{
	private E e;

	public StrongReference()
		{
		}

	public StrongReference(E e)
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
