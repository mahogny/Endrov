/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util;

import java.util.Collection;
import java.util.Set;

/**
 * Common functions for java Sets
 * 
 * @author Johan Henriksson
 *
 */
public class EvSetUtil
	{

	public static <E> boolean containsAnyOf(Set<E> set, Collection<E> anyof)
		{
		for(E e:anyof)
			if(set.contains(e))
				return true;
		return false;
		}
	
	
	}
