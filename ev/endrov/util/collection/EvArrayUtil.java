/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util.collection;

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
