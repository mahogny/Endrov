package endrov.util;

public class EvUtil
	{

	

	/**
	 * Check equality, handles null objects
	 */
	public static <E> boolean equalsHandlesNull(E a, E b)
		{
		if(a==null)
			return b==null;
		else if(b==null)
			return false;
		else
			return a.equals(b);
		}
	}
