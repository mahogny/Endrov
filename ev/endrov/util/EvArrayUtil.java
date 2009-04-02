package endrov.util;

public class EvArrayUtil
	{
	public static boolean all(boolean b[])
		{
		for(boolean c:b)
			if(!c)
				return false;
		return true;
		}
	
	}
